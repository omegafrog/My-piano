package com.omegafrog.My.piano.app.web.controller;

import com.omegafrog.My.piano.app.utils.AuthenticationUtil;
import com.omegafrog.My.piano.app.utils.response.APISuccessResponse;
import com.omegafrog.My.piano.app.utils.response.JsonAPIResponse;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.service.CashOrderApplicationService;
import lombok.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cash")
@RequiredArgsConstructor
public class CashController {

    private final CashOrderApplicationService cashOrderService;

    @GetMapping("/create")
    public JsonAPIResponse createCashOrder(String orderId, int amount)  {
        cashOrderService.createCashOrder(orderId, amount);
        return new APISuccessResponse("Create cash order success.");
    }

    @Setter
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    private static class CashOrderDto {
        public String paymentKey;
        public String orderId;
        public Integer amount;
    }

    @PostMapping("/request")
    public JsonAPIResponse requestCashOrder(@RequestBody CashOrderDto cashOrder) {
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        cashOrderService.requestCashOrder(cashOrder.paymentKey, cashOrder.orderId, cashOrder.amount, loggedInUser);
        return new APISuccessResponse("Request cash order success.");
    }

}
