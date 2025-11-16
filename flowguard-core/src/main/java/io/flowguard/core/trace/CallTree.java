package io.flowguard.core.trace;

import java.util.HashSet;
import java.util.Set;

/**
 * 호출 그래프를 나타내는 트리 구조. 스택 샘플을 수집하여 호출 관계를 구축합니다.
 */
public class CallTree {

    private final Set<EdgeKey> edges = new HashSet<>();

    public CallTree() {
    }

    public void addEdge(FrameInfo caller, FrameInfo callee) {
        EdgeKey edge = new EdgeKey(caller, callee);
        edges.add(edge);
    }

    public void ingest(FrameInfo[] frames) {
        for (int i = 0; i < frames.length - 1; i++) {
            addEdge(frames[i], frames[i + 1]);
        }
    }

    public int getEdgeCount() {
        return edges.size();
    }

    public String print() {
        StringBuilder sb = new StringBuilder();
        for (EdgeKey edge : edges) {
            String caller = edge.caller().className() + "." + edge.caller().methodName();
            String callee = edge.callee().className() + "." + edge.callee().methodName();
            sb.append(caller).append(" -> ").append(callee).append("\n");
        }
        return sb.toString();
    }
}
