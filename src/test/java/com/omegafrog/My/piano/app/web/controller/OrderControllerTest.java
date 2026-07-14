package com.omegafrog.My.piano.app.web.controller;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

class OrderControllerTest {
    @Test void orderApiPathIsStable() { assertThat("/api/v1/order").startsWith("/api/v1/"); }
}
