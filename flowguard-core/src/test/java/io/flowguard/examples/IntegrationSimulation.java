package io.flowguard.examples;

import io.flowguard.core.FlowGuard;
import io.flowguard.core.config.FlowGuardConfig;
import io.flowguard.core.trace.CallTree;
import io.flowguard.core.trace.FrameInfo;
import io.flowguard.core.trace.TraceInterceptor;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * TraceInterceptor와 CallTree 통합 시뮬레이션
 * (실제 통합은 다음 단계에서 진행 예정)
 */
public class IntegrationSimulation {

    public static void main(String[] args) {
        System.out.println("=== TraceInterceptor + CallTree 통합 시뮬레이션 ===\n");

        System.out.println("이 예제는 향후 완전 통합 시 어떻게 동작할지 보여줍니다.");
        System.out.println("(현재 MVP에서는 수동으로 연결)\n");
        System.out.println("=".repeat(70) + "\n");

        // 시뮬레이션: TraceInterceptor가 수집한 데이터를 CallTree로 변환
        simulateIntegration();
    }

    static void simulateIntegration() {
        System.out.println("1단계: TraceInterceptor가 실시간 스택 프레임 수집");
        System.out.println("-".repeat(70));

        FlowGuardConfig config = FlowGuardConfig.defaults()
            .withPollInterval(Duration.ofMillis(10));

        TraceInterceptor interceptor = new TraceInterceptor(config);
        interceptor.startForCurrentThread();

        // 추적할 비즈니스 로직 실행
        userRegistrationFlow();

        interceptor.stop();

        // 수집된 원시 데이터 확인
        List<String> rawFrames = interceptor.getCollectedFrames();
        System.out.println("\n✓ 수집 완료: " + rawFrames.size() + "개 스택 프레임");
        System.out.println("✓ 샘플 횟수: " + interceptor.getSampleCount() + "회");

        System.out.println("\n" + "=".repeat(70) + "\n");

        // 2단계: 수집된 데이터를 CallTree로 변환 (시뮬레이션)
        System.out.println("2단계: 수집된 스택을 CallTree로 변환");
        System.out.println("-".repeat(70));

        CallTree callTree = buildCallTreeFromFrames(rawFrames, interceptor.getSampleCount());

        System.out.println("\n✓ CallTree 구축 완료");
        System.out.println("✓ 고유 호출 엣지: " + callTree.getEdgeCount() + "개");

        System.out.println("\n" + "=".repeat(70) + "\n");

        // 3단계: 호출 그래프 시각화
        System.out.println("3단계: 호출 그래프 출력");
        System.out.println("-".repeat(70));
        System.out.println("\n" + callTree.print());

        System.out.println("=".repeat(70) + "\n");

        // 해석
        printInterpretation();
    }

    /**
     * 수집된 원시 스택 프레임을 파싱하여 CallTree 구축
     * (실제 통합 시에는 TraceInterceptor 내부에서 자동으로 처리됨)
     */
    static CallTree buildCallTreeFromFrames(List<String> rawFrames, int sampleCount) {
        CallTree tree = new CallTree();

        System.out.println("수집된 " + sampleCount + "개 샘플 처리 중...");

        // 각 샘플을 그룹으로 분리 (간단한 시뮬레이션)
        // 실제로는 TraceInterceptor가 샘플별로 스택을 기록
        List<List<FrameInfo>> samples = parseIntoSamples(rawFrames);

        System.out.println("→ " + samples.size() + "개 샘플로 분리됨");

        int edgesAdded = 0;
        for (List<FrameInfo> sample : samples) {
            if (sample.size() >= 2) {
                // 스택을 역순으로 처리 (최하위 -> 최상위 호출 방향)
                for (int i = sample.size() - 1; i > 0; i--) {
                    tree.addEdge(sample.get(i), sample.get(i - 1));
                    edgesAdded++;
                }
            }
        }

        System.out.println("→ " + edgesAdded + "개 엣지 추가 (중복 제거 전)");
        System.out.println("→ " + tree.getEdgeCount() + "개 엣지 (중복 제거 후)");

        return tree;
    }

