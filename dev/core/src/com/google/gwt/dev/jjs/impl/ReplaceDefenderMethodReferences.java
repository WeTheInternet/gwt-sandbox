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

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.dev.jjs.ast.Context;
import com.google.gwt.dev.jjs.ast.JBlock;
import com.google.gwt.dev.jjs.ast.JDeclaredType;
import com.google.gwt.dev.jjs.ast.JExpression;
import com.google.gwt.dev.jjs.ast.JField;
import com.google.gwt.dev.jjs.ast.JFieldRef;
import com.google.gwt.dev.jjs.ast.JInterfaceType;
import com.google.gwt.dev.jjs.ast.JMethod;
import com.google.gwt.dev.jjs.ast.JMethodBody;
import com.google.gwt.dev.jjs.ast.JMethodCall;
import com.google.gwt.dev.jjs.ast.JModVisitor;
import com.google.gwt.dev.jjs.ast.JNullLiteral;
import com.google.gwt.dev.jjs.ast.JProgram;
import com.google.gwt.dev.jjs.ast.JStatement;
import com.google.gwt.dev.jjs.ast.JThisRef;
import com.google.gwt.dev.jjs.ast.JVisitor;
import com.google.gwt.dev.jjs.ast.js.JsniMethodBody;
import com.google.gwt.dev.jjs.ast.js.JsniMethodRef;
import com.google.gwt.dev.js.ast.JsContext;
import com.google.gwt.dev.js.ast.JsExpression;
import com.google.gwt.dev.js.ast.JsInvocation;
import com.google.gwt.dev.js.ast.JsModVisitor;
import com.google.gwt.dev.js.ast.JsName;
import com.google.gwt.dev.js.ast.JsNameRef;
import com.google.gwt.dev.js.ast.JsScope;
import com.google.gwt.dev.js.ast.JsVisitable;
import com.google.gwt.dev.util.collect.Lists;
import com.google.gwt.dev.util.collect.Maps;

/**
 * Java8 defender methods are implemented by creating a forwarding method on each class that
 * inherits the implementation. For a concrete type C inheriting I.m(), there will be an
 * implementation <code>C.m(I i) { return i.m(); }</code>.
 *
 * References to I.super.m() are replaced by creating a static version of this method on the
 * interface, and then delegating to it instead.
 */
public class ReplaceDefenderMethodReferences extends JModVisitor {

  private class FindLambdaVisitor extends JVisitor {

    private String lambdaName;

    public void maybeRewrite(JMethodBody body) {
      lambdaName = body.getMethod().getEnclosingType().getShortName()+"$lambda$";
      for (JStatement statement : body.getStatements()) {
        accept(statement);
      }
    }

    @Override
    public boolean visit(JMethodCall x, Context ctx) {
      if (x.getTarget().getName().startsWith(lambdaName)) {
        // A lambda invoked within a default method must be rewritten in static form
        JMethod targetMethod = x.getTarget();
        JDeclaredType lambdaType = targetMethod.getEnclosingType();
        for (JInterfaceType iface : lambdaType.getImplements()) {
          JMethod sam = getSAM(iface);
          if (sam != null) {
            // Now we know the SAM, we can find the lambda method to rewrite.
            String samSig = sam.getSignature();
            JMethod samMethod = lambdaType.findMethod(samSig, false);
            assert samMethod != null : "Could not find sam method "+samSig+" in "+lambdaType.toSource();
            JBlock block = ((JMethodBody)samMethod.getBody()).getBlock();
            JStatement statement = block.getStatements().get(0);
            new LambdaRewriteVisitor(lambdaType).accept(statement);
            return true;
          }
        }
      }
      return super.visit(x, ctx);
    }

    private JMethod getSAM(JInterfaceType iface) {
      // At this time, there can only be a single
      // interface with a single non-default method
      for (JMethod m : iface.getMethods()) {
        if (!m.isStatic() && !m.isDefaultMethod() && !m.isSynthetic()) {
          return m;
        }
      }
      return null;
    }

  }

  private class LambdaRewriteVisitor extends JModVisitor {

