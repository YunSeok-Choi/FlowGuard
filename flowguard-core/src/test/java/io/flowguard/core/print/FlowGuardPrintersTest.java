package io.flowguard.core.print;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FlowGuardPrintersTest {

    @Test
    @DisplayName("loadAll()는 SPI 등록에 따라 최소 console 프린터를 포함한다")
    void loadAllIncludesConsole() {
        List<FlowGuardPrinter> printers = FlowGuardPrinters.loadAll();

        assertThat(printers).isNotEmpty();
        assertThat(printers.stream().map(FlowGuardPrinter::id)).contains("console");
    }

    @Test
    @DisplayName("loadDefault()는 등록 프린터가 있으면 그중 하나를 반환하고, 없을 경우 console 프린터를 반환한다")
    void loadDefaultReturnsSomething() {
        FlowGuardPrinter printer = FlowGuardPrinters.loadDefault();

        assertThat(printer).isNotNull();
        assertThat(printer.id()).isNotBlank();
    }

    @Test
    @DisplayName("loadById('console')는 콘솔 프린터를 찾는다")
    void loadByIdFindsConsole() {
        Optional<FlowGuardPrinter> found = FlowGuardPrinters.loadById("console");

        assertThat(found).isPresent();
        assertThat(found.get().id()).isEqualTo("console");
    }

    @Test
    @DisplayName("knownPrinterIds()는 등록된 프린터 ID들을 쉼표로 연결한다")
    void knownPrinterIdsReturnsCommaSeparated() {
        String ids = FlowGuardPrinters.knownPrinterIds();

        assertThat(ids).isNotBlank();
        // 최소한 console 이 포함되어야 한다
        assertThat(ids).contains("console");
        // 쉼표 기반 나열을 기대(등록이 1개일 수도 있으므로 단정은 느슨하게)
        assertThat(ids).doesNotContain("  ");
    }
}
