/**
 *
 */
package com.google.gwt.sample.hello.rebind;

import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.TreeLogger.Type;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.dev.javac.StandardGeneratorContext;
import com.google.gwt.dev.jjs.MagicMethodGenerator;
import com.google.gwt.dev.jjs.UnifyAstView;
import com.google.gwt.dev.jjs.ast.Context;
import com.google.gwt.dev.jjs.ast.JClassLiteral;
import com.google.gwt.dev.jjs.ast.JConstructor;
import com.google.gwt.dev.jjs.ast.JDeclaredType;
import com.google.gwt.dev.jjs.ast.JExpression;
import com.google.gwt.dev.jjs.ast.JMethod;
import com.google.gwt.dev.jjs.ast.JMethodCall;
import com.google.gwt.dev.jjs.ast.JNewInstance;
import com.google.gwt.thirdparty.xapi.dev.source.SourceBuilder;

import java.io.PrintWriter;
import java.lang.reflect.Modifier;

/**
 * @author James X. Nelson (james@wetheinter.net, @james)
 *
 */
public class MagicInjector implements MagicMethodGenerator {

  private static int iter = 2;
  @Override
  public JExpression injectMagic(final TreeLogger logger, final JMethodCall methodCall, final JMethod enclosingMethod, final Context context,
      final UnifyAstView ast) throws UnableToCompleteException {
    final JClassLiteral arg = (JClassLiteral) methodCall.getArgs().get(0);
    final String type = arg.getRefType().getShortName();

    String newType = newName(type,iter++);
    final String pkg = "com.google.gwt.sample.hello.client";
    final StandardGeneratorContext ctx = ast.getGeneratorContext();
    PrintWriter pw = ctx.tryCreate(logger, "com.google.gwt.sample.hello.client", newType);
    while (pw == null) {
      newType = newName(type, iter++);
      pw = ctx.tryCreate(logger, pkg, newType);
    }

    final SourceBuilder<PrintWriter> sb = new SourceBuilder<PrintWriter>("public class "+newType);
    logger.log(Type.ERROR, "Injecting "+sb.getQualifiedName());
    sb.setPackage(pkg);
    sb.getClassBuffer()
      .setSuperClass(type)
      .createConstructor(Modifier.PUBLIC);
    pw.println(sb.toString());
    ctx.commit(logger, pw);
    ast.finish(logger);

    final JDeclaredType result = ast.searchForTypeBySource(sb.getQualifiedName());
    for (final JMethod method : result.getMethods()) {
      if (method instanceof JConstructor) {
        return new JNewInstance(method.getSourceInfo(), (JConstructor)method).makeStatement().getExpr();
      }
    }
    throw new UnableToCompleteException();
  }
  /**
   * @param type
   * @param i
   * @return
   */
  private String newName(final String type, final int i) {
    return type+"_"+(i%3==0?i-1:i);
  }

}
