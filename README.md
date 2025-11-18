# FlowGuard

FlowGuard는 Java 애플리케이션의 런타임 호출 흐름을 추적하는 경량 라이브러리입니다. StackWalker 기반 샘플링을 통해 침습적인 계측(instrumentation) 없이 실행 경로를 캡처하고 시각화할 수 있습니다.

## 주요 특징

- **비침습적 추적**: StackWalker 기반 샘플링으로 코드 수정 없이 호출 흐름 캡처
- **경량 설계**: 최소한의 오버헤드로 프로덕션 환경에서도 사용 가능
- **유연한 설정**: 샘플링 간격 및 출력 전략을 자유롭게 커스터마이징
- **SPI 기반 확장**: ServiceLoader를 통한 커스텀 출력 구현 지원

## 요구사항

- Java 17 이상

## 설치

### 로컬 Maven 저장소 사용 (현재 권장)

FlowGuard는 아직 Maven Central에 배포되지 않았습니다. 로컬에서 사용하려면:

#### 1. FlowGuard를 로컬 Maven 저장소에 설치

```bash
# FlowGuard 프로젝트 루트에서 실행
./gradlew publishToMavenLocal
```

이 명령은 FlowGuard를 `~/.m2/repository/io/flowguard/`에 설치합니다.

#### 2. 프로젝트에서 사용

**Gradle:**

```gradle
repositories {
    mavenLocal()  // 로컬 Maven 저장소 추가
    mavenCentral()
}

dependencies {
    implementation 'io.flowguard:flowguard-core:0.1.0-SNAPSHOT'
}
```

**Maven:**

```xml
<!-- pom.xml -->
<repositories>
    <repository>
        <id>local-maven</id>
        <url>file://${user.home}/.m2/repository</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>io.flowguard</groupId>
        <artifactId>flowguard-core</artifactId>
        <version>0.1.0-SNAPSHOT</version>
    </dependency>
</dependencies>
```

#### 3. 설치 확인

```bash
# 로컬 Maven 저장소에 설치되었는지 확인
ls -la ~/.m2/repository/io/flowguard/flowguard-core/0.1.0-SNAPSHOT/
```

다음과 같은 파일들이 있어야 합니다:
- `flowguard-core-0.1.0-SNAPSHOT.jar` (컴파일된 클래스)
- `flowguard-core-0.1.0-SNAPSHOT-sources.jar` (소스 코드)
- `flowguard-core-0.1.0-SNAPSHOT-javadoc.jar` (Javadoc)
- `flowguard-core-0.1.0-SNAPSHOT.pom` (Maven 메타데이터)

### Maven Central (향후 배포 예정)

Maven Central 배포 후에는 별도 설치 없이 바로 사용 가능합니다:

**Gradle:**

```gradle
dependencies {
    implementation 'io.flowguard:flowguard-core:0.1.0'
}
```

**Maven:**

```xml
<dependency>
    <groupId>io.flowguard</groupId>
    <artifactId>flowguard-core</artifactId>
    <version>0.1.0</version>
</dependency>
```

## 빠른 시작

### 기본 사용법

가장 간단한 사용법은 `FlowGuard.trace()`에 추적하고 싶은 코드를 Runnable로 전달하는 것입니다:

```java
import io.flowguard.core.FlowGuard;

public class QuickStartExample {
    public static void main(String[] args) {
        FlowGuard.trace(() -> {
            businessLogic();
        });
    }

    static void businessLogic() {
        step1();
        step2();
    }

    static void step1() {
        System.out.println("Processing step 1");
    }

    static void step2() {
        System.out.println("Processing step 2");
    }
}
```

실행하면 콘솔에 다음과 같은 추적 정보가 출력됩니다:

```
FlowGuard :: main
Samples collected: 42
pollInterval=2ms
elapsed=84ms
```

### Try-with-resources 패턴

수동으로 추적 범위를 제어하고 싶다면 `autoTrace()`를 사용할 수 있습니다:

```java
import io.flowguard.core.FlowGuard;

public class ManualTraceExample {
    public static void main(String[] args) {
        try (AutoCloseable trace = FlowGuard.autoTrace()) {
            // 이 블록 안의 코드가 추적됩니다
            complexOperation();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void complexOperation() {
        // 복잡한 비즈니스 로직
    }
}
```

## 설정 커스터마이징

### 샘플링 간격 조정

기본 샘플링 간격은 2ms입니다. 더 정밀한 추적이 필요하거나 오버헤드를 줄이고 싶다면 간격을 조정할 수 있습니다:

