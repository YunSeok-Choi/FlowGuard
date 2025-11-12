package io.flowguard.core.print;

import java.util.List;
import java.util.Objects;

/**
 * System.out 에 텍스트 트리를 그대로 출력하는 기본 구현.
 */
public final class ConsoleFlowGuardPrinter implements FlowGuardPrinter {

    public static final String ID = "console";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public void print(CharSequence title, List<String> lines) {
        Objects.requireNonNull(lines, "lines");
        System.out.println(title);
        for (String line : lines) {
            System.out.println(line);
        }
    }
}
