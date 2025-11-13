package io.flowguard.core.trace;

import io.flowguard.core.config.FlowGuardConfig;
import io.flowguard.core.print.FlowGuardPrinter;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

/**
 * trace 구간의 시작/종료를 기록한다.
 */
public final class TraceInterceptor {

    private final FlowGuardConfig config;
    private final FlowGuardPrinter printer;
    private final AtomicBoolean running = new AtomicBoolean();
    private final AtomicInteger sampleCount = new AtomicInteger();
    private final List<String> collectedFrames = new CopyOnWriteArrayList<>();
    private Thread targetThread;
    private Thread samplerThread;
    private long startNanos;

    public TraceInterceptor(FlowGuardConfig config) {
        this.config = Objects.requireNonNull(config, "config");
        this.printer = config.printer();
    }

    public void startForCurrentThread() {
        if (running.compareAndSet(false, true)) {
            this.targetThread = Thread.currentThread();
            this.startNanos = System.nanoTime();
            this.samplerThread = new Thread(this::watchLoop, "FlowGuard-Sampler");
            this.samplerThread.setDaemon(true);
            this.samplerThread.start();
        }
    }

    public void stop() {
        if (!running.compareAndSet(true, false)) {
            return;
        }
        // 샘플러 스레드가 종료될 때까지 대기
        if (samplerThread != null) {
            try {
                samplerThread.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        long elapsedNanos = System.nanoTime() - startNanos;
        String title = "FlowGuard :: " + (targetThread != null ? targetThread.getName() : "unknown");
        List<String> lines =
                List.of(
                        "Samples collected: " + sampleCount.get(),
                        "pollInterval=" + formatDuration(config.pollInterval()),
                        "elapsed=" + formatDuration(Duration.ofNanos(elapsedNanos)));
        printer.print(title, lines);
    }

    public int getSampleCount() {
        return sampleCount.get();
    }

    public List<String> getCollectedFrames() {
        return Collections.unmodifiableList(new ArrayList<>(collectedFrames));
    }

    private void watchLoop() {
        long pollNanos = config.pollInterval().toNanos();

        while (running.get()) {
            // targetThread의 스택 프레임 샘플링
            if (targetThread != null) {
                StackTraceElement[] stackTrace = targetThread.getStackTrace();
                for (StackTraceElement element : stackTrace) {
                    String frameInfo =
                            element.getClassName()
                                    + "."
                                    + element.getMethodName()
                                    + ":"
                                    + element.getLineNumber();
                    collectedFrames.add(frameInfo);
                }
            }
            sampleCount.incrementAndGet();

            // pollInterval만큼 대기
            LockSupport.parkNanos(pollNanos);
        }
    }

    private static String formatDuration(Duration duration) {
        long millis = duration.toMillis();
        if (millis > 0) {
            return millis + "ms";
        }
        return duration.toNanos() + "ns";
    }
}
