package io.flowguard.core.trace;

/**
 * 호출 그래프의 엣지(caller → callee)를 표현하는 불변 클래스. CallTree에서 중복 엣지를 제거하기 위한 키로 사용됩니다.
 */
public record EdgeKey(FrameInfo caller, FrameInfo callee) {
}
