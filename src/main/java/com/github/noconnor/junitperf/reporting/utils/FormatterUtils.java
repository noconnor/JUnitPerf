package com.github.noconnor.junitperf.reporting.utils;

public class FormatterUtils {

  public static String format(float latency){
    return latency < 0 ? "N/A" : Float.toString(latency);
  }

}
