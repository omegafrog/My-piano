package com.omegafrog.My.piano.app.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.omegafrog.My.piano.app.utils.AuthenticationUtil;
import com.omegafrog.My.piano.app.web.domain.order.SellableItemFactory;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.dto.order.OrderDto;
import com.omegafrog.My.piano.app.web.dto.order.OrderRegisterDto;
import com.omegafrog.My.piano.app.web.service.CartApplicationService;
import com.omegafrog.My.piano.app.web.service.OrderService;
import com.omegafrog.My.piano.app.web.response.success.ApiSuccessResponse;
import com.omegafrog.My.piano.app.web.response.success.JsonAPISuccessResponse;
import com.omegafrog.My.piano.app.web.response.ResponseUtil;
import jakarta.persistence.PersistenceException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartApplicationService cartService;

    @Autowired
    private ObjectMapper objectMapper;

    private final OrderService orderService;
    @Autowired
    private final SellableItemFactory sellableItemFactory;

    @PostMapping("{mainResource}")
    public JsonAPISuccessResponse addToCart(@RequestBody OrderRegisterDto dto, @PathVariable String mainResource, HttpServletRequest request)
            throws JsonProcessingException {
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        OrderDto createdOrder = orderService.makeOrder(mainResource, dto);
        List<OrderDto> orderDtos = cartService.addToCart(createdOrder, loggedInUser);
        Map<String, Object> data = ResponseUtil.getStringObjectMap("contents", orderDtos);
        return new ApiSuccessResponse("Add order to cart success.", data);
    }

    @DeleteMapping("/{id}")
    public JsonAPISuccessResponse deleteFromCart(@PathVariable Long id){
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        cartService.deleteFromCart(id, loggedInUser);
        return new ApiSuccessResponse("Delete order from cart success.");
    }

    @GetMapping("")
    public JsonAPISuccessResponse getAllContentFromCart() throws JsonProcessingException , PersistenceException {
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        List<OrderDto> allContentFromCart = cartService.getAllContentFromCart(loggedInUser);
        Map<String, Object> data = ResponseUtil.getStringObjectMap("contents", allContentFromCart);
        return new ApiSuccessResponse("Get all cart contents success.", data);
    }
    @GetMapping("{mainResource}/{id}")
    public JsonAPISuccessResponse isItemInCart(@PathVariable String mainResource, @PathVariable Long id) throws JsonProcessingException {
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        boolean isInCart = cartService.isItemInCart(mainResource, id, loggedInUser);
        Map<String, Object> data = ResponseUtil.getStringObjectMap("isInCart", isInCart);
        return new ApiSuccessResponse("Check item is in cart success.", data);
    }

    @PatchMapping("")
    public JsonAPISuccessResponse purchaseInCart(@RequestParam(name="orderId") Set<Long> orderId) throws JsonProcessingException {
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        int payCnt = cartService.purchaseInCart(orderId, loggedInUser);
        Map<String, Object> data = ResponseUtil.getStringObjectMap("payCnt", payCnt);
        return new ApiSuccessResponse("Purchase all content in cart success.", data);
    }

}
