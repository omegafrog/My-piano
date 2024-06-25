package com.omegafrog.My.piano.app.web.controller;

import com.omegafrog.My.piano.app.web.dto.order.OrderDto;
import com.omegafrog.My.piano.app.web.dto.order.OrderRegisterDto;
import com.omegafrog.My.piano.app.web.response.success.ApiSuccessResponse;
import com.omegafrog.My.piano.app.web.response.success.JsonAPISuccessResponse;
import com.omegafrog.My.piano.app.web.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1")
public class OrderController {

    @Autowired
    private final OrderService orderService;

    @PostMapping("/order/{mainResource}")
    public JsonAPISuccessResponse orderItem(
            @PathVariable String mainResource,
            @Validated @RequestBody OrderRegisterDto order) {
        OrderDto createdOrder = orderService.makeOrder(mainResource, order);
        OrderDto processedOrder = orderService.makePayment(createdOrder);
        return new ApiSuccessResponse("Buy " + mainResource + " success.", processedOrder);
    }

    @GetMapping("/order/{mainResource}/{id}")
    public JsonAPISuccessResponse isOrderedItem(@PathVariable String mainResource, @PathVariable Long id){
        boolean isOrdered = orderService.isOrderedItem(mainResource, id);
        return new ApiSuccessResponse("Check isOrdered " + mainResource + "success.", isOrdered);
    }

    @GetMapping(path = "/order/{id}/cancel")
    public JsonAPISuccessResponse cancelOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return new ApiSuccessResponse("Cancel order success.");
    }

    @GetMapping(path = "/order")
    public JsonAPISuccessResponse getOrders() {
        List<OrderDto> allOrders = orderService.getAllOrders();
        return new ApiSuccessResponse("Success get all orders.", allOrders);
    }
}
