package com.github.noconnor.junitperf.datetime;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class DatetimeUtilsTest {

  @Test
  public void whenTimeHasBeenOverridden_thenTheOverriddenTimeShouldBeReturned() {
    DatetimeUtils.setOverride("TEST");
    assertThat(DatetimeUtils.now(), is("TEST"));
  }

  @Test
  public void whenRetrievingNowTime_thenFormattedStringShouldBeReturned() {
    DatetimeUtils.setOverride(null);
    String expectedPattern = "\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}";
    assertTrue(DatetimeUtils.now().matches(expectedPattern));
  }

}
