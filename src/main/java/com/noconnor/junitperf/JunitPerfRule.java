package com.noconnor.junitperf;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class JunitPerfRule implements TestRule {

  @Override
  public Statement apply(Statement base, Description description) {
    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        for(int i = 0; i < 10; i++) {
          base.evaluate();
        }
      }
    };
  }
}
