package com.github.noconnor.junitperf.reporting.utils;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class FormatterUtilsTest {

  @Test
  public void givenANegativeRequiredThreshold_thenNAStringShouldBeReturned() {
    assertThat(FormatterUtils.format(-1), is("N/A"));
    assertThat(FormatterUtils.format(-55), is("N/A"));
  }

  @Test
  public void givenANonNegativeRequiredThreshold_thenStringShouldBeReturned() {
    assertThat(FormatterUtils.format(1.909871f), is("1.909871"));
    assertThat(FormatterUtils.format(0.98799f), is("0.98799"));
  }

}
