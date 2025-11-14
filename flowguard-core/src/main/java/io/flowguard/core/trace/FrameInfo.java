package io.flowguard.core.trace;

/**
 * 스택 프레임 정보를 표현하는 불변 클래스.
 */
public record FrameInfo(String className, String methodName) {
}
