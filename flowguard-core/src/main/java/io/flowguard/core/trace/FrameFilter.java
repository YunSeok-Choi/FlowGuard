package io.flowguard.core.trace;

import java.util.function.Predicate;

/**
 * 스택 프레임을 필터링하는 인터페이스.
 */
@FunctionalInterface
public interface FrameFilter extends Predicate<FrameInfo> {

    /**
     * 모든 프레임을 허용하는 필터.
     */
    static FrameFilter acceptAll() {
        return frame -> true;
    }

    /**
     * 특정 패키지 접두사로 시작하는 프레임만 허용하는 필터.
     */
    static FrameFilter byPackagePrefix(String prefix) {
        return frame -> frame.className().startsWith(prefix);
    }
}
