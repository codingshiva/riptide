package org.zalando.riptide.failsafe;

import org.junit.jupiter.api.Test;

final class CircuitBreakerListenerTest {

    private final CircuitBreakerListener unit = CircuitBreakerListener.DEFAULT;

    @Test
    void shouldDoNothing() {
        unit.onOpen();
        unit.onHalfOpen();
        unit.onClose();
    }

}