```java
import io.flowguard.core.FlowGuard;
import io.flowguard.core.config.FlowGuardConfig;
import java.time.Duration;

public class CustomIntervalExample {
    public static void main(String[] args) {
        // 10ms 간격으로 샘플링 (오버헤드 감소)
        FlowGuardConfig config = FlowGuardConfig.defaults()
            .withPollInterval(Duration.ofMillis(10));

        FlowGuard.trace(config, () -> {
            longRunningOperation();
        });
    }

    static void longRunningOperation() {
        // 장시간 실행되는 작업
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
```

### 커스텀 출력 전략

FlowGuard는 ServiceLoader 기반 SPI를 통해 출력 전략을 확장할 수 있습니다. 커스텀 프린터를 구현하려면:

#### 1. FlowGuardPrinter 인터페이스 구현

```java
package com.example;

import io.flowguard.core.print.FlowGuardPrinter;
import java.util.List;

public class JsonFlowGuardPrinter implements FlowGuardPrinter {

    @Override
    public String id() {
        return "json";
    }

    @Override
    public void print(CharSequence title, List<String> lines) {
        System.out.println("{");
        System.out.println("  \"title\": \"" + title + "\",");
        System.out.println("  \"data\": [");
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            System.out.print("    \"" + line + "\"");
            if (i < lines.size() - 1) {
                System.out.println(",");
            } else {
                System.out.println();
            }
        }
        System.out.println("  ]");
        System.out.println("}");
    }
}
```

#### 2. ServiceLoader 등록

`src/main/resources/META-INF/services/io.flowguard.core.print.FlowGuardPrinter` 파일 생성:

```
com.example.JsonFlowGuardPrinter
```

#### 3. 커스텀 프린터 사용

```java
import io.flowguard.core.FlowGuard;
import io.flowguard.core.config.FlowGuardConfig;
import com.example.JsonFlowGuardPrinter;

public class CustomPrinterExample {
    public static void main(String[] args) {
        FlowGuardConfig config = FlowGuardConfig.defaults()
            .withPrinter(new JsonFlowGuardPrinter());

        FlowGuard.trace(config, () -> {
            businessLogic();
        });
    }

    static void businessLogic() {
        System.out.println("Doing work...");
    }
}
```

출력 예시:

```json
{
  "title": "FlowGuard :: main",
  "data": [
    "Samples collected: 15",
    "pollInterval=2ms",
    "elapsed=30ms"
  ]
}
```

## 고급 사용 예제

### 전체 설정 예제

```java
import io.flowguard.core.FlowGuard;
import io.flowguard.core.config.FlowGuardConfig;
import io.flowguard.core.print.ConsoleFlowGuardPrinter;
import java.time.Duration;

public class FullConfigExample {
    public static void main(String[] args) {
        FlowGuardConfig config = FlowGuardConfig.of(
            Duration.ofMillis(5),           // 5ms 샘플링 간격
            new ConsoleFlowGuardPrinter()   // 콘솔 출력
        );

        FlowGuard.trace(config, () -> {
            performComplexCalculation();
        });
    }

    static void performComplexCalculation() {
        int result = 0;
        for (int i = 0; i < 1000000; i++) {
            result += fibonacci(10);
        }
        System.out.println("Result: " + result);
    }

    static int fibonacci(int n) {
        if (n <= 1) return n;
        return fibonacci(n - 1) + fibonacci(n - 2);
    }
}
```

### CallTree를 사용한 호출 그래프 분석

CallTree를 직접 사용하여 호출 관계를 분석할 수 있습니다:

```java
import io.flowguard.core.trace.CallTree;
import io.flowguard.core.trace.FrameInfo;

public class CallTreeExample {
    public static void main(String[] args) {
        CallTree tree = new CallTree();

        // 호출 엣지 추가
        FrameInfo main = new FrameInfo("com.example.Main", "main");
        FrameInfo service = new FrameInfo("com.example.UserService", "getUser");
        FrameInfo dao = new FrameInfo("com.example.UserDao", "findById");

        tree.addEdge(main, service);
        tree.addEdge(service, dao);

        // 또는 스택 프레임 배열을 한번에 처리
        FrameInfo[] frames = {main, service, dao};
        tree.ingest(frames);

        // 호출 그래프 출력
        System.out.println("Edge count: " + tree.getEdgeCount());
        System.out.println("\nCall graph:");
        System.out.print(tree.print());
    }
}
```

출력:

```
Edge count: 2

Call graph:
com.example.Main.main -> com.example.UserService.getUser
com.example.UserService.getUser -> com.example.UserDao.findById
```

