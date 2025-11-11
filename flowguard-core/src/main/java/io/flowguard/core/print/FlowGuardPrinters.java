package io.flowguard.core.print;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ServiceLoader;

/**
 * ServiceLoader 기반으로 Printer 구현체를 조회하는 작은 헬퍼.
 */
public final class FlowGuardPrinters {

    private FlowGuardPrinters() {}

    /** 등록된 프린터가 없으면 콘솔 프린터를 반환한다. */
    public static FlowGuardPrinter loadDefault() {
        List<FlowGuardPrinter> printers = loadAll();
        return printers.isEmpty() ? new ConsoleFlowGuardPrinter() : printers.get(0);
    }

    /** classpath 에 등록된 모든 프린터를 반환한다. */
    public static List<FlowGuardPrinter> loadAll() {
        ServiceLoader<FlowGuardPrinter> loader = ServiceLoader.load(FlowGuardPrinter.class);
        List<FlowGuardPrinter> printers = new ArrayList<>();
        for (FlowGuardPrinter printer : loader) {
            printers.add(printer);
        }
        if (printers.isEmpty()) {
            printers.add(new ConsoleFlowGuardPrinter());
        }
        return printers;
    }

    /** ID 로 특정 프린터를 찾는다. 없으면 Optional.empty. */
    public static Optional<FlowGuardPrinter> loadById(String id) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }
        String normalized = id.trim().toLowerCase(Locale.ROOT);
        for (FlowGuardPrinter printer : loadAll()) {
            if (printer.id().equalsIgnoreCase(normalized)) {
                return Optional.of(printer);
            }
        }
        return Optional.empty();
    }

    /** 사용 가능한 프린터 ID 목록을 쉼표로 이어붙여 반환한다. */
    public static String knownPrinterIds() {
        List<FlowGuardPrinter> printers = loadAll();
        if (printers.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < printers.size(); i++) {
            builder.append(printers.get(i).id());
            if (i < printers.size() - 1) {
                builder.append(", ");
            }
        }
        return builder.toString();
    }
}
