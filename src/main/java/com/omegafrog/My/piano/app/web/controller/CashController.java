package com.omegafrog.My.piano.app.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.omegafrog.My.piano.app.external.tossPayment.PaymentStatusChangedResult;
import com.omegafrog.My.piano.app.utils.MapperUtil;
import com.omegafrog.My.piano.app.web.domain.cash.PaymentHistory;
import com.omegafrog.My.piano.app.web.response.success.ApiResponse;
import com.omegafrog.My.piano.app.web.response.success.JsonAPIResponse;
import com.omegafrog.My.piano.app.web.service.CashOrderApplicationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Nullable;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/cash")
@RequiredArgsConstructor
public class CashController {

    private final CashOrderApplicationService cashOrderService;
    private final MapperUtil mapperUtil;

    @PostMapping("/webhook")
    public void expireCashOrder(@Valid @NotNull @RequestBody String json) throws JsonProcessingException {
        PaymentStatusChangedResult tossWebHookResult = (PaymentStatusChangedResult) mapperUtil.parseTossWebhook(json);
        cashOrderService.expireCashOrder(tossWebHookResult);
    }

    @GetMapping("/info")
    public JsonAPIResponse createCashOrder(@Valid @NotNull @RequestParam(name = "orderId") String orderId,
                                           @Valid @Positive @RequestParam(name = "amount") int amount,
                                           @Valid @NotEmpty @RequestParam(name = "name") String name) {
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

    @Profile("test")
    @GetMapping("/charge")
    public JsonAPIResponse chargeCash(@Valid @RequestParam(name = "amount") int amount) {
        cashOrderService.chargeCash(amount);
        return new ApiResponse("Charge cash success.");
    }


    @GetMapping
    public JsonAPIResponse<Page<PaymentHistory>> getPaymentHistory(
            @Valid @Nullable @RequestParam(name = "start") LocalDate start,
            @Valid @Nullable @RequestParam(name = "end") LocalDate end,
            @PageableDefault(size = 30) Pageable pageable) {
        Page<PaymentHistory> paymentHistory =
                cashOrderService.getPaymentHistory(start, end, pageable);
        return new ApiResponse("get payment history success.", paymentHistory);
    }

    @DeleteMapping
    public JsonAPIResponse<String> cancelPayment(
            @Valid @NotNull @RequestParam(name = "orderId") String orderId,
            @Valid @NotNull @RequestParam(name = "cancelReason") String cancelReason
    ) {
        cashOrderService.cancelPayment(orderId, cancelReason);
        return new ApiResponse<>("Cancel payment success.");
    }
}
