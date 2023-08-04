package com.omegafrog.My.piano.app.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.dto.lesson.LessonRegisterDto;
import com.omegafrog.My.piano.app.web.dto.order.OrderDto;
import com.omegafrog.My.piano.app.web.dto.order.OrderRegisterDto;
import com.omegafrog.My.piano.app.web.service.CartApplicationService;
import com.omegafrog.My.piano.app.web.service.OrderService;
import com.omegafrog.My.piano.app.web.util.AuthenticationUtil;
import com.omegafrog.My.piano.app.web.util.response.APISuccessResponse;
import com.omegafrog.My.piano.app.web.util.response.JsonAPIResponse;
import com.omegafrog.My.piano.app.web.util.response.ResponseUtil;
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

    @PostMapping(value = {"/sheet", "/lesson"})
    public JsonAPIResponse addToCart(@RequestBody OrderRegisterDto dto, HttpServletRequest request) throws JsonProcessingException {
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        String resourceName = request.getRequestURI().split("/")[2];
        OrderDto createdOrder =
                (resourceName.equals("sheet"))
                        ? orderService.createSheetOrder(dto) : orderService.createLessonOrder(dto);
        List<OrderDto> orderDtos = cartService.addToCart(createdOrder, loggedInUser);
        Map<String, Object> data = ResponseUtil.getStringObjectMap("contents", orderDtos);
        return new APISuccessResponse("Add order to cart success.", data, objectMapper);
    }


    @DeleteMapping("/{id}")
    public JsonAPIResponse deleteFromCart(@PathVariable Long id)
            throws EntityNotFoundException {
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        cartService.deleteFromCart(id, loggedInUser);
        return new APISuccessResponse("Delete order from cart success.");
    }

    @GetMapping("")
    public JsonAPIResponse getAllContentFromCart() throws JsonProcessingException , PersistenceException {
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        List<OrderDto> allContentFromCart = cartService.getAllContentFromCart(loggedInUser);
        Map<String, Object> data = ResponseUtil.getStringObjectMap("contents", allContentFromCart);
        return new APISuccessResponse("Get all cart contents success.", data, objectMapper);
    }

}
