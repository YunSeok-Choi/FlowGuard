package io.flowguard.core.trace;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("CallTree")
class CallTreeTest {

    @Test
    @DisplayName("should create empty CallTree")
    void shouldCreateEmptyCallTree() {
        CallTree tree = new CallTree();

        assertThat(tree).isNotNull();
    }

    @Test
    @DisplayName("should add edge between two frames")
    void shouldAddEdgeBetweenTwoFrames() {
        CallTree tree = new CallTree();
        FrameInfo caller = new FrameInfo("com.example.Foo", "bar");
        FrameInfo callee = new FrameInfo("com.example.Baz", "qux");

        tree.addEdge(caller, callee);

        assertThat(tree.getEdgeCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("should deduplicate same edge")
    void shouldDeduplicateSameEdge() {
        CallTree tree = new CallTree();
        FrameInfo caller = new FrameInfo("com.example.Foo", "bar");
        FrameInfo callee = new FrameInfo("com.example.Baz", "qux");

        tree.addEdge(caller, callee);
        tree.addEdge(caller, callee);

        assertThat(tree.getEdgeCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("should ingest stack frames and create edges")
    void shouldIngestStackFramesAndCreateEdges() {
        CallTree tree = new CallTree();
        FrameInfo frame1 = new FrameInfo("com.example.Main", "main");
        FrameInfo frame2 = new FrameInfo("com.example.Foo", "bar");
        FrameInfo frame3 = new FrameInfo("com.example.Baz", "qux");

        tree.ingest(new FrameInfo[]{frame1, frame2, frame3});

        assertThat(tree.getEdgeCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("should print edges in basic format")
    void shouldPrintEdgesInBasicFormat() {
        CallTree tree = new CallTree();
        FrameInfo caller = new FrameInfo("com.example.Foo", "bar");
        FrameInfo callee = new FrameInfo("com.example.Baz", "qux");
        tree.addEdge(caller, callee);

        String output = tree.print();

        assertThat(output).contains("com.example.Foo.bar");
        assertThat(output).contains("com.example.Baz.qux");
    }
}
