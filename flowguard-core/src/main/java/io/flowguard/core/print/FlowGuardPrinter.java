package io.flowguard.core.print;

import java.util.List;

/**
 * 간단한 출력 전략 인터페이스. ServiceLoader 로 구현체를 주입할 수 있다.
 */
public interface FlowGuardPrinter {

    /** 환경 설정에서 식별할 수 있는 짧은 ID (예: "console"). */
    String id();

    /** 제목과 렌더링된 라인 리스트를 출력한다. */
    void print(CharSequence title, List<String> lines);
}
