package io.flowguard.examples;

import io.flowguard.core.FlowGuard;
import io.flowguard.core.config.FlowGuardConfig;
import java.time.Duration;

/**
 * FlowGuard 기본 사용 예제
 */
public class QuickStartExample {

    public static void main(String[] args) {
        System.out.println("=== FlowGuard Quick Start Example ===\n");

        // 예제 1: 기본 사용법
        example1Basic();

        System.out.println("\n");

        // 예제 2: 커스텀 설정
        example2CustomConfig();

        System.out.println("\n");

        // 예제 3: Try-with-resources 패턴
        example3AutoTrace();
    }

    static void example1Basic() {
        System.out.println("Example 1: Basic Usage");
        System.out.println("----------------------");

        FlowGuard.trace(() -> {
            businessLogic();
        });
    }

    static void example2CustomConfig() {
        System.out.println("Example 2: Custom Configuration");
        System.out.println("--------------------------------");

        FlowGuardConfig config = FlowGuardConfig.defaults()
            .withPollInterval(Duration.ofMillis(5));

        FlowGuard.trace(config, () -> {
            complexCalculation();
        });
    }

    static void example3AutoTrace() {
        System.out.println("Example 3: AutoTrace Pattern");
        System.out.println("-----------------------------");

        try (AutoCloseable trace = FlowGuard.autoTrace()) {
            recursiveExample(5);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void businessLogic() {
        step1();
        step2();
        step3();
    }

    static void step1() {
        sleep(10);
        System.out.println("  [Step 1] Processing data...");
    }

    static void step2() {
        sleep(15);
        System.out.println("  [Step 2] Validating results...");
    }

    static void step3() {
        sleep(20);
        System.out.println("  [Step 3] Saving to database...");
    }

    static void complexCalculation() {
        System.out.println("  [Calculation] Starting...");
        int result = 0;
        for (int i = 0; i < 100000; i++) {
            result += fibonacci(10);
        }
        System.out.println("  [Calculation] Result: " + result);
    }

    static int fibonacci(int n) {
        if (n <= 1) return n;
        return fibonacci(n - 1) + fibonacci(n - 2);
    }

    static void recursiveExample(int depth) {
        System.out.println("  [Recursive] Depth: " + depth);
        if (depth > 0) {
            sleep(5);
            recursiveExample(depth - 1);
        }
    }

    static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}