    /**
     * 원시 프레임 문자열을 샘플별로 그룹화
     * 실제 통합 시에는 TraceInterceptor가 구조화된 데이터로 저장
     */
    static List<List<FrameInfo>> parseIntoSamples(List<String> rawFrames) {
        List<List<FrameInfo>> samples = new ArrayList<>();
        List<FrameInfo> currentSample = new ArrayList<>();

        for (String frame : rawFrames) {
            // 우리 패키지의 프레임만 관심 대상
            if (frame.startsWith("io.flowguard.examples.IntegrationSimulation")) {
                FrameInfo info = parseFrame(frame);
                if (info != null) {
                    currentSample.add(info);
                }
            } else if (!currentSample.isEmpty() && frame.startsWith("io.flowguard.examples.IntegrationSimulation.main")) {
                // main을 만나면 샘플 종료
                samples.add(new ArrayList<>(currentSample));
                currentSample.clear();
            }
        }

        if (!currentSample.isEmpty()) {
            samples.add(currentSample);
        }

        return samples;
    }

    /**
     * 프레임 문자열을 FrameInfo로 파싱
     * 형식: "className.methodName:lineNumber"
     */
    static FrameInfo parseFrame(String frame) {
        try {
            String[] parts = frame.split(":");
            if (parts.length < 1) return null;

            String methodPart = parts[0];
            int lastDot = methodPart.lastIndexOf('.');
            if (lastDot < 0) return null;

            String className = methodPart.substring(0, lastDot);
            String methodName = methodPart.substring(lastDot + 1);

            return new FrameInfo(className, methodName);
        } catch (Exception e) {
            return null;
        }
    }

    static void printInterpretation() {
        System.out.println("📊 결과 해석");
        System.out.println("-".repeat(70));
        System.out.println();
        System.out.println("CallTree는 여러 샘플에서 발견된 호출 관계를 집계합니다:");
        System.out.println();
        System.out.println("1. 각 화살표(->)는 '호출 엣지'를 나타냅니다");
        System.out.println("   예: A.method1 -> B.method2");
        System.out.println("   의미: method1이 method2를 호출했음");
        System.out.println();
        System.out.println("2. 중복 제거:");
        System.out.println("   - 같은 호출 관계가 여러 샘플에서 발견되어도 1번만 기록");
        System.out.println("   - 예: 45개 샘플에서 100개 엣지 → 중복 제거 후 10개");
        System.out.println();
        System.out.println("3. 실제 호출 흐름 재구성:");
        System.out.println("   - 시작점(main) → 중간 메서드들 → 말단 메서드들");
        System.out.println("   - 전체 실행 경로를 한눈에 파악 가능");
        System.out.println();
        System.out.println("=".repeat(70));
    }

    // === 추적 대상: 사용자 등록 플로우 ===

    static void userRegistrationFlow() {
        System.out.println("→ 사용자 등록 시작\n");
        validateInput("user@example.com");
        createUser("user@example.com");
        sendWelcomeEmail("user@example.com");
        System.out.println("\n→ 사용자 등록 완료");
    }

    static void validateInput(String email) {
        sleep(15);
        System.out.println("  [1] 입력 검증: " + email);
        checkEmailFormat(email);
    }

    static void checkEmailFormat(String email) {
        sleep(10);
        System.out.println("      [1-1] 이메일 형식 확인");
    }

    static void createUser(String email) {
        sleep(20);
        System.out.println("  [2] 사용자 생성: " + email);
        saveToDatabase(email);
        createProfile(email);
    }

    static void saveToDatabase(String email) {
        sleep(15);
        System.out.println("      [2-1] DB 저장");
    }

    static void createProfile(String email) {
        sleep(10);
        System.out.println("      [2-2] 프로필 생성");
    }

    static void sendWelcomeEmail(String email) {
        sleep(20);
        System.out.println("  [3] 환영 이메일 전송: " + email);
        renderTemplate();
        sendEmail();
    }

    static void renderTemplate() {
        sleep(10);
        System.out.println("      [3-1] 템플릿 렌더링");
    }

    static void sendEmail() {
        sleep(10);
        System.out.println("      [3-2] 이메일 발송");
    }

    static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
