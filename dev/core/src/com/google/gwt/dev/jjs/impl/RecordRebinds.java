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
package com.google.gwt.dev.jjs.impl;

import java.util.Set;

import com.google.gwt.core.ext.BadPropertyValueException;
import com.google.gwt.core.ext.SelectionProperty;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.dev.Permutation;
import com.google.gwt.dev.cfg.BindingProperty;
import com.google.gwt.dev.cfg.PropertyProvider;
import com.google.gwt.dev.jjs.PrecompilationContext;
import com.google.gwt.dev.jjs.ast.Context;
import com.google.gwt.dev.jjs.ast.JPermutationDependentValue;
import com.google.gwt.dev.jjs.ast.JProgram;
import com.google.gwt.dev.jjs.ast.JVisitor;
import com.google.gwt.dev.util.log.PrintWriterTreeLogger;
import com.google.gwt.dev.util.log.speedtracer.CompilerEventType;
import com.google.gwt.dev.util.log.speedtracer.SpeedTracerLogger;
import com.google.gwt.dev.util.log.speedtracer.SpeedTracerLogger.Event;
import com.google.gwt.thirdparty.guava.common.collect.Sets;

/**
 * Records all live rebinds.
 */
public class RecordRebinds {

  private class RebindVisitor extends JVisitor {
    @Override
    public void endVisit(JPermutationDependentValue x, Context ctx) {
      if (x.isTypeRebind()) {
        liveRebindRequests.add(x.getRequestedValue());
      } else {
        // a property which may or may not cause a permutation to exist...
        // we'll treat any multi-valued property with a property provider as a permutation source.
        String propName = x.getRequestedValue();
        if (seenProperties.add(propName)) {
          try {
            final TreeLogger logger = new PrintWriterTreeLogger();
            final SelectionProperty res = precompilationContext.getRebindPermutationOracle().getGeneratorContext()
                .getPropertyOracle().getSelectionProperty(logger, propName);
            if (res.getPossibleValues().size() > 1) {
              for (Permutation permutation : precompilationContext.getPermutations()) {

                for (BindingProperty bindingProperty : permutation.getProperties().getBindingProperties()) {
                  if (propName.equals(bindingProperty.getName())) {
                    final PropertyProvider provider = bindingProperty.getProvider();
                    if (provider != null) {
                      liveRebindRequests.add(x.getRequestedValue());
                      return;
                    }
                  }
                }
              }
            }
          } catch (BadPropertyValueException ignored) { }
        }
      }
    }
  }


  public static Set<String> exec(JProgram program, PrecompilationContext precompilationContext) {
    Event recordRebindsEvent = SpeedTracerLogger.start(CompilerEventType.RECORD_REBINDS);
    Set<String> liveRebindRequests = Sets.newHashSet();
    new RecordRebinds(program, liveRebindRequests, precompilationContext).execImpl();
    recordRebindsEvent.end();
    return liveRebindRequests;
  }

  private final Set<String> liveRebindRequests;
  private final JProgram program;
  private final PrecompilationContext precompilationContext;
  private final Set<String> seenProperties;

  private RecordRebinds(JProgram program, Set<String> liveRebindRequests, PrecompilationContext precompilationContext) {
    this.program = program;
    this.seenProperties = Sets.newHashSet();
    this.liveRebindRequests = liveRebindRequests;
    this.precompilationContext = precompilationContext;
  }

  private void execImpl() {
    RebindVisitor rebinder = new RebindVisitor();
    rebinder.accept(program);
  }

}
