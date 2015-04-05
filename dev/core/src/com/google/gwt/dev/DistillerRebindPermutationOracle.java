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
package com.google.gwt.dev;

import com.google.gwt.core.ext.ConfigurationPropertyOnlyOracle;
import com.google.gwt.core.ext.PropertyOracle;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.linker.ArtifactSet;
import com.google.gwt.dev.cfg.BindingProperty;
import com.google.gwt.dev.cfg.BindingProps;
import com.google.gwt.dev.cfg.ConfigProps;
import com.google.gwt.dev.cfg.ModuleDef;
import com.google.gwt.dev.cfg.PropertyPermutations;
import com.google.gwt.dev.cfg.Rule;
import com.google.gwt.dev.javac.CompilationState;
import com.google.gwt.dev.javac.StandardGeneratorContext;
import com.google.gwt.dev.jdt.RebindOracle;
import com.google.gwt.dev.jdt.RebindPermutationOracle;
import com.google.gwt.dev.shell.StandardRebindOracle;
import com.google.gwt.dev.util.Util;
import com.google.gwt.dev.util.collect.HashSet;
import com.google.gwt.dev.util.log.speedtracer.CompilerEventType;
import com.google.gwt.dev.util.log.speedtracer.SpeedTracerLogger;
import com.google.gwt.dev.util.log.speedtracer.SpeedTracerLogger.Event;

import java.util.Deque;
import java.util.Set;

/**
 * Implementation of RebindPermutationOracle used by Precompile.
 */
class DistillerRebindPermutationOracle implements RebindPermutationOracle {
  private CompilationState compilationState;
  private StandardGeneratorContext generatorContext;
  private final Permutation[] permutations;
  private final RebindOracle[] rebindOracles;
  private PropertyOracle propertyOracle;

  public DistillerRebindPermutationOracle(final CompilerContext compilerContext,
      final CompilationState compilationState, final ArtifactSet generatorArtifacts,
      final PropertyPermutations perms) {
    final ModuleDef module = compilerContext.getModule();
    this.compilationState = compilationState;
    permutations = new Permutation[perms.size()];
    rebindOracles = new RebindOracle[perms.size()];
    generatorContext = new StandardGeneratorContext(
        compilerContext, compilationState, generatorArtifacts, true);
    final BindingProperty[] orderedProps = perms.getOrderedProperties();
    final ConfigProps config = new ConfigProps(module);
    final Deque<Rule> rules = module.getRules();
    for (int i = 0; i < rebindOracles.length; ++i) {
      final BindingProps props = new BindingProps(orderedProps, perms.getOrderedPropertyValues(i), config);
      propertyOracle = props.toPropertyOracle();
      rebindOracles[i] = new StandardRebindOracle(propertyOracle, rules,
          generatorContext);
      permutations[i] = new Permutation(i, props);
    }
  }

  @Override
  public void clear() {
    generatorContext.clear();
    compilationState = null;
    generatorContext = null;
  }

  @Override
  public String[] getAllPossibleRebindAnswers(TreeLogger logger,
      final String requestTypeName) throws UnableToCompleteException {

    final String msg = "Computing all possible rebind results for '"
        + requestTypeName + "'";
    logger = logger.branch(TreeLogger.DEBUG, msg, null);

    final Set<String> answers = new HashSet<String>();
    final Event getAllRebindsEvent = SpeedTracerLogger.start(CompilerEventType.GET_ALL_REBINDS);
    for (int i = 0; i < getPermutationCount(); ++i) {
      final String resultTypeName = rebindOracles[i].rebind(logger, requestTypeName);
      answers.add(resultTypeName);
      // Record the correct answer into each permutation.
      permutations[i].putRebindAnswer(requestTypeName, resultTypeName);
    }
    final String[] result = Util.toArray(String.class, answers);
    getAllRebindsEvent.end();
    return result;
  }

  @Override
  public CompilationState getCompilationState() {
    return compilationState;
  }

  @Override
  public StandardGeneratorContext getGeneratorContext() {
    return generatorContext;
  }

  public int getPermutationCount() {
    return rebindOracles.length;
  }

  public Permutation[] getPermutations() {
    return permutations;
  }

  @Override
  public PropertyOracle getConfigurationPropertyOracle() {
    return new ConfigurationPropertyOnlyOracle(propertyOracle);
  }
}
