Classloader Tracing Tool
------------------------

This tool assists with discovering where classes get loaded from. It was derived from the following post: https://blogs.oracle.com/sundararajan/entry/tracing_class_loading_1_5

Simply enable it with:

    -javaagent:<path>/classload-tracer.jar=log=classloading.log,classPattern=.*

Both `log` and `classPattern` are optional. Without the `log` option, output will be sent to stdout.

The `classPattern` option is a regular expression which, when matched against the classname being loaded, will print a stack trace for the call which triggered the request.

The output of the tool looks something like this:

    2014/03/70 15:44:49.923 - sun.misc.Launcher$AppClassLoader@1e1ff563 [loader=1e1ff563, thread=Thread[main,5,main]] loaded com/gemstone/gemfire/InternalGemFireException from jar:file:/Users/jdeppe/gemfire/70/lib/gemfire.jar!/com/gemstone/gemfire/InternalGemFireException.class
        <...>
        java.lang.ClassLoader.loadClass(ClassLoader.java:357)
        com.gemstone.gemfire.i18n.StringId.getBundle(StringId.java:77)
        com.gemstone.gemfire.i18n.StringId.setLocale(StringId.java:63)
        com.gemstone.gemfire.i18n.StringId.<clinit>(StringId.java:46)
        com.gemstone.gemfire.internal.i18n.ParentLocalizedStrings.<clinit>(ParentLocalizedStrings.java:21)
        com.gemstone.gemfire.distributed.LocatorLauncher.<clinit>(LocatorLauncher.java:76)


Building
--------

Simply do:

    mvn clean package

This produces two artifacts in `target/`. Make sure to use the `-with-dependencies.jar` as it contains the correct manifest entries required to run.
