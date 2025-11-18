package io.flowguard.examples;

import io.flowguard.core.FlowGuard;
import io.flowguard.core.config.FlowGuardConfig;
import io.flowguard.core.trace.TraceInterceptor;
import java.time.Duration;

/**
 * 실제 코드 실행 중 FlowGuard가 수집하는 데이터 확인 예제
 */
public class RealTraceExample {

    public static void main(String[] args) {
        System.out.println("=== Real Trace Example ===\n");
        System.out.println("이 예제는 실제 실행 중인 코드의 호출 스택을 추적합니다.\n");

        // 예제 1: TraceInterceptor가 수집하는 원시 데이터 확인
        example1RawData();

        System.out.println("\n" + "=".repeat(60) + "\n");

        // 예제 2: 실제 비즈니스 로직 추적
        example2BusinessLogic();
    }

    /**
     * TraceInterceptor가 실제로 수집하는 스택 프레임 데이터 확인
     */
    static void example1RawData() {
        System.out.println("Example 1: 원시 스택 프레임 데이터 확인");
        System.out.println("-".repeat(60));

        // TraceInterceptor를 직접 생성하여 수집된 프레임 확인
        FlowGuardConfig config = FlowGuardConfig.defaults()
            .withPollInterval(Duration.ofMillis(10)); // 천천히 샘플링

        TraceInterceptor interceptor = new TraceInterceptor(config);

        System.out.println("추적 시작...\n");
        interceptor.startForCurrentThread();

        // 추적할 코드 실행
        methodA();

        interceptor.stop();

        // 수집된 프레임 출력
        System.out.println("\n수집된 스택 프레임 샘플:");
        System.out.println("-".repeat(60));
        var frames = interceptor.getCollectedFrames();
        System.out.println("총 " + frames.size() + "개 프레임 수집됨");

        if (!frames.isEmpty()) {
            System.out.println("\n처음 수집된 스택 (최신 -> 최하위):");
            // 첫 번째 샘플만 출력 (보기 쉽게)
            int count = 0;
            for (String frame : frames) {
                System.out.println("  " + frame);
                count++;
                if (count >= 15) { // 상위 15개만 출력
                    System.out.println("  ... (생략) ...");
                    break;
                }
            }
        }
    }

    /**
     * 실제 비즈니스 로직을 추적하고 결과 확인
     */
    static void example2BusinessLogic() {
        System.out.println("Example 2: 실제 비즈니스 로직 추적");
        System.out.println("-".repeat(60));

        System.out.println("시나리오: 사용자 주문 처리 플로우\n");

        FlowGuard.trace(() -> {
            System.out.println("→ 주문 처리 시작");
            processOrder("ORDER-12345");
            System.out.println("→ 주문 처리 완료");
        });

        System.out.println("\n위 출력에서 FlowGuard가 자동으로 수집한 통계:");
        System.out.println("- Samples collected: 샘플링된 횟수");
        System.out.println("- pollInterval: 샘플링 간격");
        System.out.println("- elapsed: 총 실행 시간");
    }

    // === 추적 대상 메서드들 ===

    static void methodA() {
        sleep(20);
        methodB();
    }

    static void methodB() {
        sleep(20);
        methodC();
    }

    static void methodC() {
        sleep(20);
        System.out.println("  methodC 실행 중...");
    }

    // 비즈니스 로직 시뮬레이션

    static void processOrder(String orderId) {
        validateOrder(orderId);
        calculatePrice(orderId);
        saveToDatabase(orderId);
        sendNotification(orderId);
    }

    static void validateOrder(String orderId) {
        sleep(15);
        System.out.println("  [1] 주문 검증: " + orderId);
        checkInventory();
    }

    static void checkInventory() {
        sleep(10);
        System.out.println("  [1-1] 재고 확인");
    }

    static void calculatePrice(String orderId) {
        sleep(20);
        System.out.println("  [2] 가격 계산: " + orderId);
        applyDiscount();
    }

    static void applyDiscount() {
        sleep(10);
        System.out.println("  [2-1] 할인 적용");
    }

    static void saveToDatabase(String orderId) {
        sleep(25);
        System.out.println("  [3] 데이터베이스 저장: " + orderId);
    }

    static void sendNotification(String orderId) {
        sleep(15);
        System.out.println("  [4] 알림 전송: " + orderId);
    }

    static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
