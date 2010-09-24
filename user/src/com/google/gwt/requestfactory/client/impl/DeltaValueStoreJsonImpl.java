/*
 * Copyright 2010 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.gwt.requestfactory.client.impl;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.requestfactory.shared.EntityProxy;
import com.google.gwt.requestfactory.shared.EntityProxyId;
import com.google.gwt.requestfactory.shared.WriteOperation;
import com.google.gwt.requestfactory.shared.impl.CollectionProperty;
import com.google.gwt.requestfactory.shared.impl.Property;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 * <span style="color:red">Experimental API: This class is still under rapid
 * development, and is very likely to be deleted. Use it at your own risk.
 * </span>
 * </p>
 * Accumulates the local edits, made in the context of a
 * {@link com.google.gwt.requestfactory.shared.Request}.
 * 
 */
class DeltaValueStoreJsonImpl {

  static class ReturnRecord extends JavaScriptObject {

    public static final native JsArray<ReturnRecord> getRecords(
        JavaScriptObject response, String operation) /*-{
      return response[operation];
    }-*/;

    private static native void fillKeys(JavaScriptObject jso, HashSet<String> s) /*-{
      for (key in jso) {
        if (jso.hasOwnProperty(key)) {
          s.@java.util.HashSet::add(Ljava/lang/Object;)(key);
        }
      }
    }-*/;

    protected ReturnRecord() {
    }

    public final native void fillViolations(HashMap<String, String> s) /*-{
      for (key in this.violations) {
        if (this.violations.hasOwnProperty(key)) {
          s.@java.util.HashMap::put(Ljava/lang/Object;Ljava/lang/Object;)(key, this.violations[key]);
        }
      }
    }-*/;

    public final String getEncodedId() {
      String parts[] = getSchemaAndId().split("@");
      return parts[1];
    }

    public final native String getFutureId()/*-{
      return this[@com.google.gwt.requestfactory.shared.impl.RequestData::ENCODED_FUTUREID_PROPERTY];
    }-*/;

    public final String getSchema() {
      String parts[] = getSchemaAndId().split("@");
      return parts[0];
    }

    public final native String getSchemaAndId() /*-{
      return this[@com.google.gwt.requestfactory.shared.impl.RequestData::ENCODED_ID_PROPERTY];
    }-*/;

    public final native String getVersion()/*-{
      return this[@com.google.gwt.requestfactory.shared.impl.RequestData::ENCODED_VERSION_PROPERTY];
    }-*/;

    public final native boolean hasFutureId()/*-{
      return @com.google.gwt.requestfactory.shared.impl.RequestData::ENCODED_FUTUREID_PROPERTY in this;
    }-*/;

    public final native boolean hasId()/*-{
      return @com.google.gwt.requestfactory.shared.impl.RequestData::ENCODED_ID_PROPERTY in this;
    }-*/;

    public final native boolean hasViolations()/*-{
      return 'violations' in this;
    }-*/;
  }

  private final ValueStoreJsonImpl master;

  private final RequestFactoryJsonImpl requestFactory;
  // track C-U-D of CRUD operations
  private final Map<EntityProxyId<?>, ProxyJsoImpl> creates = new HashMap<EntityProxyId<?>, ProxyJsoImpl>();

  private final Map<EntityProxyId<?>, ProxyJsoImpl> updates = new HashMap<EntityProxyId<?>, ProxyJsoImpl>();
  // nothing for deletes because DeltaValueStore is not involved in deletes. The
  // operation alone suffices.

  private final Map<EntityProxyId<?>, WriteOperation> operations = new HashMap<EntityProxyId<?>, WriteOperation>();

  private boolean used = false;

  public DeltaValueStoreJsonImpl(ValueStoreJsonImpl master,
      RequestFactoryJsonImpl requestFactory) {
    this.master = master;
    this.requestFactory = requestFactory;
  }

  public void addValidation() {
    throw new UnsupportedOperationException("Auto-generated method stub");
  }

  public <V> V get(Property<V> property, EntityProxy record) {
    ProxyJsoImpl proxy = creates.get(record.stableId());
    if (proxy == null) {
      proxy = updates.get(record.stableId());
    }
    if (proxy == null) {
      return null;
    }
    return proxy.get(property);
  }

  public boolean isChanged() {
    return !operations.isEmpty();
  }

  /**
   * Returns <code>true</code> if a call to {@link #set} was made with the given
   * property and record.
   */
  public <V> boolean isPropertySet(Property<V> property, EntityProxy record) {
    ProxyJsoImpl proxy = creates.get(record.stableId());
    if (proxy == null) {
      proxy = updates.get(record.stableId());
    }
    if (proxy == null) {
      return false;
    }
    return proxy.isDefined(property.getName());
  }

  /**
   * Reset the used flag. To be called only when an unsuccessful reponse has
   * been received after a {@link #toJson()} string has been sent to the server.
   */
  public void reuse() {
    used = false;
  }

