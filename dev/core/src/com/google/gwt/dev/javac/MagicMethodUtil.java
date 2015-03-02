/**
 *
 */
package com.google.gwt.dev.javac;

import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.lookup.AnnotationBinding;

import com.google.gwt.dev.jjs.ast.JMethod;

/**
 * @author "James X. Nelson (james@wetheinter.net)"
 *
 */
public class MagicMethodUtil {

  public static void maybeSetMagicMethodProperties(final AbstractMethodDeclaration x,
    final JMethod method) {
    if (x.annotations != null) {
      final AnnotationBinding magicMethod = JdtUtil.getAnnotationBySimpleName(x.binding, MAGIC_METHOD_CLASS);
      if (magicMethod != null) {
        final boolean value = JdtUtil.getAnnotationParameterBoolean(magicMethod, "doNotVisit");
        if (value) {
          method.setDoNotVisit();
        }
      }
    }
  }

  public static final String MAGIC_METHOD_CLASS = "MagicMethod";

  private MagicMethodUtil() {}

}
