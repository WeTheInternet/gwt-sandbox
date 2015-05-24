/*
 * Copyright 2008 Google Inc.
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
package com.google.gwt.core.ext.linker;

import com.google.gwt.core.ext.Linker;

/**
 * A resource created by a Generator invoking
 * {@link com.google.gwt.core.ext.GeneratorContext#tryCreate(com.google.gwt.core.ext.TreeLogger, String, String)}
 * during the compilation process.  Currently only used for resources created via magic method injection;
 * this artifact type is used to build a manifest file for Super Dev Mode so that it can direct
 * sourcemap requests to the correct file.
 */
@Transferable
public class GeneratedSource extends Artifact<GeneratedSource> {

  private static final long serialVersionUID = -7289811447180071354L;
  private final String fileName;
  private final String typeName;

  public GeneratedSource(Class<? extends Linker> linkerType, String typeName, String fileName) {
    super(linkerType);
    this.fileName = fileName;
    this.typeName = typeName;
  }

  @Override
  public int hashCode() {
    return typeName.hashCode();
  }

  @Override
  protected int compareToComparableArtifact(GeneratedSource o) {
    return typeName.compareTo(o.typeName);
  }

  /**
   * @see com.google.gwt.core.ext.linker.Artifact#getComparableArtifactType()
   */
  @Override
  protected Class<GeneratedSource> getComparableArtifactType() {
    return GeneratedSource.class;
  }

  /**
   * @return -> fileName
   */
  public String getFileName() {
    return fileName;
  }

  /**
   * @return -> typeName
   */
  public String getTypeName() {
    return typeName;
  }
}
