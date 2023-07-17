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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    @Autowired
    private ObjectMapper objectMapper;
    private final OrderService orderService;

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Object processValidationError(MethodArgumentNotValidException ex) {
        BindingResult bindingResult = ex.getBindingResult();
        StringBuilder builder = new StringBuilder();
        List<ObjectError> allErrors = bindingResult.getAllErrors();
        allErrors.forEach(objectError -> {
            FieldError fieldError = (FieldError) objectError;
            builder.append(fieldError.getField() + " " + fieldError.getDefaultMessage());
        });
        return new APIBadRequestResponse(builder.toString());
    }

    @ExceptionHandler(value = {JsonProcessingException.class, PersistenceException.class})
    public Object internalServerError(JsonProcessingException ex) {
        ex.printStackTrace();
        return new APIInternalServerResponse(ex.getMessage());
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public Object clientRequestError(EntityNotFoundException ex) {
        ex.printStackTrace();
        return new APIBadRequestResponse(ex.getMessage());
    }


    @PostMapping(path = {"/sheet/buy", "/lesson/buy"})
    public JsonAPIResponse orderItem(@Validated @RequestBody OrderRegisterDto order, HttpServletRequest request)
            throws JsonProcessingException, EntityNotFoundException {
        String mainResource = Arrays.asList(request.getRequestURI().split("/")).get(1);
        OrderDto createdOrder =
                (mainResource.equals("sheet"))
                        ? orderService.createSheetOrder(order) : orderService.createLessonOrder(order);
        OrderDto processedOrder = orderService.makePayment(createdOrder);

        Map<String, Object> data = new HashMap<>();
        data.put("order", processedOrder);
        return new APISuccessResponse("Buy " + mainResource + " success.", objectMapper, data);
    }

    @GetMapping(path = "/order/{id}/cancel")
    public JsonAPIResponse cancelOrder(@PathVariable Long id) throws PersistenceException {
        orderService.deleteOrder(id);
        return new APISuccessResponse("Cancel order success.");
    }


    @GetMapping(path = "/order")
    public JsonAPIResponse getOrders() throws JsonProcessingException, PersistenceException {
        SecurityContext context = SecurityContextHolder.getContext();
        User loggedInUser = (User) context.getAuthentication().getDetails();
        List<OrderDto> allOrders = orderService.getAllOrders(loggedInUser);
        Map<String, Object> data = new HashMap<>();
        data.put("orders", allOrders);
        return new APISuccessResponse("Success get all orders.", objectMapper, data);
    }
}
