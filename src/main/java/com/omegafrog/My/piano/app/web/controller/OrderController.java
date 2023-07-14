package com.omegafrog.My.piano.app.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.dto.order.OrderDto;
import com.omegafrog.My.piano.app.web.dto.order.OrderRegisterDto;
import com.omegafrog.My.piano.app.web.response.APIBadRequestResponse;
import com.omegafrog.My.piano.app.web.response.APIInternalServerResponse;
import com.omegafrog.My.piano.app.web.response.APISuccessResponse;
import com.omegafrog.My.piano.app.web.response.JsonAPIResponse;
import com.omegafrog.My.piano.app.web.service.OrderService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class OrderController {

    @Autowired
    private ObjectMapper objectMapper;
    private final OrderService orderService;

    @PostMapping(path = {"/sheet/buy","/lesson/buy"})
    public JsonAPIResponse orderItem( @RequestBody OrderRegisterDto order,
                                     HttpServletRequest request) {
        try {
            String mainResource = Arrays.asList(request.getRequestURI().split("/")).get(1);
            OrderDto createdOrder =
                    (mainResource.equals("sheet"))
                            ? orderService.createSheetOrder(order) : orderService.createLessonOrder(order);
            OrderDto processedOrder = orderService.makePayment(createdOrder);

            Map<String, Object> data = new HashMap<>();
            data.put("order", processedOrder);
            return new APISuccessResponse("Buy " + mainResource + " success.", objectMapper, data);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return new APIInternalServerResponse(e.getMessage());
        } catch (EntityNotFoundException e) {
            e.printStackTrace();
            return new APIBadRequestResponse(e.getMessage());
        }
    }

    @GetMapping(path = "/order/{id}/cancel")
    public JsonAPIResponse cancelOrder(@PathVariable Long id){
        try {
            orderService.deleteOrder(id);
            return new APISuccessResponse("Cancel order success.");
        } catch (EntityNotFoundException e) {
            e.printStackTrace();
            return new APIBadRequestResponse(e.getMessage());
        }catch (PersistenceException e){
            e.printStackTrace();
            return new APIInternalServerResponse(e.getMessage());
        }
    }

    @GetMapping(path = "/order")
    public JsonAPIResponse getOrders(Authentication authentication){
        try {
            User loggedInUser = (User) authentication.getDetails();
            List<OrderDto> allOrders = orderService.getAllOrders(loggedInUser);
            Map<String, Object> data = new HashMap<>();
            data.put("orders", allOrders);
            return new APISuccessResponse("Success get all orders.", objectMapper, data);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return new APIInternalServerResponse(e.getMessage());
        }
    }
}
