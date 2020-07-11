package com.github.noconnor.junitperf.datetime;

import lombok.Setter;
import lombok.experimental.UtilityClass;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static java.util.Objects.nonNull;

@UtilityClass
public class DatetimeUtils {

  @Setter
  private static String override;

  public static String now() {
    if (nonNull(override)) {
      return override;
    }
    return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
  }

}
