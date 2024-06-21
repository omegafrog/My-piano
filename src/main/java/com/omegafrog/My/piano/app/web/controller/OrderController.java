package com.omegafrog.My.piano.app.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.omegafrog.My.piano.app.utils.AuthenticationUtil;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.dto.order.OrderDto;
import com.omegafrog.My.piano.app.web.dto.order.OrderRegisterDto;
import com.omegafrog.My.piano.app.web.response.success.ApiSuccessResponse;
import com.omegafrog.My.piano.app.web.response.success.JsonAPISuccessResponse;
import com.omegafrog.My.piano.app.web.response.ResponseUtil;
import com.omegafrog.My.piano.app.web.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1")
public class OrderController {

    @Autowired
    private final OrderService orderService;

    @PostMapping("/order/{mainResource}")
    public JsonAPISuccessResponse orderItem(@PathVariable String mainResource, @Validated @RequestBody OrderRegisterDto order, HttpServletRequest request)
            throws JsonProcessingException {
        OrderDto createdOrder = orderService.makeOrder(mainResource, order);
        OrderDto processedOrder = orderService.makePayment(createdOrder);
        Map<String, Object> data = ResponseUtil.getStringObjectMap("order", processedOrder);
        return new ApiSuccessResponse("Buy " + mainResource + " success.", data);
    }

    @GetMapping("/order/{mainResource}/{id}")
    public JsonAPISuccessResponse isOrderedItem(@PathVariable String mainResource, @PathVariable Long id, HttpServletRequest request) throws JsonProcessingException {
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        boolean isOrdered = orderService.isOrderedItem(mainResource, id, loggedInUser);
        Map<String, Object> data = ResponseUtil.getStringObjectMap("isOrdered", isOrdered);
        return new ApiSuccessResponse("Check isOrdered " + mainResource + "success.", data);
    }

    @GetMapping(path = "/order/{id}/cancel")
    public JsonAPISuccessResponse cancelOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return new ApiSuccessResponse("Cancel order success.");
    }

    @GetMapping(path = "/order")
    public JsonAPISuccessResponse getOrders() throws JsonProcessingException {
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        List<OrderDto> allOrders = orderService.getAllOrders(loggedInUser);
        Map<String, Object> data = ResponseUtil.getStringObjectMap("orders", allOrders);
        return new ApiSuccessResponse("Success get all orders.", data);
    }
}
