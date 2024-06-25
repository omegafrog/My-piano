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
import jakarta.persistence.PersistenceException;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartApplicationService cartService;
    private final OrderService orderService;

    @PostMapping("{mainResource}")
    public JsonAPISuccessResponse addToCart(@RequestBody OrderRegisterDto dto, @PathVariable String mainResource)  {
        OrderDto createdOrder = orderService.makeOrder(mainResource, dto);
        List<OrderDto> orderDtoList = cartService.addToCart(createdOrder);
        return new ApiSuccessResponse("Add order to cart success.", orderDtoList);
    }

    @DeleteMapping("/{id}")
    public JsonAPISuccessResponse deleteFromCart(@NotNull @PathVariable Long id){
        cartService.deleteFromCart(id);
        return new ApiSuccessResponse("Delete order from cart success.");
    }

    @GetMapping("")
    public JsonAPISuccessResponse getAllContentFromCart() throws PersistenceException {
        List<OrderDto> allContentFromCart = cartService.getAllContentFromCart();
        return new ApiSuccessResponse("Get all cart contents success.", allContentFromCart);
    }
    @GetMapping("{mainResource}/{id}")
    public JsonAPISuccessResponse isItemInCart(@PathVariable String mainResource, @PathVariable Long id) throws JsonProcessingException {
        boolean isInCart = cartService.isItemInCart(mainResource, id);
        return new ApiSuccessResponse("Check item is in cart success.", isInCart);
    }

    @PatchMapping("")
    public JsonAPISuccessResponse purchaseInCart(@RequestParam(name="orderId") Set<Long> orderId) throws JsonProcessingException {
        int payCnt = cartService.purchaseInCart(orderId);
        return new ApiSuccessResponse("Purchase all content in cart success.",payCnt);
    }

}
