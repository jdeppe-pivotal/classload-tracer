package com.vmware;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class Utils {

  public static PrintStream LOGFILE = System.err;
  private static ThreadLocal<AtomicInteger> indent = ThreadLocal.withInitial(() -> new AtomicInteger(0));

  public static Integer getIndent() {
    return indent.get().getAndIncrement();
  }

  public static void putIndent() {
    indent.get().decrementAndGet();
  }

  public static Map<String, String> processArgs(String argString) {
    Map<String, String> args = new HashMap<String, String>();

    if (argString != null) {
      String[] splits = argString.split(",");
      for (String s : splits) {
        String[] pair = s.split("=");
        args.put(pair[0], pair[1]);
      }
    }

    return args;
  }

  public static PrintStream makeLog(String filename) {
    if (filename != null) {
      try {
        return new PrintStream(new FileOutputStream(filename));
      } catch (FileNotFoundException e) {
        System.err.println(" Exception creating log file " + filename + " - " + e.getMessage());
      }
    }

    return System.err;
  }

}
