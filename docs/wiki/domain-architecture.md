# 도메인·아키텍처

## Coupon BC

`Coupon`은 단일 쿠폰 영속 모델이다. 정액·정률 할인 조건과 순서가 있는 검증 조건 컬렉션을 소유한다. 조건은 소유자, 유효기간, 사용 상태, 최소 주문금액을 평가하고 첫 실패에서 중단한다.

`CouponApplication`은 주문 ID, 쿠폰 ID, 임시 할인액, 적용 상태를 보관한다. 결제 결과를 중복 처리하지 않도록 수신 이벤트 ID도 영속한다.

## 결제 결과 통신

주문·현금결제 컴포넌트가 JSON `PaymentResultEvent(eventId, orderId, occurredAt)`를 발행한다.

- 성공 topic: `payment-succeeded`
- 실패 topic: `payment-failed`
- Coupon consumer: 성공 시 사용 완료, 실패 시 복구
- 실패 처리: 1초부터 2배 간격으로 3회 재시도 후 `<원본-topic>.DLT`

Coupon Kafka 구성은 `coupon.kafka.enabled`가 `true`일 때 활성화된다.
