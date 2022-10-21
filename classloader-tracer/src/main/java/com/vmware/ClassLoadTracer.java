package com.vmware;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Java agent which helps with debugging classloading issues by displaying where classes are loaded
 * from.
 * <p/>
 * To use it start your Java process with:
 * <p/>
 * <pre>
 * -javaagent=&lt;path to classload-tracer.jar>
 * </pre>
 *
 * By default, the output goes to stdout; by adding a 'log' option, the output can be directed to a
 * file.
 * <p/>
 * Additionally, if you want to see exactly where in your code a class load is being triggered you
 * can use the 'classPattern' option. This option takes a regular expression which matches against
 * the class names. For the classes that match, additional stack info will be printed allowing you
 * to pinpoint exact code locations which have triggered a class to be loaded.
 */
public class ClassLoadTracer {
  private static final SimpleDateFormat SDF = new SimpleDateFormat(
      "yyyy/MM/DD HH:mm:ss.S");

  public static void premain(String agentArgs, Instrumentation inst) {
    Map<String, String> args = Utils.processArgs(agentArgs);
    PrintStream out = Utils.makeLog(args.get("log"));

    String patternStr = args.get("classPattern");
    Pattern tmpPattern = null;
    if (patternStr != null) {
      tmpPattern = Pattern.compile(patternStr);
    }
    final Pattern pattern = tmpPattern;

    inst.addTransformer(
        (loader, className, classBeingRedefined, protectionDomain, classfileBuffer) -> {

          String from = loader.getResource(className.replace('.', '/') +
              ".class").toString();

          StringBuilder builder = new StringBuilder(SDF.format(new Date()));
          builder.append(" - [classloader=").append(loader.toString());
          builder.append(", thread=").append(Thread.currentThread());
          builder.append("] loaded ").append(className);
          builder.append(" from ").append(from);

          out.println(builder);

          if (pattern != null) {
            Matcher m = pattern.matcher(className);
            if (m.find()) {
              StackTraceElement[] frames = Thread.currentThread().getStackTrace();
              // Try and skip a bunch of standard stacks...
              int idx = frames.length - 1;
              for (; idx > 0; idx--) {
                String c = frames[idx].getClassName();
                if (c.startsWith("java.lang.ClassLoader") ||
                    c.startsWith("sun.misc.Launcher") ||
                    c.startsWith("java.net.URLClassLoader") ||
                    c.startsWith("java.security.SecureClassLoader")) {
                  break;
                }
              }

              if (idx > 0) {
                out.println("    <...>");
              }

              for (; idx < frames.length; idx++) {
                StringBuilder line = new StringBuilder();
                line.append("    ");
                line.append(frames[idx].getClassName()).append(".");
                line.append(frames[idx].getMethodName()).append("(");
                line.append(frames[idx].getFileName()).append(":");
                line.append(frames[idx].getLineNumber()).append(")");
                out.println(line);
              }
            }
          }

          // we just want the original .class bytes to be loaded!
          return null;
        });
  }

}
