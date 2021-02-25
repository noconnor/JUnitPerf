package com.github.noconnor.junitperf.datetime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class DatetimeUtilsTest {

  @Test
  public void whenTimeHasBeenOverridden_thenTheOverriddenTimeShouldBeReturned() {
    DatetimeUtils.setOverride("TEST");
    assertEquals("TEST", DatetimeUtils.now());
  }

  @Test
  public void whenRetrievingNowTime_thenFormattedStringShouldBeReturned() {
    DatetimeUtils.setOverride(null);
    String expectedPattern = "\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}";
    assertTrue(DatetimeUtils.now().matches(expectedPattern));
  }

  @Test
  public void whenDurationIsValid_thenDurationShouldBeFormatted() {
    assertEquals("1d:3h:46m:40s", DatetimeUtils.format(100_000_000));
    assertEquals("2h:46m:40s", DatetimeUtils.format(10_000_000));
    assertEquals("16m:40s", DatetimeUtils.format(1_000_000));
    assertEquals("59s", DatetimeUtils.format(59_000));
    assertEquals("300ms", DatetimeUtils.format(300));
  }

}
