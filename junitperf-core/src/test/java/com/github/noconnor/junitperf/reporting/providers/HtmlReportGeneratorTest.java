package com.github.noconnor.junitperf.reporting.providers;

import com.github.noconnor.junitperf.datetime.DatetimeUtils;
import com.github.noconnor.junitperf.reporting.BaseReportGeneratorTest;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import static java.lang.System.getProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class HtmlReportGeneratorTest extends BaseReportGeneratorTest {

    private HtmlReportGenerator reportGenerator;

    @Before
    public void setup() throws IOException {
        reportFile = folder.newFile("report.html");
        reportGenerator = new HtmlReportGenerator(reportFile.getPath());
        DatetimeUtils.setOverride("unittest o'clock");
        initialisePerfTestAnnotationMock();
        initialisePerfTestRequirementAnnotationMock();
    }

    @Test
    public void whenCallingDefaultConstructor_thenNoExceptionShouldBeThrown() throws IOException {
        reportGenerator = new HtmlReportGenerator();
    }

    @Test(expected = IllegalStateException.class)
    public void whenGeneratingAReport_andPathIsNotWritable_thenExceptionShouldBeThrown() throws IOException {
        reportGenerator = new HtmlReportGenerator("///foo");
        reportGenerator.generateReport(generateAllFailureOrderedContexts());
    }

    @Test
    public void whenGeneratingAReport_andAllTestsFailed_thenAppropriateReportShouldBeGenerated() throws IOException {
        reportGenerator.generateReport(generateAllFailureOrderedContexts());
        File expectedContents = getResourceFile("html/example_all_failed_report.html");
        assertEquals(readFileContents(expectedContents), readFileContents(reportFile));
    }

    @Test
    public void whenGeneratingAReport_andAllTestsPass_thenAppropriateReportShouldBeGenerated() throws IOException {
        reportGenerator.generateReport(generateAllPassedOrderedContexts());
        File expectedContents = getResourceFile("html/example_all_passed_report.html");
        assertEquals(readFileContents(expectedContents), readFileContents(reportFile));
    }

    @Test
    public void whenGeneratingAReport_andTestsContainsAMixOfPassAndFailures_thenAppropriateReportShouldBeGenerated() throws IOException {
        reportGenerator.generateReport(generateMixedOrderedContexts());
        File expectedContents = getResourceFile("html/example_mixed_report.html");
        assertEquals(readFileContents(expectedContents), readFileContents(reportFile));
    }

    @Test
    public void whenGeneratingAReport_andTestsContainsSomeFailures_thenAppropriateReportShouldBeGenerated() throws IOException {
        reportGenerator.generateReport(generateSomeFailuresContext());
        File expectedContents = getResourceFile("html/example_some_failures_report.html");
        assertEquals(readFileContents(expectedContents), readFileContents(reportFile));
    }

    @Test
    public void whenCallingGetReportPath_andCustomPathHasBeenSpecified_thenCorrectPathShouldBeReturned() {
        assertThat(reportGenerator.getReportPath(), is(reportFile.getPath()));
    }

    @Test
    public void whenCallingGetReportPath_andDefaultPathHasBeenSpecified_thenCorrectPathShouldBeReturned() {
        reportGenerator = new HtmlReportGenerator();
        String expected = String.join(
                File.separator,
                getProperty("user.dir") ,
                "build",
                "reports",
                "junitperf_report.html"
        );
        assertEquals(expected, reportGenerator.getReportPath());
    }

    @Test
    public void whenHtmlProcessorProcessBlocksIsCalled_thenTheCorrectBlocksShouldBeProcessed() {
        Map<String, String> blocks = HtmlReportGenerator.HtmlTemplateProcessor.parseTemplateBlocks();
        assertEquals(4, blocks.size());
        assertTrue(blocks.containsKey("root"));
        assertTrue(blocks.containsKey("{% OVERVIEW_BLOCK %}"));
        assertTrue(blocks.containsKey("{% DETAILED_BLOCK %}"));
        assertTrue(blocks.containsKey("{% PERCENTILES_BLOCK %}"));

        assertEquals(919, blocks.get("root").length());
        assertEquals(296, blocks.get("{% OVERVIEW_BLOCK %}").length());
        assertEquals(7883, blocks.get("{% DETAILED_BLOCK %}").length());
        assertEquals(704, blocks.get("{% PERCENTILES_BLOCK %}").length());
    }
}
