/*
 * Copyright 2007 Google Inc.
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
package com.google.gwt.sample.hello.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.reflect.shared.GwtReflect;

/**
 * HelloWorld application.
 */
public class Hello implements EntryPoint {

  @Override
  public void onModuleLoad() {
    Scheduler.get().scheduleDeferred(this::start);
  }
 
  private void start() {
    GWT.runAsync(Magic.class, new RunAsyncCallback() {

      @Override
      public void onSuccess() {
        try {
          GwtReflect.invoke(Hello.class, "doStuff", new Class[0], Hello.this);
        } catch (final Throwable e) {
          throw new RuntimeException(e);
        }
      }

      @Override
      public void onFailure(final Throwable reason) {
      }
    });
  }

  private void doStuff() throws Throwable {

    final Thing thing = new Thing();
    Object result =  Thing.class.getMethod("doLog").invoke(thing);
    log(result,result.getClass().getName()+"!");
    result = OtherThing.doLog();
//    result = GwtReflect.invoke(OtherThing.class, "doLog", new Class<?>[]{}, null);
    log(result,result.getClass().getName()+"!");
  }

  public static native void log(Object result)
  /*-{
     $wnd.console.log(name);
     var div = $doc.createElement('div');
     div.innerHTML = "" + result;
     $doc.body.appendChild(div);
  }-*/;

  public static native void log(Object result, Object name)
  /*-{
     $wnd.console.log(name, result);
      var div = $doc.createElement('div');
      div.innerHTML = result+" : "+name;
      $doc.body.appendChild(div);
  }-*/;
}
