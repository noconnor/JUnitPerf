package com.github.noconnor.junitperf.examples;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.github.noconnor.junitperf.JUnitPerfInterceptor;
import com.github.noconnor.junitperf.JUnitPerfTest;
import com.github.noconnor.junitperf.TestContextSupplier;
import com.github.noconnor.junitperf.data.TestContext;

@ExtendWith(JUnitPerfInterceptor.class)
public class ExampleParameterizedTests {

    static List<String> hostnames() {
        return Arrays.asList("www.google.com", "www.example.com");
    }

    @MethodSource("hostnames")
    @ParameterizedTest(name = "test1(hostname = {0})")
    @JUnitPerfTest(durationMs = 3_000, maxExecutionsPerSecond = 1)
    public void test1(String hostname) throws IOException {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(hostname, 80), 1000);
        }
    }

    @MethodSource("hostnames")
    @ParameterizedTest(name = "test2(hostname = {0})")
    @JUnitPerfTest(durationMs = 3_000, maxExecutionsPerSecond = 1)
    public void test2(String hostname, TestContextSupplier supplier) {
    	TestContext context = supplier.startMeasurement();
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(hostname, 80), 1000);
            context.success();
        } catch (IOException e) {
            context.fail();
        }
    }

}
