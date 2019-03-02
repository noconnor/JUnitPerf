package com.github.noconnor.junitperf.data;

public class NoOpTestContext extends TestContext {

  public static final NoOpTestContext INSTANCE = new NoOpTestContext();

  public NoOpTestContext() {
    super(null);
  }

  @Override
  public void success() {
    // Do nothing
  }

  @Override
  public void fail() {
    // Do nothing
  }
}
