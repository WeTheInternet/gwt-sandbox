## Changes Made in WeTheInter.net's Fork

  This fork of the [Gwt project](http://gwtproject.org) has been modified to  
  support additional features; It tracks Gwt master with periodic rebases; the  
  first released version is 2.7.0, which is based upon Gwt master a few months  
  after the pristine 2.7.0 was released.  New versions of this fork will be  
  released as Gwt master is updated, with these and other enhancements.

  No warranty is supplied, however, for support, you may reach James -AT- WeTheInter -DOT- net.

  Additional features:  
  - Magic Method Injection
  - Java Reflection
  - JSNI for Default Methods

### Magic Method Injection

  This feature is based upon GWT.create, and allows you to declare your own  
  magic methods.  A magic method is a method signature which the compiler swaps  
  out for some other valid AST nodes.  GWT.create uses rebind rules in XML to  
  determine what instance object is produced, but it does not allow you to pass  
  in any additional parameters, thus sometimes requiring a lot of boilerplate  
  code.  You can enable its use by inheriting [com.google.gwt.core.MagicMethods](https://github.com/WeTheInternet/gwt-sandbox/blob/xapi-gwt/user/src/com/google/gwt/core/MagicMethods.gwt.xml).

  Once enabled, you will be able to declaring a mapping from any method  
  signature to a class which implements [MagicMethodGenerator](https://github.com/WeTheInternet/gwt-sandbox/blob/xapi-gwt/dev/core/src/com/google/gwt/dev/jjs/MagicMethodGenerator.java).  The compiler  
  instantiates your generator (called an injector in most cases), and will  
  call your injector whenever it transverses a method matching the signature  
  declared using the "gwt.magic.method" configuration property.

  The exact (and yes, ugly) syntax of this configuration property is as follows  
  `foo.ClassName.method(Lfoo/ParamType;)Lfoo/ReturnType; *= bar.dev.MyInjectorClass`  
  This syntax may be updated if community members would rather see something  
  cleaner, like a classname which has magic method mapping annotations on it.

  The reflection support described below uses this mechanism to extract  
  Class and String literals parameters from core reflection methods to generate  
  runtime support for methods, fields, constructors and annotations.

  Two important features to note are the [UnifyAstListener](https://github.com/WeTheInternet/gwt-sandbox/blob/xapi-gwt/dev/core/src/com/google/gwt/dev/jjs/UnifyAstListener.java) interface,  
  which is used to allow your injector to be notified when the [UnifyAst](https://github.com/WeTheInternet/gwt-sandbox/blob/xapi-gwt/dev/core/src/com/google/gwt/dev/jjs/impl/UnifyAst.java) process  
  begins and ends, and the @[MagicMethod](https://github.com/WeTheInternet/gwt-sandbox/blob/xapi-gwt/user/src/com/google/gwt/core/client/MagicMethod.java) annotation, which can be used to  
  effectively erase the method body of whatever is inside your magic method.  

  UnifyAstListener can be used to allow your injector to collect up calls to a  
  given magic method, and then, once the entire application has been visited,  
  inject some new code to be included into the application (beware, race  
  conditions between injectors could exist if generated code calls other magic  
  methods).  It can also be used to inject code at the beginning of the  
  application that will be run without requiring any particular method to be  
  called.  You may also implement this interface solely for the destroy()  
  method, so that you can clean up any state that you may have stored in your  
  injector.  Do note that all state should be stored in a ThreadLocal, or else  
  your code will have trouble when running in multi module super dev mode.

  The MagicMethod annotation is simpler; it allows you to document the fact  
  that the method is magic, and signal to the compiler that the contents of  
  that method should not be visited.  This is vital in cases where you want  
  a pure java implementation to call into code that is not supported by Gwt.

  The classes referenced inside the magic method must be Gwt compatible, but  
  the methods called by the magic method may themselves call into unsupported  
  classes, provided `@MagicMethod(doNotVisit=true)` is added.

  By default, the content of the magic method is visited, and will be considered  
  live.  This is useful in cases where you might perform AST stitching  
  (injecting) when class or string literals are supplied, but fallback to  
  original method if a class or string reference is passed instead (references  
  are opaque to the compiler, meaning you cannot know at compile time what the  
  value of the reference will be).  In such cases, your injector can return null  
  to tell the compiler to leave the JMethodCall untouched.

### GwtReflect (Reflection Support)

  Reflection support in Gwt is delivered in three formats, ranging from complete  
  emulation of Java reflection to lightweight reflection invocation that uses  
  standard reflection in JVMs and JSNI in Gwt.

  The different levels of reflection are designed to minimize the amount of  
  generated code that will be added to your application, while still offering  
  convenience and flexibility in the code you write.

  The first step in using Gwt reflection is to inherit the module which supports  
  it; `com.google.gwt.reflect.Reflect`.  This will include a class, GwtReflect, plus  
  enable a number of magic methods that provide reflection support.

#### Flyweight (Invoke-Only) Reflection Support

  In many cases, you do not need access to an actual Field or Method object, you  
  simply want to perform a reflective operation on a member that you do not have  
  standard access to.

  In such situtations, you can bypass all reflection metadata, and simply invoke  
  the member you want to access by using helper methods in the GwtReflect class.

  These methods are all magic-methods; in a standard JVM they will perform  
  normal reflection operations (including .setAccessible(true)), and in Gwt,  
  if all of the parameters required to do a compile-time lookup of the member  
  are traceable to literal values, then the compiler will emit a method call  
  that simply performs the given operation (passing along any parameters to  
  an Executable like Method or Constructor).

  In practice, this would look like this:  

    static final Class[] EQUALS_PARAMS = new Class[]{Object.class};
    GwtReflect.invoke(List.class, "equals", EQUALS_PARAMS, list1, list2);

  In the contrived example above, the Class, name and parameters were all compile  
  time constants, so this invocation will be replaced with:

    /*-{ return list1.@java.util.List::equals(Ljava/lang/Object;)(list2); }-*/
    // The actual code will auto-box the boolean to match expect JVM behavior

  In a JVM, the invoke method would just perform normal reflection:

    return List.class.getMethod("equals", EQUALS_PARAMS).invoke(list1, list2);

  This gives you all of the power to access private members interopably in Gwt  
  and standard JVM runtimes! As an added bonus, you can also (dangerously)  
  invoke a default method without an instance object; this will obviously  
  fail spectacularly if you try to invoke other instance methods on the "this"  
  parameter (which will be null), but in cases where you have default methods  
  that just return values, this can be a rather powerful tool.

  Although the method signatures for these helpers are fairly bulky, their use  
  affords you powerful, interoperable reflection functionality, without a bunch  
  of extra code / data needed to implement Method, Field or Constructor objects!

#### Mediumweight (On Demand) Reflection Support

  The medium-weight option is to only generate a Field, Method or Constructor as  
  needed. This is done by making the reflection methods on the Class object  
  magic methods. For example, if you call `MyClass.class.getField("myField");`,  
  and both MyClass.class and "myField" are compile-time literals (meaning the  
  compiler can figure out what Class and String they are), then we will inject  
  just the one field you have asked for, instead of enhancing the entire class.

  Now, the constraint here is that all of the parameters needed to determine  
  which Field, Method or Constructor you want must evaluate to compile time  
  literals; if any of them resolve to opaque references, the compiler will either  
  fail fast, or will create code that checks at runtime to see if the class  
  referenced has already been enhanced (more on this below).  

  The default behavior is to allow runtime lookups, but if you want the fail-fast  
  behavior because you never want to pay for full class enhancement, just set the  
  configuration property gwt.reflect.never.fail=false.

  Note that, in this fork, the process for looking up compile-time literals is  
  a little more relaxed than the standard GWT.create lookup rules.  In addition  
  to class / String literals, you may also use constant field references, or  
  final methods with a single return statement (thus allowing super-source  
  to override a given literal at compile time).

  Good examples:  
  * `List.class.getMethod("equals", new Class<?>[]{Object.class}});`  
  * `final static Class<List> LIST = List.class;`  
    `final static Class<?>[] params() { return new Class<?>[]{Object.class};}`  
    `LIST.getMethod("equals", params());`

  Bad examples:  
  * `myList.getClass().getMethod("size");`
  * `List.class.getMethod("equals", new Class<?>[]{myObject.getClass()});`


#### Heavyweight (Complete) Reflection Support

  The most flexible option is complete emulation, where you simply send a class  
  literal to a single magic method (GwtReflect.magicClass), and then you can use  
  any kind of reflective construct on the class. Once a class has been enhanced,  
  you will be able to use reflection on non-literal references to that class (for  
  example: `someObject.getClass().getField("someField").get(someObject)` will work.

  The price you pay for this simplicity is a fair amount of extra code added to  
  your project.  The generated Class enhancer will create Field, Method,  
  Constructor and Annotation objects for every member in that class (as well as  
  inner classes). There are levers and knobs provided to reduce this overhead,  
  which will be covered in greater detail below.

  It is only recommended to use this heavyweight option when you are using code  
  you do not control which relies on runtime reflection (ex: Spring or JUnit).
   
#### Mitigating Code Bloat from Reflection Metadata

  

## GWT (Original Documentation)

  GWT is the official open source project for GWT releases 2.5 and onwards.

  In this document you have some quick instructions to build the SDK from
  source code and to run its tests.

  For a more detailed documentation visit our [web site](http://gwtproject.org).
  If you are interested in contributing with the project, please read the
  [Making GWT better](http://gwtproject.gquery.org/makinggwtbetter.html)
  section.

### Building the GWT SDK:

 - In order to build GWT, `java` and `ant` are required in your system.

 - Optional: if you want to compile elemental you need
   `python` and `g++` installed.

 - You need the [gwt-tools](https://google-web-toolkit.googlecode.com/svn/tools/)
   checked out and up-to-date, and it will be placed
   by default at `../tools`. You can override the default
   location using the GWT_TOOLS environment variable or passing `-Dgwt.tools=`
   argument to ant.

   _Note: that you need `svn` to checkout `gwt-tools`_

 - To create the SDK distribution files run:

   `$ ant clean elemental dist-dev`

   or if you don't have `python` and `g++` just run

   `$ ant clean dist-dev`

   Then you will get all `.jar` files in the folder `build/lib` and
   the redistributable file will be: `build/dist/gwt-0.0.0.zip`

   if you want to specify a different version number run:

   `$ ant elemental clean dist-dev -Dgwt.version=x.x.x`

 - To compile everything including examples you have to run

   `$ ant clean elemental dist`

### How to verify GWT code conventions:

 - In GWT we have some conventions so as all code written
   by contributors look similar being easier to review.

 - After you make any modification, run this command to compile
   everything including tests, to check APIs, and to verify code style.
   It shouldn't take longer than 3-4 minutes.

   `$ ant compile.tests apicheck checkstyle -Dprecompile.disable=true`

### How to run GWT tests

 - Previously to run any test you have to set some environment variables
   to guarantee that they are run in the same conditions for all
   developers.

   In a _Unix_ like platform you can use the `export` command:

   `$ export TZ=America/Los_Angeles ANT_OPTS=-Dfile.encoding=UTF-8`

   But in _Windows™_ you have to set the time-zone in your control panel, and
   the environment variables using the command `set`.

 - Finally you can run all test suites with the following command, but be
   prepared because it could take hours, and probably it would fail because
   of timeouts, etc.

   `$ ant test`

 - Thus, you might want to run only certain tests so as you can
   focus on checking the modifications you are working on.

   GWT build scripts use specific ant tasks and a bunch of system
   properties listed in the following table to specify which tests
   to run and how.

   For instance to run the task `test` in the module `user` you
   have to change to the `user` folder and run `ant` with the task
   as argument, adding any other property with the `-D` flag:

   `$ ( cd user && ant test -Dtest.emma.htmlunit.disable=true ; cd .. )`

    Module         | Task                   | Property to skip
    -------------- | ---------------------- | ----------------
    dev            | test                   | test.dev.disable
    codeserver     | test                   | test.codeserver.disable
    user           | test                   | test.user.disable
    user           | test.nongwt            | test.nongwt.disable
    user           | test.dev.htmlunit      | test.dev.htmlunit.disable
    user           | test.web.htmlunit      | test.web.htmlunit.disable
    user           | test.draft.htmlunit    | test.draft.htmlunit.disable
    user           | test.nometa.htmlunit   | test.nometa.htmlunit.disable
    user           | test.emma.htmlunit     | test.emma.htmlunit.disable
    user           | test.coverage.htmlunit | test.coverage.htmlunit.disable
    user           | test.dev.selenium      | test.dev.selenium.disable
    user           | test.web.selenium      | test.web.selenium.disable
    user           | test.draft.selenium    | test.draft.selenium.disable
    user           | test.nometa.selenium   | test.nometa.selenium.disable
    user           | test.emma.selenium     | test.emma.selenium.disable
    requestfactory | test                   |
    elemental      | test                   |
    elemental      | test.nongwt            |
    elemental      | test.dev.htmlunit      |
    elemental      | test.web.htmlunit      |
    tools          | test                   |

   Additionally you can utilize some variables to filter which test to run in each task:

    Module         | Task                                  | Properties                           | Default
    ---------------|---------------------------------------|--------------------------------------|-------------------
    dev/core       | test                                  | gwt.junit.testcase.dev.core.includes | `**/com/google/**/*Test.class`
                   |                                       | gwt.junit.testcase.dev.core.excludes |
    user           | test                                  | gwt.junit.testcase.includes          | `**/*Suite.class`
    user           | test.nongwt                           | gwt.nongwt.testcase.includes         | `**/*JreSuite.class`
                   |                                       | gwt.nongwt.testcase.excludes         |
    user           | test.web.* test.draft.* test.nometa.* | gwt.junit.testcase.web.includes      | `**/*Suite.class`
                   |                                       | gwt.junit.testcase.web.excludes      | `**/*JsInteropSuite.class,**/*JreSuite.class,***/OptimizedOnly*`
    user           | test.dev.* test.emma.*                | gwt.junit.testcase.dev.includes      | `**/*Suite.class`
                   |                                       | gwt.junit.testcase.dev.excludes      | `**/*JsInteropSuite.class,**/*JreSuite.class,***/OptimizedOnly*`

### Examples

 - Run all tests in dev

   `$ ( cd dev && ant test ; cd .. )`

    _Note: that the last `cd ..' is only needed in Windows._

 - There is another option to do the same but without changing to the
   module folder. We have to specify the module as the ant task, and
   the task as a target argument.

   `$ ant dev -Dtarget=test`

 - Run all tests in codeserver

   `$ ( cd dev/codeserver && ant test )`

    or

   `$ ant codeserver -Dtarget=test -Dtest.dev.disable=true`

    _Note: that we disable dev tests because code server depends on dev
    and we don`t want to run its tests._

 - Run all tests in elemental:

   `$ ( cd elemental && ant test.nongwt )`

    or

   `$ ant elemental -Dtarget=test -Dtest.dev.disable=true -Dtest.user.disable=true`

    _Note: that we have to disable dev and user tests because elemental
    depends on both._

 - Run all tests in tools

   `$ ant tools -Dtarget=test -Dtest.dev.disable=true -Dtest.user.disable=true`

 - Run only the JsniRefTest in dev

   ```
   $ ant dev -Dtarget=test \
       -Dgwt.junit.testcase.dev.core.includes="**/JsniRefTest.class"
   ```

 - Run a couple of tests in dev

   ```
   $ ant dev -Dtarget=test \
       -Dgwt.junit.testcase.dev.core.includes="**/JsniRefTest.class,**/JsParserTest.class"
   ```

   _Note: that you have to use regular expressions separated by comma to
   select the test classes to execute._

 - Run all Jre tests in user, they should take not longer than 3min.
   We have two ways to run them. Although the second case is more
   complex it is here to know how disable properties work.

   `$ ( cd user && ant test.nongwt )`

      or

   ```
   $ ant user -Dtarget=test -Dtest.dev.disable=true \
          -Dtest.dev.htmlunit.disable=true \
          -Dtest.web.htmlunit.disable=true \
          -Dtest.coverage.htmlunit.disable=true \
          -Dtest.dev.selenium.disable=true \
          -Dtest.draft.htmlunit.disable=true \
          -Dtest.draft.selenium.disable=true \
          -Dtest.emma.htmlunit.disable=true \
          -Dtest.emma.selenium.disable=true \
          -Dtest.nometa.htmlunit.disable=true \
          -Dtest.nometa.selenium.disable=true \
          -Dtest.web.selenium.disable=true
   ```

    _Note: that we have to set all disable variables but `test.nongwt.disable`_

 - Run certain Jre tests in the user module.

   `$ ( cd user && ant test.nongwt -Dgwt.nongwt.testcase.includes="**/I18NJreSuite.class" )`

 - Run all GWT tests in user using htmlunit in dev mode.

   `$ ( cd user && ant test.dev.htmlunit )`

