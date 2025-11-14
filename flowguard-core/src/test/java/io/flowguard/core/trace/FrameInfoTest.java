package io.flowguard.core.trace;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("FrameInfo")
class FrameInfoTest {

    @Test
    @DisplayName("should create FrameInfo with className and methodName")
    void shouldCreateFrameInfoWithClassAndMethod() {
        FrameInfo frame = new FrameInfo("com.example.Foo", "bar");

        assertThat(frame.className()).isEqualTo("com.example.Foo");
        assertThat(frame.methodName()).isEqualTo("bar");
    }
}