    private JDeclaredType lambdaClass;

    public LambdaRewriteVisitor(JDeclaredType lambdaClass) {
      this.lambdaClass = lambdaClass;
    }

    @Override
    public boolean visit(JMethodCall x, Context ctx) {
      JMethod targetMethod = x.getTarget();

      JMethod staticMethod = program.getStaticImpl(targetMethod);
      if (staticMethod == null) {
        maybeRewriteLambdas(targetMethod, ctx);
        staticImplCreator.accept(targetMethod);
        staticMethod = program.getStaticImpl(targetMethod);
      }
      // Cannot use setStaticDispatchOnly() here because interfaces don't have prototypes
      JMethodCall callStaticMethod = new JMethodCall(x.getSourceInfo(), null, staticMethod);
      // add 'this' as first parameter

      for (JField field : lambdaClass.getFields()) {
        if (field.getName().equals(GwtAstBuilder.OUTER_LAMBDA_PARAM_NAME)) {
          JFieldRef fieldRef = new JFieldRef(x.getSourceInfo(), new JThisRef(x.getSourceInfo(), targetMethod.getEnclosingType()), field, lambdaClass);
          callStaticMethod.addArg(fieldRef);
          callStaticMethod.addArgs(x.getArgs());
          ctx.replaceMe(callStaticMethod);
          return true;
        }
      }

      return false;
    }
  }

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
  private Set<String> seen;

  public static void exec(JProgram program) {
    ReplaceDefenderMethodReferences visitor =
        new ReplaceDefenderMethodReferences(program);
    visitor.accept(program);
  }

  private ReplaceDefenderMethodReferences(JProgram program) {
    this.program = program;
    this.staticImplCreator = new MakeCallsStatic.CreateStaticImplsVisitor(program);
    this.seen = new HashSet<String>();
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
        JMethod staticMethod = staticImplCreator.getOrCreateStaticImpl(program, method.getTarget());
        // We now mark the method we want to rewrite, so the visitor knows what to swap
        String stat = staticMethod.getJsniSignature(true, false);
        rewriter.mark(method.getIdent(), "@"+stat);
        // Next, we want to add a method ref so our static rewrite gets rescued
        JsniMethodRef methodRef = new JsniMethodRef(method.getSourceInfo(), "@"+staticMethod.getJsniSignature(true, false), staticMethod, staticMethod.getEnclosingType());
        x.addJsniRef(methodRef);
        // Finally, visit the body of the statically rewritten method
        accept(methodRef);
      }
    }
    if (rewriter.needsRewrite()) {
      // If we've found anything, visit the refs and swap the methods
      rewriter.accept(x.getFunc());
    }
  }

  @Override
  public boolean visit(JMethod x, Context ctx) {
    maybeRewriteLambdas(x, ctx);
    return super.visit(x, ctx);
  }

  @Override
  public void endVisit(JMethodCall x, Context ctx) {
    JMethod targetMethod = x.getTarget();
    if (targetMethod.isDefaultMethod() && x.isStaticDispatchOnly()) {

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
        JExpression inst = x.getInstance();
        callStaticMethod.addArg(inst);
      }
      callStaticMethod.addArgs(x.getArgs());
      ctx.replaceMe(callStaticMethod);
      accept(callStaticMethod);
    }
  }

  private void maybeRewriteLambdas(JMethod x, Context ctx) {
    if (x.isDefaultMethod() && seen.add(x.getJsniSignature(true, true))) {
      // For any default method, we must rewrite all lambda invocations, as the AST
      // generated by eclipse will contain this references that must be made static,
      // as JsType interfaces will not actually contain the synthetic lambda methods.

      // This should likely be viewed as a workaround that should be removed
      // by implicitly making all lambda methods static in GwtAstBuilder
      assert x.getBody() instanceof JMethodBody :
        "Jsni (native) method "+x.getSignature()+" cannot be default";
      JMethodBody body = (JMethodBody) x.getBody();
      new FindLambdaVisitor().maybeRewrite(body);;
    }
  }
}
