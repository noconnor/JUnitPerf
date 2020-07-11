package com.github.noconnor.junitperf.examples.utils;

import com.github.noconnor.junitperf.reporting.providers.HtmlReportGenerator;

import static java.lang.System.getProperty;

public class ReportingUtils {

  public static HtmlReportGenerator newHtmlReporter(String fileName){
    return new HtmlReportGenerator(getProperty("user.dir") + "/build/reports/" + fileName);
  }

}