## API 레퍼런스

### FlowGuard

메인 엔트리 포인트 클래스입니다.

| 메서드 | 설명 |
|--------|------|
| `static void trace(Runnable runnable)` | 기본 설정으로 코드 블록을 추적합니다 |
| `static void trace(FlowGuardConfig config, Runnable runnable)` | 커스텀 설정으로 코드 블록을 추적합니다 |
| `static AutoCloseable autoTrace()` | 기본 설정으로 추적을 시작하고 AutoCloseable을 반환합니다 |
| `static AutoCloseable autoTrace(FlowGuardConfig config)` | 커스텀 설정으로 추적을 시작하고 AutoCloseable을 반환합니다 |

### FlowGuardConfig

추적 동작을 제어하는 불변 설정 객체입니다.

| 메서드 | 설명 |
|--------|------|
| `static FlowGuardConfig defaults()` | 기본 설정 반환 (2ms 샘플링 간격) |
| `static FlowGuardConfig of(Duration pollInterval, FlowGuardPrinter printer)` | 커스텀 설정 생성 |
| `FlowGuardConfig withPollInterval(Duration pollInterval)` | 새로운 샘플링 간격을 설정한 복사본 반환 |
| `FlowGuardConfig withPrinter(FlowGuardPrinter printer)` | 새로운 프린터를 설정한 복사본 반환 |
| `Duration pollInterval()` | 현재 샘플링 간격 반환 |
| `FlowGuardPrinter printer()` | 현재 프린터 반환 |

### FlowGuardPrinter

출력 전략을 정의하는 SPI 인터페이스입니다.

| 메서드 | 설명 |
|--------|------|
| `String id()` | 프린터 고유 식별자 반환 |
| `void print(CharSequence title, List<String> lines)` | 추적 결과를 출력 |

### CallTree

호출 그래프를 관리하는 클래스입니다.

| 메서드 | 설명 |
|--------|------|
| `void addEdge(FrameInfo caller, FrameInfo callee)` | 호출자-피호출자 엣지 추가 |
| `void ingest(FrameInfo[] frames)` | 스택 프레임 배열을 처리하여 엣지 생성 |
| `int getEdgeCount()` | 수집된 엣지 개수 반환 |
| `String print()` | 호출 그래프를 문자열로 출력 |

## 성능 고려사항

### 샘플링 간격

- **짧은 간격 (1-2ms)**: 더 정밀한 추적, 높은 오버헤드
- **긴 간격 (10-50ms)**: 낮은 오버헤드, 일부 호출 놓칠 수 있음
- **권장**: 개발 환경에서는 2-5ms, 프로덕션에서는 10-20ms

### 오버헤드

FlowGuard는 별도의 데몬 스레드에서 샘플링을 수행하므로 메인 스레드에 미치는 영향을 최소화합니다. 그러나 매우 짧은 간격으로 고빈도 샘플링을 하면 CPU 사용량이 증가할 수 있습니다.

## 제한사항

현재 MVP 버전의 제한사항:

- **단일 스레드 추적**: 한 번에 하나의 스레드만 추적 가능
- **기본 통계**: 샘플 수와 실행 시간만 제공 (향후 확장 예정)
- **필터링 없음**: 패키지/클래스 필터링 미지원 (향후 추가 예정)

## 로드맵

향후 계획된 기능:

- [ ] CallTree 통합 및 시각화된 호출 그래프 출력
- [ ] 패키지 include/exclude 필터링
- [ ] 호출 깊이 제한
- [ ] 호출 카운트 및 핫스팟 통계
- [ ] 멀티 스레드 동시 추적
- [ ] Graphviz DOT/JSON 포맷 출력
- [ ] Spring Boot AutoConfiguration
- [ ] `@FlowGuardAutoTrace` 애노테이션 지원

## 빌드 및 테스트

### 전체 빌드

```bash
./gradlew build
```

### 모듈별 테스트

```bash
# Core 모듈 테스트
./gradlew :flowguard-core:test

# 특정 테스트 클래스만 실행
./gradlew :flowguard-core:test --tests FlowGuardTraceTest
```

### 코드 포맷

```bash
# Google Java Format 적용
./gradlew spotlessApply

# 포맷 검사만 수행
./gradlew spotlessCheck
```

### 로컬 Maven 저장소 배포

```bash
./gradlew publishToMavenLocal
```

## 모듈 구조

- **flowguard-core**: 핵심 추적 엔진 및 API
- **flowguard-spring-starter**: Spring Boot 통합 (개발 중)
