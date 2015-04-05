/*
 * Copyright 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.gwt.core.ext;

import com.google.gwt.core.ext.TreeLogger.Type;

/**
 * A property oracle that prevents access to any properties not named in its predefined set.<br />
 *
 * Used by the generator driver framework to limit property access for the purpose of
 * forcing generators to accurately declare their property dependencies.
 */
public class ConfigurationPropertyOnlyOracle implements PropertyOracle {

  private final PropertyOracle wrappedPropertyOracle;

  public ConfigurationPropertyOnlyOracle(final PropertyOracle wrappedPropertyOracle) {
    this.wrappedPropertyOracle = wrappedPropertyOracle;
  }

  @Override
  public ConfigurationProperty getConfigurationProperty(final String propertyName)
      throws BadPropertyValueException {
    return wrappedPropertyOracle.getConfigurationProperty(propertyName);
  }

  @Override
  public SelectionProperty getSelectionProperty(final TreeLogger logger, final String propertyName)
      throws BadPropertyValueException {
    logger.log(Type.ERROR, "This property oracle does not support retrieval of SelectionProperties");
    throw new UnsupportedOperationException("This property oracle does not support retrieval of SelectionProperties");
  }
}
