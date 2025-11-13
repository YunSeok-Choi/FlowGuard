package io.flowguard.core.trace;

import static org.assertj.core.api.Assertions.assertThat;

import io.flowguard.core.config.FlowGuardConfig;
import io.flowguard.core.print.FlowGuardPrinter;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("TraceInterceptor 샘플링 루프 테스트")
class TraceInterceptorTest {

    @Test
    @DisplayName("startForCurrentThread()는 별도 스레드에서 샘플링 루프를 시작한다")
    void startsSamplingLoopInSeparateThread() throws Exception {
        FlowGuardConfig config =
                FlowGuardConfig.of(
                        Duration.ofMillis(10),
                        new FlowGuardPrinter() {
                            @Override
                            public String id() {
                                return "test";
                            }

                            @Override
                            public void print(CharSequence title, List<String> lines) {
                            }
                        });
        TraceInterceptor interceptor = new TraceInterceptor(config);

        interceptor.startForCurrentThread();

        // 샘플링이 발생할 시간을 준다 (최소 3번 샘플링 가능한 시간)
        Thread.sleep(50);

        interceptor.stop();

        // 샘플링이 최소 1회 이상 발생했는지 확인
        assertThat(interceptor.getSampleCount()).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("샘플링은 pollInterval 간격으로 발생한다")
    void samplingOccursAtPollInterval() throws Exception {
        FlowGuardConfig config =
                FlowGuardConfig.of(
                        Duration.ofMillis(10),
                        new FlowGuardPrinter() {
                            @Override
                            public String id() {
                                return "test";
                            }

                            @Override
                            public void print(CharSequence title, List<String> lines) {
                            }
                        });
        TraceInterceptor interceptor = new TraceInterceptor(config);

        interceptor.startForCurrentThread();

        // 10ms 간격으로 샘플링하므로, 50ms면 약 4-5회 샘플링 예상
        Thread.sleep(50);

        interceptor.stop();

        int count = interceptor.getSampleCount();
        // 타이밍 오차를 고려하여 3-7회 사이면 통과
        assertThat(count).isBetween(3, 7);
    }

    @Test
    @DisplayName("stop()은 샘플링 루프를 중단한다")
    void stopHaltsSamplingLoop() throws Exception {
        FlowGuardConfig config =
                FlowGuardConfig.of(
                        Duration.ofMillis(10),
                        new FlowGuardPrinter() {
                            @Override
                            public String id() {
                                return "test";
                            }

                            @Override
                            public void print(CharSequence title, List<String> lines) {
                            }
                        });
        TraceInterceptor interceptor = new TraceInterceptor(config);

        interceptor.startForCurrentThread();
        Thread.sleep(30);

        int countBeforeStop = interceptor.getSampleCount();
        interceptor.stop();

        // stop 후 추가 샘플링이 발생하지 않음을 확인
        Thread.sleep(30);
        int countAfterStop = interceptor.getSampleCount();

        assertThat(countAfterStop).isEqualTo(countBeforeStop);
    }

    @Test
    @DisplayName("StackWalker로 수집한 프레임 정보를 반환한다")
    void collectsStackFrames() throws Exception {
        FlowGuardConfig config =
                FlowGuardConfig.of(
                        Duration.ofMillis(10),
                        new FlowGuardPrinter() {
                            @Override
                            public String id() {
                                return "test";
                            }

                            @Override
                            public void print(CharSequence title, List<String> lines) {
                            }
                        });
        TraceInterceptor interceptor = new TraceInterceptor(config);

        interceptor.startForCurrentThread();

        // 여러 메서드 호출을 통해 스택 프레임 생성
        helperMethodA();

        Thread.sleep(30);
        interceptor.stop();

        // 수집된 프레임 정보 확인
        List<String> collectedFrames = interceptor.getCollectedFrames();
        assertThat(collectedFrames).isNotEmpty();
        // 이 테스트 메서드 이름이 프레임에 포함되어 있는지 확인
        assertThat(collectedFrames).anyMatch(frame -> frame.contains("collectsStackFrames"));
    }

    private void helperMethodA() {
        helperMethodB();
    }

    private void helperMethodB() {
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
