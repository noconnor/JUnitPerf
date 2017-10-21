package com.noconnor.junitperf;

import org.junit.Rule;
import org.junit.Test;

public class JunitPerfRuleTest extends BaseTest {

  @Rule
  public JunitPerfRule perfRule = new JunitPerfRule();

  @Test
  public void whenJunitPerfRuleIsPresent_thenTestCasesShouldBeExecutedMultipleTimes() {
    System.out.println("Hello World!");
  }

}
