/*
 * Copyright 2014 Google Inc.
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

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.dev.jjs.ast.Context;
import com.google.gwt.dev.jjs.ast.JMethod;
import com.google.gwt.dev.jjs.ast.JMethodCall;
import com.google.gwt.dev.jjs.ast.JModVisitor;
import com.google.gwt.dev.jjs.ast.JNameOf;
import com.google.gwt.dev.jjs.ast.JNullLiteral;
import com.google.gwt.dev.jjs.ast.JProgram;
import com.google.gwt.dev.jjs.ast.JThisRef;
import com.google.gwt.dev.jjs.ast.js.JsniMethodBody;
import com.google.gwt.dev.jjs.ast.js.JsniMethodRef;
import com.google.gwt.dev.js.ast.JsBlock;
import com.google.gwt.dev.js.ast.JsContext;
import com.google.gwt.dev.js.ast.JsExpression;
import com.google.gwt.dev.js.ast.JsInvocation;
import com.google.gwt.dev.js.ast.JsModVisitor;
import com.google.gwt.dev.js.ast.JsName;
import com.google.gwt.dev.js.ast.JsNameRef;
import com.google.gwt.dev.js.ast.JsScope;
import com.google.gwt.dev.js.ast.JsStatement;
import com.google.gwt.dev.js.ast.JsVars.JsVar;
import com.google.gwt.dev.js.ast.JsVisitable;
import com.google.gwt.dev.util.collect.Lists;
import com.google.gwt.dev.util.collect.Maps;
import com.google.gwt.dev.util.collect.Sets;

/**
 * Java8 defender methods are implemented by creating a forwarding method on each class that
 * inherits the implementation. For a concrete type C inheriting I.m(), there will be an
 * implementation <code>C.m() { return I.super.m(); }</code>.
 *
 * References to I.super.m() are replaced by creating a static version of this method on the
 * interface, and then delegating to it instead.
 */
public class ReplaceDefenderMethodReferences extends JModVisitor {

  private static class JsniDefenderRewriter extends JsModVisitor {
    Map<String, String> methodsToRewrite = Maps.create();
    public JsniDefenderRewriter() {
    }
    
    @Override
    public void endVisit(JsNameRef x, JsContext ctx) {
      // During a JsInvocation, we ignore the nameRef as we will already be processing default methods
      if (!ignoreNameRef && methodsToRewrite.containsKey(x.getIdent())) {
        // Allows us to replace @foo.Bar::method() references to methods that aren't invocations
        // This is for code that wants to extract a function to manually call later on.
        JsNameRef newRef = new JsNameRef(x.getSourceInfo(), methodsToRewrite.get(x.getIdent()));
        ctx.replaceMe(newRef);
      }
      super.endVisit(x, ctx);
    }
  
    boolean ignoreNameRef;
    @Override
    public boolean visit(JsInvocation x, JsContext ctx) {
      // Prevents the @foo.Bar::method() in @foo.Bar::method()() from being replaced
      // This is necessary as invocations must replace any instance.@foo... qualifiers
      ignoreNameRef = true;
      return super.visit(x, ctx);
    }
    @Override
    protected <T extends JsVisitable> void doAcceptList(List<T> collection) {
      // This is called immediately after the JsNameRef in a JsInvocation is processed.
      // This method is used to process the parameters sent to an invocation,
      // which we definitely want to process 
      ignoreNameRef = false;
      super.doAcceptList(collection);
    }
    @Override
    public void endVisit(JsInvocation x, JsContext ctx) {
      // only interested in rewriting methods
      if (x.getQualifier() instanceof JsNameRef) { 
        JsNameRef ref = (JsNameRef)x.getQualifier();
        if (methodsToRewrite.containsKey(ref.getIdent())) {
          JsScope scope = findScope(ref);
          // This invocation needs to be replaced with a new one pointing to static method
          JsName name = scope.declareName(methodsToRewrite.get(ref.getIdent()));
          JsNameRef newRef = new JsNameRef(ref.getSourceInfo(), name.getIdent());

          // Move the qualifer (instance object) into the arguments
          List<JsExpression> args = x.getArguments();
          args = Lists.add(args, 0, ref.getQualifier());
          // Swap out the method invocation
          JsInvocation newInvoke = new JsInvocation(x.getSourceInfo(), newRef, args);
          ctx.replaceMe(newInvoke);
        }
      }
      super.endVisit(x, ctx);
    }
    private JsScope findScope(JsNameRef ref) {
      // Look up until we find a name to derive scope from.
      // Because we're always dealing in instance dispatch,
      // there will always be a var for us to get scope from.
      if (ref.getName() == null) {
        if (ref.getQualifier() == null) {
          throw new NullPointerException("Unable to find scope for "+ref);
        }
        return findScope((JsNameRef)ref.getQualifier());
      } else {
        return ref.getName().getEnclosing();
      }
    }
    public void mark(String ident, String newTarget) {
      methodsToRewrite = Maps.put(methodsToRewrite, ident, newTarget);
    }
    public boolean needsRewrite() {
      return !methodsToRewrite.isEmpty();
    }
  }

