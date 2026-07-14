package com.omegafrog.My.piano.app.web.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.omegafrog.My.piano.app.web.dto.coupon.CouponResponse;
import com.omegafrog.My.piano.app.web.dto.coupon.IssueCouponRequest;
import com.omegafrog.My.piano.app.web.response.success.ApiResponse;
import com.omegafrog.My.piano.app.web.response.success.JsonAPIResponse;
import com.omegafrog.My.piano.app.web.service.CouponIssuanceApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/coupons")
@RequiredArgsConstructor
@Tag(name = "운영자 쿠폰 API", description = "운영자가 사용자에게 할인 쿠폰을 발급합니다.")
public class AdminCouponController {
    private final CouponIssuanceApplicationService couponIssuanceApplicationService;

    @PostMapping
    @Operation(summary = "할인 쿠폰 발급", description = "관리자 또는 슈퍼 관리자가 대상 사용자에게 정액 또는 정률 할인 쿠폰을 발급합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "쿠폰 발급 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "쿠폰 할인 조건 또는 유효기간이 잘못됨"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "운영자 권한 없음")
    })
    public JsonAPIResponse<CouponResponse> issue(@Valid @RequestBody IssueCouponRequest request) {
        return new ApiResponse<>("Coupon issued.", couponIssuanceApplicationService.issue(request));
    }
}
