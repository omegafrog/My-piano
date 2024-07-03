package com.omegafrog.My.piano.app.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.omegafrog.My.piano.app.external.tossPayment.PaymentStatusChangedResult;
import com.omegafrog.My.piano.app.utils.MapperUtil;
import com.omegafrog.My.piano.app.web.response.success.ApiResponse;
import com.omegafrog.My.piano.app.web.response.success.JsonAPIResponse;
import com.omegafrog.My.piano.app.web.domain.cash.PaymentHistory;
import com.omegafrog.My.piano.app.web.service.CashOrderApplicationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Nullable;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/cash")
@RequiredArgsConstructor
public class CashController {

    private final CashOrderApplicationService cashOrderService;
    private final MapperUtil mapperUtil;

    @PostMapping("/webhook")
    public void expireCashOrder(@Valid @NotNull @RequestBody String json) throws JsonProcessingException {
        PaymentStatusChangedResult tossWebHookResult =  (PaymentStatusChangedResult) mapperUtil.parseTossWebhook(json);
        cashOrderService.expireCashOrder(tossWebHookResult);
    }

    @GetMapping("/info")
    public JsonAPIResponse createCashOrder(@Valid @NotNull String orderId,
                                           @Valid @Positive int amount,
                                           @Valid @NotEmpty String name) {
        cashOrderService.createCashOrder(orderId, amount, name);
        return new ApiResponse("Create cash order success.");
    }

    @Setter
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    private static class CashOrderDto {
        @NotNull
        public String paymentKey;
        @NotNull
        public String orderId;
        @Positive
        public Integer amount;
    }

    @PostMapping("/request")
    public JsonAPIResponse requestCashOrder(
            @Valid @RequestBody CashOrderDto cashOrder) throws JsonProcessingException {
        cashOrderService.requestCashOrder(cashOrder.paymentKey, cashOrder.orderId, cashOrder.amount);
        return new ApiResponse("Request cash order success.");
    }

    @GetMapping
    public JsonAPIResponse<List<PaymentHistory>> getPaymentHistory(
            @Valid @Nullable @RequestParam LocalDate start,
            @Valid @Nullable @RequestParam LocalDate end,
            Pageable pageable) {
        List<PaymentHistory> paymentHistory =
                cashOrderService.getPaymentHistory( start, end, pageable);
        return new ApiResponse("get payment history success.", paymentHistory);
    }
}
