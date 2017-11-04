package com.github.noconnor.junitperf.examples;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import org.junit.Rule;
import org.junit.Test;
import com.github.noconnor.junitperf.JUnitPerfRule;
import com.github.noconnor.junitperf.JUnitPerfTest;

import static org.junit.Assert.assertTrue;

public class ExampleSuccessTests {

  @Rule
  public JUnitPerfRule perfRule = new JUnitPerfRule();

  @Test
  @JUnitPerfTest(threads = 10, durationMs = 10_000, warmUpMs = 1_000, maxExecutionsPerSecond = 100)
  public void whenNoRequirementsArePresent_thenTestShouldAlwaysPass() throws IOException {
    try (Socket socket = new Socket()) {
      socket.connect(new InetSocketAddress("www.google.com", 80), 1000);
      assertTrue(socket.isConnected());
    }
  }
}