  public <V> void set(Property<V> property, EntityProxy record, V value) {
    assertNotUsedAndCorrectType(record);
    ProxyImpl recordImpl = (ProxyImpl) record;
    EntityProxyId<?> recordKey = recordImpl.stableId();

    retainValue(value);

    ProxyJsoImpl rawMasterRecord = master.records.get(recordKey);
    WriteOperation priorOperation = operations.get(recordKey);
    if (rawMasterRecord == null && priorOperation == null) {
      operations.put(recordKey, WriteOperation.CREATE);
      creates.put(recordKey, recordImpl.asJso());
      priorOperation = WriteOperation.CREATE;
    }
    if (priorOperation == null) {
      addNewChangeRecord(recordKey, recordImpl, property, value);
      return;
    }

    ProxyJsoImpl priorRecord = null;
    switch (priorOperation) {
      case CREATE:
        // nothing to do here.
        priorRecord = creates.get(recordKey);
        assert priorRecord != null;
        priorRecord.set(property, value);
        break;
      case UPDATE:
        priorRecord = updates.get(recordKey);
        assert priorRecord != null;

        if (isRealChange(property, value, rawMasterRecord)) {
          priorRecord.set(property, value);
          updates.put(recordKey, priorRecord);
          return;
        }
        /*
         * Not done yet. If the user has changed the value back to the original
         * value, we should eliminate the previous value from the changeRecord.
         * And if the changeRecord is now empty, we should drop it entirely.
         */

        if (priorRecord.isDefined(property.getName())) {
          priorRecord.delete(property.getName());
        }
        if (updates.containsKey(recordKey) && priorRecord.isEmpty()) {
          updates.remove(recordKey);
          operations.remove(recordKey);
        }
        break;
    }
  }

  void processFuturesAndPostEvents(JavaScriptObject returnedJso) {
    HashSet<String> keys = new HashSet<String>();
    ReturnRecord.fillKeys(returnedJso, keys);

    Set<EntityProxyId<?>> toRemove = new HashSet<EntityProxyId<?>>();
    for (WriteOperation writeOperation : WriteOperation.values()) {
      if (!keys.contains(writeOperation.getUnObfuscatedEnumName())) {
        continue;
      }
      JsArray<ReturnRecord> returnedRecords = ReturnRecord.getRecords(
          returnedJso, writeOperation.getUnObfuscatedEnumName());
      int length = returnedRecords.length();
      for (int i = 0; i < length; i++) {
        ReturnRecord returnedRecord = returnedRecords.get(i);
        ProxySchema<?> schema = requestFactory.getSchema(returnedRecord.getSchema());
        // IMPORTANT: The future mapping must be processed before creating the proxyId.
        if (writeOperation == WriteOperation.CREATE) {
          requestFactory.datastoreToFutureMap.put(
              returnedRecord.getEncodedId(), schema,
              returnedRecord.getFutureId());
          requestFactory.futureToDatastoreMap.put(returnedRecord.getFutureId(),
              returnedRecord.getEncodedId());
        }
        final EntityProxyIdImpl<?> proxyId = getPersistedProxyId(
            returnedRecord.getEncodedId(), schema);
        /*
         * TODO(amitmanjhi): replace copy in postChangeEvent by EntityProxyId
         * since only the id and schema of the copy are used.
         */
        ProxyJsoImpl copy = ProxyJsoImpl.create((String) proxyId.encodedId, 1,
            schema, requestFactory);
        toRemove.add(proxyId);
        if (writeOperation == WriteOperation.CREATE) {
          /*
           * TODO(robertvawter): remove this assert after reverting the addition
           * of unpersisted proxies to ValueStore.
           */
          assert master.records.containsKey(proxyId);
        } else {
          requestFactory.postChangeEvent(copy, writeOperation);
        }
        if (writeOperation == WriteOperation.DELETE) {
          master.records.remove(proxyId);
        }
      }
      processToRemove(toRemove, writeOperation);
    }
  }

  /**
   * Clean-up logic to ensure that any values referenced in the payload will be
   * transmitted to the server. Specifically, this ensures that all referenced,
   * future ProxyImpls will be transmitted to the server, even if they have no
   * associated property changes.
   */
  void retainValue(Object value) {
    if (value instanceof Iterable<?>) {
      // Retain values in collections
      for (Object o : (Iterable<?>) value) {
        retainValue(o);
        return;
      }
    }

    if (!(value instanceof ProxyImpl)) {
      // Ignore anything that's not a proxy
      return;
    }

    ProxyImpl proxy = (ProxyImpl) value;
    if (!proxy.unpersisted()) {
      // A persisted proxy doesn't need to be retained
      return;
    }

    EntityProxyId<?> id = proxy.stableId();
    if (operations.containsKey(id)) {
      // Already retained
      return;
    }

    // Retain
    creates.put(id, proxy.asJso());
    operations.put(id, WriteOperation.CREATE);
  }

