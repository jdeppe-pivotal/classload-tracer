package io.pivotal;

import com.ea.agentloader.AgentLoader;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestMyAgent {

//  @BeforeClass
//  public static void setup() {
//    AgentLoader.loadAgentClass(ExcpetionReporterAgent.class.getName(), null, null, true, true, false);
//  }

  @Test
  public void insanity() {

  }

  public static void main(String[] args) throws Exception {
    TestMyAgent.class.getClassLoader().loadClass("unknown.Clazz");
  }

}
