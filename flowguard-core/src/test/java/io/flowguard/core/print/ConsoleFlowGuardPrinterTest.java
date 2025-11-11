package io.flowguard.core.print;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ConsoleFlowGuardPrinterTest {

    private PrintStream originalOut;
    private ByteArrayOutputStream capture;

    @BeforeEach
    void setUp() {
        originalOut = System.out;
        capture = new ByteArrayOutputStream();
        System.setOut(new PrintStream(capture, true, StandardCharsets.UTF_8));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    @Test
    @DisplayName("id()는 'console'을 반환한다")
    void idIsConsole() {
        ConsoleFlowGuardPrinter printer = new ConsoleFlowGuardPrinter();

        assertThat(printer.id()).isEqualTo("console");
    }

    @Test
    @DisplayName("print(title, lines)는 제목과 각 라인을 System.out에 출력한다")
    void printWritesToStdout() {
        ConsoleFlowGuardPrinter printer = new ConsoleFlowGuardPrinter();

        printer.print("TITLE", List.of("line1", "line2"));

        String out = capture.toString(StandardCharsets.UTF_8);
        assertThat(out).contains("TITLE");
        assertThat(out).contains("line1");
        assertThat(out).contains("line2");
    }
}