  /**
   * Has side effect of setting the used flag, meaning further sets will fail
   * until clearUsed is called. Cannot be called while used.
   */
  String toJson() {
    assertNotUsed();

    used = true;
    StringBuffer jsonData = new StringBuffer("{");
    for (WriteOperation writeOperation : new WriteOperation[] {
        WriteOperation.CREATE, WriteOperation.UPDATE}) {
      String jsonDataForOperation = getJsonForOperation(writeOperation);
      if (jsonDataForOperation.equals("")) {
        continue;
      }
      if (jsonData.length() > 1) {
        jsonData.append(",");
      }
      jsonData.append(jsonDataForOperation);
    }
    jsonData.append("}");
    return jsonData.toString();
  }

  /**
   * returns true if a new change record has been added.
   */
  private <V> boolean addNewChangeRecord(EntityProxyId<?> recordKey,
      ProxyImpl recordImpl, Property<V> property, V value) {
    ProxyJsoImpl rawMasterRecord = master.records.get(recordKey);
    ProxyJsoImpl changeRecord = newChangeRecord(recordImpl);
    if (isRealChange(property, value, rawMasterRecord)) {
      changeRecord.set(property, value);
      if (recordImpl.unpersisted()) {
        creates.put(recordKey, changeRecord);
        operations.put(recordKey, WriteOperation.CREATE);
      } else {
        updates.put(recordKey, changeRecord);
        operations.put(recordKey, WriteOperation.UPDATE);
      }
      return true;
    }
    return false;
  }

  private void assertNotUsed() {
    if (used) {
      throw new IllegalStateException("Cannot refire request before "
          + "response received, or after successful response");
    }
  }

  private void assertNotUsedAndCorrectType(EntityProxy record) {
    assertNotUsed();

    if (!(record instanceof ProxyImpl)) {
      throw new IllegalArgumentException(record + " + must be an instance of "
          + ProxyImpl.class);
    }
  }

  private String getJsonForOperation(WriteOperation writeOperation) {
    assert (writeOperation == WriteOperation.CREATE || writeOperation == WriteOperation.UPDATE);
    Map<EntityProxyId<?>, ProxyJsoImpl> recordsMap = getRecordsMap(writeOperation);
    if (recordsMap.size() == 0) {
      return "";
    }
    StringBuffer requestData = new StringBuffer("\""
        + writeOperation.getUnObfuscatedEnumName() + "\":[");
    boolean first = true;
    for (Map.Entry<EntityProxyId<?>, ProxyJsoImpl> entry : recordsMap.entrySet()) {
      ProxyJsoImpl impl = entry.getValue();
      if (first) {
        first = false;
      } else {
        requestData.append(",");
      }
      requestData.append("{\"" + entry.getValue().getSchema().getToken()
          + "\":");
      requestData.append(impl.toJson());
      requestData.append("}");
    }
    requestData.append("]");
    return requestData.toString();
  }

  private EntityProxyIdImpl<?> getPersistedProxyId(String encodedId,
      ProxySchema<?> schema) {
    return new EntityProxyIdImpl<EntityProxy>(encodedId, schema,
        RequestFactoryJsonImpl.NOT_FUTURE,
        requestFactory.datastoreToFutureMap.get(encodedId, schema));
  }

  private Map<EntityProxyId<?>, ProxyJsoImpl> getRecordsMap(
      WriteOperation writeOperation) {
    switch (writeOperation) {
      case CREATE:
        return creates;
      case UPDATE:
        return updates;
      default:
        throw new IllegalStateException("unknow writeOperation "
            + writeOperation.getUnObfuscatedEnumName());
    }
  }

  private <V> boolean isRealChange(Property<V> property, V value,
      ProxyJsoImpl rawMasterRecord) {
    ProxyJsoImpl masterRecord = null;

    if (rawMasterRecord == null) {
      return true;
    }

    if (property instanceof CollectionProperty<?, ?>) {
      return true;
    }

    masterRecord = rawMasterRecord.cast();

    if (!masterRecord.isDefined(property.getName())) {
      return true;
    }

    V masterValue = masterRecord.get(property);

    if (masterValue == value) {
      return false;
    }

    if ((masterValue != null)) {
      return !masterValue.equals(value);
    }

    return true;
  }

  private ProxyJsoImpl newChangeRecord(ProxyImpl fromRecord) {
    return ProxyJsoImpl.emptyCopy(fromRecord.asJso());
  }

  private void processToRemove(Set<EntityProxyId<?>> toRemove,
      WriteOperation writeOperation) {
    for (EntityProxyId<?> recordKey : toRemove) {
      operations.remove(recordKey);
      if (writeOperation == WriteOperation.CREATE) {
        creates.remove(recordKey);
      } else if (writeOperation == WriteOperation.UPDATE) {
        updates.remove(recordKey);
      }
    }
  }
}
