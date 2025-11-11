package io.flowguard.core.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.flowguard.core.print.FlowGuardPrinter;
import io.flowguard.core.print.FlowGuardPrinters;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FlowGuardConfigTest {

    @Test
    @DisplayName("기본 설정은 2ms 폴링과 콘솔 프린터를 사용한다")
    void defaultsProvideConsolePrinter() {
        FlowGuardConfig config = FlowGuardConfig.defaults();

        assertThat(config.pollInterval()).isEqualTo(FlowGuardConfig.DEFAULT_POLL_INTERVAL);
        assertThat(config.printer()).isNotNull();
    }

    @Test
    @DisplayName("of()/with* 메서드로 샘플링 주기와 프린터를 교체할 수 있다")
    void overrideValuesWithFactoryMethods() {
        FlowGuardPrinter printer =
                new FlowGuardPrinter() {
                    @Override
                    public String id() {
                        return "test";
                    }

                    @Override
                    public void print(CharSequence title, List<String> lines) {}
                };

        FlowGuardConfig config = FlowGuardConfig.defaults()
                .withPollInterval(Duration.ofMillis(25))
                .withPrinter(printer);

        assertThat(config.pollInterval()).isEqualTo(Duration.ofMillis(25));
        assertThat(config.printer()).isSameAs(printer);
    }

    @Test
    @DisplayName("0 또는 음수 폴링 주기는 예외를 발생시킨다")
    void rejectsInvalidPollInterval() {
        assertThatThrownBy(() -> FlowGuardConfig.of(Duration.ZERO, FlowGuardPrinters.loadDefault()))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
