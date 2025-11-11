package io.flowguard.core.config;

import io.flowguard.core.print.FlowGuardPrinter;
import io.flowguard.core.print.FlowGuardPrinters;
import java.time.Duration;
import java.util.Objects;

/**
 * 흐름 추적에 필요한 설정을 담는 단순 불변 객체.
 * 샘플링 간격과 출력 전략을 제어한다.
 */
public final class FlowGuardConfig {

    public static final Duration DEFAULT_POLL_INTERVAL = Duration.ofMillis(2);

    private final Duration pollInterval;
    private final FlowGuardPrinter printer;

    private FlowGuardConfig(Duration pollInterval, FlowGuardPrinter printer) {
        this.pollInterval = pollInterval;
        this.printer = printer;
    }

    public static FlowGuardConfig defaults() {
        return new FlowGuardConfig(DEFAULT_POLL_INTERVAL, FlowGuardPrinters.loadDefault());
    }

    public static FlowGuardConfig of(Duration pollInterval, FlowGuardPrinter printer) {
        if (pollInterval == null || pollInterval.isZero() || pollInterval.isNegative()) {
            throw new IllegalArgumentException("pollInterval must be > 0");
        }
        return new FlowGuardConfig(
                pollInterval, Objects.requireNonNull(printer, "printer"));
    }

    public FlowGuardConfig withPollInterval(Duration pollInterval) {
        return of(pollInterval, printer);
    }

    public FlowGuardConfig withPrinter(FlowGuardPrinter printer) {
        return of(pollInterval, printer);
    }

    public Duration pollInterval() {
        return pollInterval;
    }

    public FlowGuardPrinter printer() {
        return printer;
    }
}
