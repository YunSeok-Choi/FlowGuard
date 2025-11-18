package io.flowguard.examples;

import io.flowguard.core.trace.CallTree;
import io.flowguard.core.trace.FrameInfo;

/**
 * CallTree 직접 사용 예제
 */
public class CallTreeExample {

    public static void main(String[] args) {
        System.out.println("=== CallTree Example ===\n");

        CallTree tree = new CallTree();

        // 예제 1: 수동으로 엣지 추가
        System.out.println("Example 1: Manual Edge Addition");
        System.out.println("--------------------------------");

        FrameInfo main = new FrameInfo("com.example.Application", "main");
        FrameInfo controller = new FrameInfo("com.example.UserController", "getUser");
        FrameInfo service = new FrameInfo("com.example.UserService", "findUser");
        FrameInfo repository = new FrameInfo("com.example.UserRepository", "findById");

        tree.addEdge(main, controller);
        tree.addEdge(controller, service);
        tree.addEdge(service, repository);

        System.out.println("Added 3 edges manually\n");

        // 예제 2: 스택 프레임 배열로 일괄 처리
        System.out.println("Example 2: Ingest Stack Frames");
        System.out.println("-------------------------------");

        FrameInfo[] stackTrace = {
            main,
            controller,
            new FrameInfo("com.example.UserService", "validateUser"),
            new FrameInfo("com.example.ValidationService", "check")
        };

        tree.ingest(stackTrace);

        System.out.println("Ingested 4 frames (3 edges)\n");

        // 결과 출력
        System.out.println("Results:");
        System.out.println("--------");
        System.out.println("Total unique edges: " + tree.getEdgeCount());
        System.out.println("\nCall Graph:");
        System.out.print(tree.print());

        // 예제 3: 중복 제거 확인
        System.out.println("\nExample 3: Deduplication Test");
        System.out.println("------------------------------");

        CallTree dedupTree = new CallTree();
        FrameInfo caller = new FrameInfo("com.example.Foo", "bar");
        FrameInfo callee = new FrameInfo("com.example.Baz", "qux");

        dedupTree.addEdge(caller, callee);
        dedupTree.addEdge(caller, callee);
        dedupTree.addEdge(caller, callee);

        System.out.println("Added same edge 3 times");
        System.out.println("Actual edge count: " + dedupTree.getEdgeCount() + " (deduplicated!)");
    }
}
