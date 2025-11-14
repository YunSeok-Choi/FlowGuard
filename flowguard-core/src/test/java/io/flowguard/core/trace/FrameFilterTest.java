package io.flowguard.core.trace;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("FrameFilter")
class FrameFilterTest {

    @Test
    @DisplayName("should accept frame when no filter is applied")
    void shouldAcceptFrameWhenNoFilter() {
        FrameFilter filter = FrameFilter.acceptAll();
        FrameInfo frame = new FrameInfo("com.example.Foo", "bar");

        boolean accepted = filter.test(frame);

        assertThat(accepted).isTrue();
    }

    @Test
    @DisplayName("should accept frame matching package prefix")
    void shouldAcceptFrameMatchingPackagePrefix() {
        FrameFilter filter = FrameFilter.byPackagePrefix("com.example");
        FrameInfo frame = new FrameInfo("com.example.Foo", "bar");

        boolean accepted = filter.test(frame);

        assertThat(accepted).isTrue();
    }

    @Test
    @DisplayName("should reject frame not matching package prefix")
    void shouldRejectFrameNotMatchingPackagePrefix() {
        FrameFilter filter = FrameFilter.byPackagePrefix("com.example");
        FrameInfo frame = new FrameInfo("org.other.Bar", "baz");

        boolean accepted = filter.test(frame);

        assertThat(accepted).isFalse();
    }
}
