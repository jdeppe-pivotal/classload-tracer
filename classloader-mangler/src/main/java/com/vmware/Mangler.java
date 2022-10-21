package com.vmware;

import java.io.File;
import java.io.PrintStream;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.Collections;
import java.util.Map;
import java.util.jar.JarFile;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.matcher.ElementMatchers;

public class Mangler {
  public static void premain(String arg, Instrumentation inst) throws Exception {
    Map<String, String> args = Utils.processArgs(arg);
    Utils.LOGFILE = Utils.makeLog(args.get("log"));

    install(arg, inst);
  }

  private static void install(String arg, Instrumentation inst) throws Exception {

    File temp = Files.createTempDirectory("tmp").toFile();

    String jarFile = System.getProperty("bootstrap.jar");
    if (jarFile != null) {
      inst.appendToBootstrapClassLoaderSearch(new JarFile(jarFile));
    }

    new AgentBuilder.Default()
        .with(new AgentBuilder.Listener.StreamWriting(Utils.LOGFILE).withTransformationsOnly())
        .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
        .with(AgentBuilder.InitializationStrategy.NoOp.INSTANCE)
        .with(new AgentBuilder.InjectionStrategy.UsingInstrumentation(inst, temp))
        .ignore(ElementMatchers.nameStartsWith("net.bytebuddy."))
        .type(ElementMatchers.isSubTypeOf(ClassLoader.class)
            .or(ElementMatchers.nameContainsIgnoreCase("classloader")))
        .transform((builder, type, classLoader, module, protectionDomain) -> builder
            .method(ElementMatchers.named("loadClass")
                .or(ElementMatchers.named("findClass")))
            .intercept(Advice.to(LoadClassInterceptor.class)))
        .installOn(inst);
  }

  public static class LoadClassInterceptor {

    @Advice.OnMethodEnter
    public static void enter(
        @Advice.AllArguments Object[] args,
        @Advice.Origin Method method,
        @Advice.Origin Class<?> clazz
    ) throws Exception {
      int n = Utils.getIndent();
      String indent = String.join("", Collections.nCopies(n, "  "));

      Utils.LOGFILE.format("%s--->>> %s [0x%08x] %s.%s  %s\n", indent,
          Thread.currentThread().getName(),
          System.identityHashCode(clazz),
          clazz.getName(),
          method.getName(),
          args[0]);
    }

    @Advice.OnMethodExit(onThrowable = Throwable.class)
    public static void exit(
        @Advice.Origin Method method,
        @Advice.Origin Class<?> clazz,
        @Advice.Thrown Throwable exception) {

      Utils.putIndent();
      if (exception instanceof NoClassDefFoundError) {
        System.out.println("--->>> EXCEPTION: " + exception + " in " +
            clazz.getSimpleName() + "." + method.getName());
        exception.printStackTrace();
      }
    }
  }

}
