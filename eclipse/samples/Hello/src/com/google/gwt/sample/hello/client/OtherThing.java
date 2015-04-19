/**
 *
 */
package com.google.gwt.sample.hello.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.reflect.shared.GwtReflect;

import java.lang.reflect.Method;

import org.junit.JUnit4Test;
import org.junit.Test;

/**
 * @author James X. Nelson (james@wetheinter.net, @james)
 *
 */
public class OtherThing {

  private static Class<OtherThing> ENHANCED = GwtReflect.magicClass(OtherThing.class);

  public static boolean doLog() throws Throwable {

    final OtherThing thing = new OtherThing();
    GWT.runAsync(OtherThing.class, new RunAsyncCallback() {

      @Override
      public void onSuccess() {
        try {
          String.class.getMethod("equals", Object.class).invoke("!", "!");
        } catch (final Exception e) {
          Hello.log("Basic string reflection not working; "
            + "expect failures...", e);
        }
        Hello.log("String reflection works!");
        try {
          Hello.log(ENHANCED.getDeclaredMethods());
          for (final Method test : JUnit4Test.findTests(OtherThing.class)) {
            Hello.log(test, test.getName());
            test.invoke(thing);
          }
        } catch(final Throwable e) {
          Hello.log("Bailed finding tests", e);
        }
      }
      /**
       * @see com.google.gwt.core.client.RunAsyncCallback#onFailure(java.lang.Throwable)
       */
      @Override
      public void onFailure(final Throwable reason) {
      }
    });

    return true;
  }

  @Test
  public void testMethod() {
    Hello.log("Hello! ");
  }

}

