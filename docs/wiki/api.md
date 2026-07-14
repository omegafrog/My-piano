# API

## OpenAPI

- [Swagger UI](/swagger-ui/index.html)
- [OpenAPI JSON](/v3/api-docs)

## 운영자 쿠폰 발급

`POST /api/v1/admin/coupons`

관리자 또는 슈퍼 관리자가 쿠폰을 발급한다. 요청에는 대상 사용자 ID, 쿠폰명, 할인 방식·값, 최소 주문금액, 최대 할인액, 유효기간이 포함된다.

응답 코드:

- `200`: 발급 성공
- `400`: 할인 조건 또는 유효기간이 유효하지 않음
- `403`: 운영자 권한 없음
