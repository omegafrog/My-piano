# 검증

CHG-20260714-002에서 쿠폰 Kafka 설정, 조건 체인, 적용 기록, 운영자 발급 API, 주문 적용, 결제 결과 발행·소비, OpenAPI 런타임을 대상으로 테스트를 수행했다.

```bash
./gradlew test --tests com.omegafrog.My.piano.app.config.CouponKafkaConfigTest \
  --tests com.omegafrog.My.piano.app.web.domain.coupon.CouponTest \
  --tests com.omegafrog.My.piano.app.web.infrastructure.coupon.CouponApplicationRepositoryTest \
  --tests com.omegafrog.My.piano.app.web.controller.AdminCouponControllerTest \
  --tests com.omegafrog.My.piano.app.web.service.CouponApplicationServiceTest \
  --tests com.omegafrog.My.piano.app.web.controller.OrderControllerTest \
  --tests com.omegafrog.My.piano.app.web.service.PaymentResultPublisherTest \
  --tests com.omegafrog.My.piano.app.web.event.coupon.CouponPaymentResultConsumerIntegrationTest \
  --tests com.omegafrog.My.piano.app.web.controller.OpenApiRuntimeTest
```

전체 `./gradlew build`는 사용자 승인으로 면제했다. MySQL test cleanup lock과 Ehcache 리소스 추적 문제는 CHG-20260714-003에서 별도로 복구한다.
