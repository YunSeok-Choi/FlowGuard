package io.flowguard.core.trace;

import io.flowguard.core.config.FlowGuardConfig;
import io.flowguard.core.print.FlowGuardPrinter;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * trace 구간의 시작/종료를 기록한다.
 */
public final class TraceInterceptor {

    private final FlowGuardConfig config;
    private final FlowGuardPrinter printer;
    private final AtomicBoolean running = new AtomicBoolean();
    private Thread targetThread;
    private long startNanos;

    public TraceInterceptor(FlowGuardConfig config) {
        this.config = Objects.requireNonNull(config, "config");
        this.printer = config.printer();
    }

    public void startForCurrentThread() {
        if (running.compareAndSet(false, true)) {
            this.targetThread = Thread.currentThread();
            this.startNanos = System.nanoTime();
        }
    }

    public void stop() {
        if (!running.compareAndSet(true, false)) {
            return;
        }
        long elapsedNanos = System.nanoTime() - startNanos;
        String title = "FlowGuard :: " + (targetThread != null ? targetThread.getName() : "unknown");
        List<String> lines = List.of(
                "MVP trace stub (실제 StackWalker 연동 전)",
                "pollInterval=" + formatDuration(config.pollInterval()),
                "elapsed=" + formatDuration(Duration.ofNanos(elapsedNanos)));
        printer.print(title, lines);
    }

    private static String formatDuration(Duration duration) {
        long millis = duration.toMillis();
        if (millis > 0) {
            return millis + "ms";
        }
        return duration.toNanos() + "ns";
    }
}
