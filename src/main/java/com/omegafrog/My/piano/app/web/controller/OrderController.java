package com.omegafrog.My.piano.app.web.controller;

import com.omegafrog.My.piano.app.web.dto.order.OrderDto;
import com.omegafrog.My.piano.app.web.dto.order.OrderRegisterDto;
import com.omegafrog.My.piano.app.web.response.success.ApiResponse;
import com.omegafrog.My.piano.app.web.response.success.JsonAPIResponse;
import com.omegafrog.My.piano.app.web.service.OrderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/order")
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/{mainResource}")
    public JsonAPIResponse orderItem(
            @Valid @NotNull @PathVariable(name = "mainResource") String mainResource,
            @Valid @NotNull @RequestBody OrderRegisterDto order) {
        OrderDto createdOrder = orderService.makeOrder(mainResource, order);
        OrderDto processedOrder = orderService.makePayment(createdOrder);
        return new ApiResponse("Buy " + mainResource + " success.", processedOrder);
    }

    @GetMapping("/{mainResource}/{id}")
    public JsonAPIResponse isOrderedItem(
            @Valid @NotNull @PathVariable(name = "mainResource") String mainResource,
            @Valid @NotNull @PathVariable(name = "id") Long id) {
        boolean isOrdered = orderService.isOrderedItem(mainResource, id);
        return new ApiResponse("Check isOrdered " + mainResource + "success.", isOrdered);
    }

    @DeleteMapping(path = "/{id}")
    public JsonAPIResponse cancelOrder(
            @Valid @NotNull @PathVariable(name = "id") Long id) {
        orderService.deleteOrder(id);
        return new ApiResponse("Cancel order success.");
    }

    @GetMapping(path = "")
    public JsonAPIResponse getOrders() {
        List<OrderDto> allOrders = orderService.getAllOrders();
        return new ApiResponse("Success get all orders.", allOrders);
    }
}
