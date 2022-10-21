package com.vmware;

import java.util.concurrent.atomic.AtomicInteger;

public class Utils {
  private static ThreadLocal<AtomicInteger> indent = ThreadLocal.withInitial(() -> new AtomicInteger(0));

  public static Integer getIndent() {
    return indent.get().getAndIncrement();
  }

  public static void putIndent() {
    indent.get().decrementAndGet();
  }
}