  private final MakeCallsStatic.CreateStaticImplsVisitor staticImplCreator;
  private JProgram program;

  public static void exec(JProgram program) {
    ReplaceDefenderMethodReferences visitor =
        new ReplaceDefenderMethodReferences(program);
    visitor.accept(program);
  }

  private ReplaceDefenderMethodReferences(JProgram program) {
    this.program = program;
    this.staticImplCreator = new MakeCallsStatic.CreateStaticImplsVisitor(program);
  }

  @Override
  public void endVisit(JsniMethodBody x, Context ctx) {
    // Whenever we visit a JsniMethodBody, we want to iterate through its collected method refs
    super.endVisit(x, ctx);
    JsniDefenderRewriter rewriter = new JsniDefenderRewriter();
    // Avoid CoMod exceptions
    JsniMethodRef[] refs = x.getJsniMethodRefs().toArray(new JsniMethodRef[0]);
    for (JsniMethodRef method : refs) {
      if (method.getTarget().isDefaultMethod()) {
        // Generate rewritten static dispatch method
        JMethod staticMethod = program.getStaticImpl(method.getTarget());
        if (staticMethod == null) {
          staticImplCreator.accept(method.getTarget());
          staticMethod = program.getStaticImpl(method.getTarget());
        }
        // We now mark the method we want to rewrite, so the visitor knows what to swap
        String stat = staticMethod.getJsniSignature(true, false);
        rewriter.mark(method.getIdent(), "@"+stat);
        // Finally, we want to add a method ref so our static rewrite gets rescued
        x.addJsniRef(new JsniMethodRef(method.getSourceInfo(), "@"+staticMethod.getJsniSignature(true, false), staticMethod, staticMethod.getEnclosingType()));
      }
    }
    if (rewriter.needsRewrite()) {
      // If we've found anything, visit the refs and swap the methods
      rewriter.accept(x.getFunc());
    }
  }
  
  @Override
  public void endVisit(JMethodCall x, Context ctx) {
    JMethod targetMethod = x.getTarget();
    if (targetMethod.isDefaultMethod() && x.isStaticDispatchOnly()) {
      assert x.getInstance() instanceof JThisRef;

      JMethod staticMethod = staticImplCreator.getOrCreateStaticImpl(program, targetMethod);
      // Cannot use setStaticDispatchOnly() here because interfaces don't have prototypes
      JMethodCall callStaticMethod = new JMethodCall(x.getSourceInfo(), null, staticMethod);
      // add 'this' as first parameter
      
      if (x.getInstance() instanceof JNullLiteral) {
        // The instance will be null when making unqualified invocations within the type
        callStaticMethod.addArg(new JThisRef(x.getSourceInfo(), targetMethod.getEnclosingType()));
      } else {
        // However, the instance will not be null when making qualified invocations elsewhere
        // at least, it is when working with JsType interfaces
        callStaticMethod.addArg(x.getInstance());
      }
      callStaticMethod.addArgs(x.getArgs());
      ctx.replaceMe(callStaticMethod);
    }
  }
}
