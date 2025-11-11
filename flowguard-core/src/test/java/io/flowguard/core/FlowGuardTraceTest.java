package io.flowguard.core;

import static org.assertj.core.api.Assertions.assertThat;

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
