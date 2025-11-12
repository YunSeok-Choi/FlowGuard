package io.flowguard.core;

import io.flowguard.core.config.FlowGuardConfig;
import io.flowguard.core.trace.TraceInterceptor;

import java.util.Objects;

/**
 * 엔트리 API. {@code FlowGuard.trace(() -> ...)} 형태로 사용한다.
 */
public final class FlowGuard {

    private FlowGuard() {
    }

    public static void trace(Runnable runnable) {
        trace(FlowGuardConfig.defaults(), runnable);
    }

    public static void trace(FlowGuardConfig config, Runnable runnable) {
        Objects.requireNonNull(runnable, "runnable");
        try (AutoCloseable ignored = autoTrace(config)) {
            runnable.run();
        } catch (RuntimeException | Error e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to stop FlowGuard tracing", e);
        }
    }

    public static AutoCloseable autoTrace() {
        return autoTrace(FlowGuardConfig.defaults());
    }

    public static AutoCloseable autoTrace(FlowGuardConfig config) {
        TraceInterceptor interceptor = new TraceInterceptor(config);
        interceptor.startForCurrentThread();
        return interceptor::stop;
    }
}
