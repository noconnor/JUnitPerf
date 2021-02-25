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

  public static String format(int durationMs) {
    long seconds = durationMs / 1000;
    long minutes = seconds / 60;
    long hours = minutes / 60;
    long days = hours / 24;
    if (days > 0){
      return days + "d:" + hours % 24 + "h:" + minutes % 60 + "m:" + seconds % 60 + "s";
    } else if (hours > 0){
      return hours % 24 + "h:" + minutes % 60 + "m:" + seconds % 60 + "s";
    } else if (minutes > 0){
      return minutes % 60 + "m:" + seconds % 60 + "s";
    } if (seconds > 0){
      return (seconds % 60) + "s";
    } else {
      return durationMs + "ms";
    }
  }
}
