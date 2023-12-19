package com.omegafrog.My.piano.app.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.omegafrog.My.piano.app.utils.AuthenticationUtil;
import com.omegafrog.My.piano.app.utils.response.*;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.dto.order.OrderDto;
import com.omegafrog.My.piano.app.web.dto.order.OrderRegisterDto;
import com.omegafrog.My.piano.app.web.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    @Autowired
    private ObjectMapper objectMapper;
    private final OrderService orderService;

    @PostMapping("/order/{mainResource}")
    public JsonAPIResponse orderItem(@PathVariable String mainResource, @Validated @RequestBody OrderRegisterDto order, HttpServletRequest request)
            throws JsonProcessingException {
        OrderDto createdOrder = orderService.makeOrder(mainResource, order);
        OrderDto processedOrder = orderService.makePayment(createdOrder);
        Map<String, Object> data = ResponseUtil.getStringObjectMap("order", processedOrder);
        return new APISuccessResponse("Buy " + mainResource + " success.", data);
    }

    @GetMapping("/order/{mainResource}/{id}")
    public JsonAPIResponse isOrderedItem(@PathVariable String mainResource, @PathVariable Long id, HttpServletRequest request) throws JsonProcessingException {
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        boolean isOrdered = orderService.isOrderedItem(mainResource, id, loggedInUser);
        Map<String, Object> data = ResponseUtil.getStringObjectMap("isOrdered", isOrdered);
        return new APISuccessResponse("Check isOrdered " + mainResource + "success.", data);
    }

    @GetMapping(path = "/order/{id}/cancel")
    public JsonAPIResponse cancelOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return new APISuccessResponse("Cancel order success.");
    }

    @GetMapping(path = "/order")
    public JsonAPIResponse getOrders() throws JsonProcessingException {
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        List<OrderDto> allOrders = orderService.getAllOrders(loggedInUser);
        Map<String, Object> data = ResponseUtil.getStringObjectMap("orders", allOrders);
        return new APISuccessResponse("Success get all orders.", data);
    }
}
