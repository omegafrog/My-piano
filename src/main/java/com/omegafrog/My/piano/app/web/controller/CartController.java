package com.omegafrog.My.piano.app.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.omegafrog.My.piano.app.utils.AuthenticationUtil;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.dto.order.OrderDto;
import com.omegafrog.My.piano.app.web.dto.order.OrderRegisterDto;
import com.omegafrog.My.piano.app.utils.exception.payment.PaymentException;
import com.omegafrog.My.piano.app.web.service.CartApplicationService;
import com.omegafrog.My.piano.app.web.service.OrderService;
import com.omegafrog.My.piano.app.utils.response.APISuccessResponse;
import com.omegafrog.My.piano.app.utils.response.JsonAPIResponse;
import com.omegafrog.My.piano.app.utils.response.ResponseUtil;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartApplicationService cartService;

    @Autowired
    private ObjectMapper objectMapper;

    private final OrderService orderService;



    @DeleteMapping("/{id}")
    public JsonAPIResponse deleteFromCart(@PathVariable Long id)
             {
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        cartService.deleteFromCart(id, loggedInUser);
        return new APISuccessResponse("Delete order from cart success.");
    }

    @GetMapping("/pay")
    public JsonAPIResponse payCart() throws PaymentException {
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        cartService.payAll(loggedInUser);
        return new APISuccessResponse("Buy items in your cart success.");
    }

    @GetMapping("")
    public JsonAPIResponse getAllContentFromCart() throws JsonProcessingException , PersistenceException {
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        List<OrderDto> allContentFromCart = cartService.getAllContentFromCart(loggedInUser);
        Map<String, Object> data = ResponseUtil.getStringObjectMap("contents", allContentFromCart);
        return new APISuccessResponse("Get all cart contents success.", data);
    }
}
