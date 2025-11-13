package io.flowguard.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.flowguard.core.config.FlowGuardConfig;
import io.flowguard.core.print.FlowGuardPrinter;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FlowGuardTraceTest {

    @Test
    @DisplayName("trace()는 Runnable을 실행하고 종료 시 Printer를 한 번 호출한다")
    void traceRunsRunnableAndPrints() {
        CapturingPrinter printer = new CapturingPrinter();
        FlowGuardConfig config = FlowGuardConfig.of(Duration.ofMillis(5), printer);
        AtomicInteger counter = new AtomicInteger();

        FlowGuard.trace(config, counter::incrementAndGet);

        assertThat(counter).hasValue(1);
        assertThat(printer.printCount).isEqualTo(1);
        assertThat(printer.lastTitle).contains("FlowGuard ::");
        assertThat(printer.lastLines).isNotEmpty();
    }

    @Test
    @DisplayName("autoTrace() 핸들은 close() 시 Printer를 호출하고 중복 호출을 방지한다")
    void autoTraceHandleStopsOnce() throws Exception {
        CapturingPrinter printer = new CapturingPrinter();
        FlowGuardConfig config = FlowGuardConfig.of(Duration.ofMillis(1), printer);

        AutoCloseable handle = FlowGuard.autoTrace(config);
        assertThat(printer.printCount).isEqualTo(0);

        handle.close();

        assertThat(printer.printCount).isEqualTo(1);
        assertThat(printer.lastTitle).contains("FlowGuard ::");
    }

    @Test
    @DisplayName("trace(Runnable)은 기본 config로 Runnable을 실행한다")
    void traceWithDefaultConfig() {
        AtomicInteger counter = new AtomicInteger();

        FlowGuard.trace(counter::incrementAndGet);

        assertThat(counter).hasValue(1);
    }

    @Test
    @DisplayName("autoTrace()는 기본 config로 핸들을 반환한다")
    void autoTraceWithDefaultConfig() throws Exception {
        AutoCloseable handle = FlowGuard.autoTrace();

        assertThat(handle).isNotNull();

        handle.close();
    }

    @Test
    @DisplayName("trace()는 Runnable 내부의 RuntimeException을 전파한다")
    void tracePropagatessRuntimeException() {
        CapturingPrinter printer = new CapturingPrinter();
        FlowGuardConfig config = FlowGuardConfig.of(Duration.ofMillis(1), printer);

        assertThrows(
                IllegalStateException.class,
                () ->
                        FlowGuard.trace(
                                config,
                                () -> {
                                    throw new IllegalStateException("test exception");
                                }));

        assertThat(printer.printCount).isEqualTo(1);
    }

    @Test
    @DisplayName("trace()는 null Runnable을 거부한다")
    void traceRejectsNullRunnable() {
        FlowGuardConfig config = FlowGuardConfig.defaults();

        assertThrows(NullPointerException.class, () -> FlowGuard.trace(config, null));
    }

    private static final class CapturingPrinter implements FlowGuardPrinter {

        private int printCount;
        private String lastTitle;
        private List<String> lastLines = new ArrayList<>();

        @Override
        public String id() {
            return "capture";
        }

        @Override
        public void print(CharSequence title, List<String> lines) {
            this.printCount++;
            this.lastTitle = title.toString();
            this.lastLines = new ArrayList<>(lines);
        }
    }
}
