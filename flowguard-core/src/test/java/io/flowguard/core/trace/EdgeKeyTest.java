package io.flowguard.core.trace;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("EdgeKey")
class EdgeKeyTest {

    @Test
    @DisplayName("should create EdgeKey with caller and callee")
    void shouldCreateEdgeKeyWithCallerAndCallee() {
        FrameInfo caller = new FrameInfo("com.example.Foo", "bar");
        FrameInfo callee = new FrameInfo("com.example.Baz", "qux");

        EdgeKey edge = new EdgeKey(caller, callee);

        assertThat(edge.caller()).isEqualTo(caller);
        assertThat(edge.callee()).isEqualTo(callee);
    }
}
