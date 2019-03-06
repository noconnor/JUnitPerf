package com.github.noconnor.junitperf.data;

import org.junit.Before;
import org.junit.Test;

public class NoOpTestContextTest {

  private NoOpTestContext context;

  @Before
  public void setup() {
    context = new NoOpTestContext();
  }

  @Test
  public void whenCallingSuccess_thenNoExceptionsShouldBeThrown() {
    context.success();
  }

  @Test
  public void whenCallingFail_thenNoExceptionsShouldBeThrown() {
    context.fail();
  }
}
