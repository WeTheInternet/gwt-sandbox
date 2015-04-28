/**
 *
 */
package com.google.gwt.dev;

import com.google.gwt.dev.jjs.ast.JProgram;

/**
 * A simple callback interface used so {@link JProgram} can allow the {@link MinimalRebuildCache}
 * to remove reference only type names whenever a generated unit is injected, without having to
 * include a reference to the JProgram itself.
 *
 * @author James X. Nelson (james@wetheinter.net, @james)
 *
 */
public interface TypeNameCallback {

  void receiveTypeName(String binaryName);

}
