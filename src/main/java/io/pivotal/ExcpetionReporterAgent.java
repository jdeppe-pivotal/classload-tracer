package io.pivotal;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.security.ProtectionDomain;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtBehavior;
import javassist.CtClass;

public class ExcpetionReporterAgent {

  public static void premain(final String agentArgs, final Instrumentation inst) {
    inst.addTransformer(new ExceptionReporterTransformer(), true);

    for (Class clazz : inst.getAllLoadedClasses()) {
      try {
        System.out.println("--->>> " + clazz.getName());
        inst.retransformClasses(clazz);
      } catch (UnmodifiableClassException e) {
        System.out.println("Cannot retransform: " + clazz.getName());
      }
    }
  }

  public static class ExceptionReporterTransformer implements ClassFileTransformer {

    private CtClass exceptionCtClass;

    public byte[] transform(ClassLoader loader, String className, Class redefiningClass,
                            ProtectionDomain domain, byte[] bytes) {

      System.out.println("+++>>> " + className);
      return transformClass(redefiningClass, bytes);
    }

    private byte[] transformClass(Class classToTransform, byte[] b) {
      ClassPool pool = ClassPool.getDefault();
      CtClass cl = null;
      try {
        cl = pool.makeClass(new java.io.ByteArrayInputStream(b));
        if (cl.getName().contains("ClassLoader")) {
          exceptionCtClass = cl; //Or any exception, you can move this logic into constructor.
//        } else {
          modifyClass(cl);
        }

        b = cl.toBytecode();

      } catch (Exception e) {
        e.printStackTrace();
      } finally {
        if (cl != null) {
          cl.detach();
        }
      }

      return b;
    }

    private byte[] modifyClass(CtClass cl) throws Exception {
      CtBehavior[] methods = cl.getDeclaredBehaviors();
      for (CtBehavior method : methods) {
        if (method.getMethodInfo().getName().equals("findClass")) {
          changeMethod(method);
        }
      }
      return cl.toBytecode();
    }

    private void changeMethod(CtBehavior method) throws CannotCompileException {
//      method.addLocalVariable("$_start", CtClass.longType);
//      method.addLocalVariable("$_end", CtClass.longType);
//      method.addLocalVariable("$_total", CtClass.longType);
//      method.insertBefore("{ $_start = System.currentTimeMillis(); }");
      method.insertAfter("{ if ($_ == null) { System.out.println($0 + \" After " + exceptionCtClass.getName() + " \" + $1); } }", true);

      //For methods returning String
      // method.insertAfter("{ $_end = System.currentTimeMillis();\n$_total = $_end - $_start;\nSystem.out.println(\"Total: \" + $_total);return \"AAA\"; }");

//      method.addCatch(
//          "{ System.out.println(\"Caught exception\");\n$_e.printStackTrace();\nthrow $_e; }",
//          exceptionCtClass, "$_e");

      //For methods returning String
      // method.insertAfter("{ System.out.println(\"Finally\");\nreturn \"ABC\"; }", true);

//      method.insertAfter("{ System.out.println(\"After " + exceptionCtClass.getName() + "\"); }");

      System.out.println("Modifying method: " + method.getName());
    }
  }
}
