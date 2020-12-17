# Riptide: Concurrent

[![Lifebelt](../docs/rope.jpg)](https://pixabay.com/photos/rain-water-drip-drop-of-water-wire-57202/)

[![Javadoc](https://www.javadoc.io/badge/org.zalando/riptide-concurrent.svg)](http://www.javadoc.io/doc/org.zalando/riptide-concurrent)
[![Maven Central](https://img.shields.io/maven-central/v/org.zalando/riptide-concurrent.svg)](https://maven-badges.herokuapp.com/maven-central/org.zalando/riptide-concurrent)

*Riptide: Concurrent* offers a wizard-like API to safely and comprehensively construct a `java.util.concurrent.ThreadPoolExecutor`.

## Example

```java
var pool = ThreadPoolExecutors.builder()
    .elasticSize(5, 20)
    .boundedQueue(20)
    .build()
```

## Features

* Fluent `ThreadPoolExecutor` builder
* Dependency-free, i.e. can be used w/o the Riptide ecosystem
* Great developer experience in an IDE with code-completion
* Safe against misuse, i.e. less runtime errors due to

## Dependencies

- Java 8

## Installation

Add the following dependency to your project:

```xml
<dependency>
    <groupId>org.zalando</groupId>
    <artifactId>riptide-concurrent</artifactId>
    <version>${riptide.version}</version>
</dependency>
```

## Usage

The Builder API is essentially a state machine that will guide you through the process of constructing a `ThreadPoolExecutor`.
It will do so by only offering options in combinations that actually make sense and will work.

 

You can take a look at the following diagram to see the process:

<details>
  <summary>State diagram</summary>

*This will be rendered as an image, if you open it in IntelliJ IDEA with the Markdown plugin and Plantuml extension enabled.*

```plantuml
hide empty description

[*] --> Start

Start --> FixedSize: fixedSize
Start --> ElasticSize: elasticSize

FixedSize --> Threads: withoutQueue
FixedSize --> Threads: boundedQueue
FixedSize --> Threads: unboundedQueue

ElasticSize --> KeepAliveTime: keepAlive

KeepAliveTime --> QueueFirst: queueFirst
KeepAliveTime --> ScaleFirst: scaleFirst
KeepAliveTime --> Threads: withoutQueue

QueueFirst --> Threads: boundedQueue
ScaleFirst --> Threads: boundedQueue
ScaleFirst --> Threads: unboundedQueue

Threads --> [*]: build
Threads --> RejectedExecutions: threadFactory
Threads --> Build: handler
RejectedExecutions --> [*]: build
RejectedExecutions --> Build: handler

Build --> [*]: build
```

</details>

Please refer to the following code blocks for examples:

```java
var pool = ThreadPoolExecutors.builder()
    .fixedSize(20)
    .boundedQueue(20)
    .threadFactory(new CustomizableThreadFactory("my-prefix-"))
    .preStartCoreThreads()
    .handler(new CallerRunsPolicy())
    .build();
```

<summary>Elastic pool example</summary>

```java
var pool = ThreadPoolExecutors.builder()
    .elasticSize(5, 20)
    .keppAlive(Duration.ofMinutes(1))
    .boundedQueue(20)
    .threadFactory(new CustomizableThreadFactory("my-prefix-"))
    .preStartCoreThreads()
    .handler(new CallerRunsPolicy())
    .build();
```

### Elastic vs fixed size?

The very first decision that you need to make is whether a fixed or elastic thread pool is needed.
A fixed size thread pool will start off empty and ultimately grow to its maximum size.
Once it's at the maximum, it will stay there and never shrink back.

An elastic thread pool on the other hand has a core, and a maximum pool size.
If it's idling, it will shrink down to its core pool size.
The maximum time an idling thread is kept alive is configurable.

### Without queue vs un/bounded queue?

In general, one has the following options when deciding for a work queue:

 * `withoutQueue`  
   No work queue, i.e. reject tasks if no thread is available
 * `boundedQueue(int)`  
   A work queue with a maximum size, i.e. rejects tasks if no thread is available **and** work queue is full
 * `unboundedQueue()`  
   A work queue without a maximum size, i.e. it never rejects tasks

:rotating_light: **Unbounded queues** are risky in production since they will grow without limits and may either hide scaling/latency issues, consume too much memory or even both.

### Queue vs scale first?

Elastic pools that use a work queue have two options:

 * `scaleFirst()`  
   Start new threads until the pool reaches its maximum, before tasks are queued.
 * `queueFirst()`  
   Queue tasks in the work queue until it's full, before starting new threads.

The `ThreadPoolExecutor`'s default behavior (w/o using this library) is *queue-first*.
Most applications would benefit from defaulting to a *scale-first* policy though.

### Optional configuration

 * `threadFactory(ThreadFactory)`  
   * Spring's `CustomizableThreadFactory` which adds a thread name prefix
 * `preStartCoreThreads()` or `preStartCoreThreads(boolean)`  
   * Eagerly starts all core threads immediately, instead of *when needed*
   * Defaults to `false`
 * `handler(RejectedExecutionHandler)`, for example:
   * `AbortPolicy` (default)
   * `CallerRunsPolicy`
   * `DiscardOldestPolicy`
   * `DiscardPolicy`
   * or a custom `RejectedExecutionHandler`

```java
ThreadPoolExecutors.builder()
    // ...
    .threadFactory(new CustomizableThreadFactory("prefix-"))
    .preStartCoreThreads()
    .handler(new CallerRunsPolicy())
    .build():
```

## Getting Help

If you have questions, concerns, bug reports, etc., please file an issue in this repository's [Issue Tracker](../../../../issues).

## Getting Involved/Contributing

To contribute, simply make a pull request and add a brief description (1-2 sentences) of your addition or change. For
more details, check the [contribution guidelines](../.github/CONTRIBUTING.md).

# Credits and references 

 * [Java Scale First ExecutorService — A myth or a reality](https://medium.com/@uditharosha/java-scale-first-executorservice-4245a63222df)
