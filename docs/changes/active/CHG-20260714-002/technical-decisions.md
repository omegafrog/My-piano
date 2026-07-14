---
status: ready
---

# 기술 결정

| 기술 문제 | 선택 | 구현 영향 | 테스트 영향 |
| --- | --- | --- | --- |
| 기존 쿠폰 모델과 신규 할인 쿠폰 모델의 공존 방지 | 기존 `Coupon` 엔티티를 확장·마이그레이션해 단일 쿠폰 모델로 유지한다. 별도 `DiscountCoupon` 영속 모델은 만들지 않는다. | 쿠폰 발급·검증·임시 적용·사용·복구 기능과 검증 조건 컬렉션은 기존 `Coupon`에 추가한다. 기존 쿠폰 데이터와 참조 경로는 새 모델로 분기하지 않는다. | 기존 Coupon 조회·참조 흐름의 회귀와, 조건 체인/임시 적용 상태가 단일 Coupon 모델에서 동작함을 검증한다. |
| 결제 결과 Kafka 이벤트 발행 책임 | 이 애플리케이션의 주문/현금결제 처리 컴포넌트가 결제 성공·실패 이벤트를 발행한다. 외부 OrderPayment BC 발행자를 전제하지 않는다. | 주문/현금결제의 성공·실패 확정 지점에서 `payment-succeeded` 또는 `payment-failed`를 발행하고 Coupon consumer가 이를 구독한다. | 주문/현금결제 성공·실패별 발행 payload, Coupon consumer 연동, 발행 실패·중복 처리 경로를 검증한다. |
| OrderPayment BC의 결제 성공·실패 이벤트 전달 | 신규 Kafka 계약: `payment-succeeded`, `payment-failed` topic; `eventId`, `orderId`, `occurredAt` payload; Coupon consumer group; 멱등 consumer | Coupon BC가 두 topic을 구독해 `orderId`의 CouponApplication과 Coupon 상태를 USED 또는 RESTORED로 전이한다. `eventId`를 기준으로 중복 전달을 무시한다. | 동일 `eventId` 중복, 순서 변경, consumer 재시작 후 최종 상태가 중복 전이되지 않는 통합 테스트가 필요하다. |
| Kafka 직렬화·재시도/DLQ 구성 | JSON 직렬화; 처리 실패 시 3회 지수 백오프 재시도; 재시도 소진 시 `<원본-topic>.DLT` 전송 | Coupon consumer는 JSON 역직렬화로 결제 결과를 받고, 실패 레코드를 지수 백오프로 최대 3회 재처리한다. 이후 `payment-succeeded.DLT` 또는 `payment-failed.DLT`로 보낸다. | JSON 역직렬화, 3회 재시도, 재시도 소진 후 원본 topic별 DLT 전송을 검증하는 통합 테스트가 필요하다. |

## 확인 결과

- 기존 Coupon을 확장·마이그레이션하는 단일 쿠폰 모델로 확정했다. DDD 문서의 `DiscountCoupon`은 확장된 기존 `Coupon`의 도메인 역할명이며, 별도 `DiscountCoupon` 영속 모델은 만들지 않는다.
- 결제 성공·실패 이벤트는 이 애플리케이션의 주문/현금결제 처리 컴포넌트가 발행한다.
- 결제 결과 계약은 신규 topic 두 개와 `eventId`, `orderId`, `occurredAt` payload로 확정됐다.
- Coupon consumer는 consumer group 소속이며, `eventId` 기준 멱등 처리를 한다.
- 결제 결과 이벤트는 JSON 스키마로 직렬화한다.
- 처리 실패 레코드는 최대 3회 지수 백오프로 재시도한다.
- 재시도 소진 레코드는 원본 topic별 `<원본-topic>.DLT`로 전송한다.
