package com.noconnor.junitperf;

import org.junit.Ignore;
import org.mockito.MockitoAnnotations;

@Ignore
public class BaseTest {

  public BaseTest() {
    MockitoAnnotations.initMocks(this);
  }

}
