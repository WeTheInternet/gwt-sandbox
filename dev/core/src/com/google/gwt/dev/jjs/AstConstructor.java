/*
 * Copyright 2011 Google Inc.
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
package com.google.gwt.dev.jjs;

import com.google.gwt.core.ext.PropertyOracle;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.dev.CompilerContext;
import com.google.gwt.dev.NullRebuildCache;
import com.google.gwt.dev.PrecompileTaskOptions;
import com.google.gwt.dev.cfg.ConfigurationProperties;
import com.google.gwt.dev.javac.CompilationState;
import com.google.gwt.dev.javac.StandardGeneratorContext;
import com.google.gwt.dev.jdt.RebindPermutationOracle;
import com.google.gwt.dev.jjs.ast.JProgram;
import com.google.gwt.dev.jjs.ast.JTypeOracle.StandardTypes;
import com.google.gwt.dev.jjs.impl.AssertionNormalizer;
import com.google.gwt.dev.jjs.impl.AssertionRemover;
import com.google.gwt.dev.jjs.impl.FixAssignmentsToUnboxOrCast;
import com.google.gwt.dev.jjs.impl.ImplementClassLiteralsAsFields;
import com.google.gwt.dev.jjs.impl.UnifyAst;
import com.google.gwt.dev.jjs.impl.codesplitter.CodeSplitters;
import com.google.gwt.dev.jjs.impl.codesplitter.ReplaceRunAsyncs;
import com.google.gwt.dev.js.ast.JsProgram;

/**
 * Constructs a full Java AST from source.
 */
public class AstConstructor {

  public static JProgram construct(TreeLogger logger, final CompilationState state,
      PrecompileTaskOptions options, ConfigurationProperties config)
      throws UnableToCompleteException {
    CompilerContext compilerContext = new CompilerContext.Builder().options(options)
        .minimalRebuildCache(new NullRebuildCache()).build();
    return construct(logger, state, compilerContext, config);
  }

  /**
   * Construct an simple AST representing an entire {@link CompilationState}.
   * Does not support deferred binding. Implementation mostly copied from
   * {@link JavaToJavaScriptCompiler}.
   */
  public static JProgram construct(TreeLogger logger, final CompilationState state,
      final CompilerContext compilerContext, final ConfigurationProperties config)
      throws UnableToCompleteException {

    InternalCompilerException.preload();

    PrecompilationContext precompilationContext = new PrecompilationContext(
      new RebindPermutationOracle() {
        @Override
        public void clear() {
        }

        @Override
        public String[] getAllPossibleRebindAnswers(TreeLogger logger, String sourceTypeName)
            throws UnableToCompleteException {
          return new String[0];
        }

        @Override
        public CompilationState getCompilationState() {
          return state;
        }

        @Override
        public StandardGeneratorContext getGeneratorContext() {
          return null;
        }

        @Override
        public PropertyOracle getConfigurationPropertyOracle() {
          return config.toConfigurationOnlyPropertyOracle();
        }

      }
    );

    final JProgram jprogram = new JProgram(compilerContext.getMinimalRebuildCache());
    final JsProgram jsProgram = new JsProgram();
    final UnifyAst unifyAst = new UnifyAst(logger, compilerContext, jprogram, jsProgram, precompilationContext);
    unifyAst.buildEverything();

    // Compute all super type/sub type info
    jprogram.typeOracle.computeBeforeAST(StandardTypes.createFrom(jprogram),
        jprogram.getDeclaredTypes(), jprogram.getModuleDeclaredTypes());

    // (3) Perform Java AST normalizations.
    FixAssignmentsToUnboxOrCast.exec(jprogram);

    /*
     * TODO: If we defer this until later, we could maybe use the results of the
     * assertions to enable more optimizations.
     */
    if (compilerContext.getOptions().isEnableAssertions()) {
      // Turn into assertion checking calls.
      AssertionNormalizer.exec(jprogram);
    } else {
      // Remove all assert statements.
      AssertionRemover.exec(jprogram);
    }

    if (compilerContext.getOptions().isRunAsyncEnabled()) {
      ReplaceRunAsyncs.exec(logger, jprogram);
      if (config != null) {
        CodeSplitters.pickInitialLoadSequence(logger, jprogram, config);
      }
    }

    ImplementClassLiteralsAsFields.exec(jprogram, true);
    return jprogram;
  }
}
