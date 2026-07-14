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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/coupons")
@RequiredArgsConstructor
public class AdminCouponController {
    private final CouponIssuanceApplicationService couponIssuanceApplicationService;
    @PostMapping
    public JsonAPIResponse<CouponResponse> issue(@Valid @RequestBody IssueCouponRequest request) {
        return new ApiResponse<>("Coupon issued.", couponIssuanceApplicationService.issue(request));
    }
}
