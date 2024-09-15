package com.omegafrog.My.piano.app.web.service;

import com.omegafrog.My.piano.app.utils.AuthenticationUtil;
import com.omegafrog.My.piano.app.web.domain.cart.Cart;
import com.omegafrog.My.piano.app.web.domain.order.Order;
import com.omegafrog.My.piano.app.web.domain.order.OrderRepository;
import com.omegafrog.My.piano.app.web.domain.order.SellableItem;
import com.omegafrog.My.piano.app.web.domain.order.SellableItemFactory;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.domain.user.UserRepository;
import com.omegafrog.My.piano.app.web.dto.order.OrderDto;
import com.omegafrog.My.piano.app.web.exception.message.ExceptionMessage;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class CartApplicationService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    @Autowired
    private final SellableItemFactory sellableItemFactory;
    private final AuthenticationUtil authenticationUtil;

    public List<OrderDto> addToCart(OrderDto dto) {
        User loggedInUser = authenticationUtil.getLoggedInUser();
        Order order = orderRepository.findById(dto.getId())
                .orElseThrow(() -> new EntityNotFoundException(ExceptionMessage.ENTITY_NOT_FOUND_ORDER + dto.getId()));
        loggedInUser.getCart().addContent(order);
        order.setCart(loggedInUser.getCart());
        return userRepository.save(loggedInUser).getCart().getContents().stream().map(Order::toDto).toList();
    }


    public void deleteFromCart(Long id) {
        User loggedInUser = authenticationUtil.getLoggedInUser();
        Cart cart = loggedInUser.getCart();
        cart.deleteContent(id);

    }

    public List<OrderDto> getAllContentFromCart() {
        User loggedInUser = authenticationUtil.getLoggedInUser();
        User user = userRepository.findById(loggedInUser.getId())
                .orElseThrow(() -> new EntityNotFoundException(ExceptionMessage.ENTITY_NOT_FOUND_USER + loggedInUser.getId()));
        return user.getCart().getContents().stream().map(Order::toDto).toList();
    }

    public void payAll() {
        User loggedInUser = authenticationUtil.getLoggedInUser();
        User user = userRepository.findById(loggedInUser.getId())
                .orElseThrow(() -> new EntityNotFoundException(ExceptionMessage.ENTITY_NOT_FOUND_USER + loggedInUser.getId()));
        user.getCart().payAllContents();
    }

    public boolean isItemInCart(String mainResource, Long id) {
        User loggedInUser = authenticationUtil.getLoggedInUser();
        SellableItem detailedItem = sellableItemFactory.createDetailedItem(mainResource, id);
        return loggedInUser.getCart().getContents().stream().anyMatch(order -> order.getItem().equals(detailedItem));
    }

    public int purchaseInCart(Set<Long> orderIds) {
        User loggedInUser = authenticationUtil.getLoggedInUser();
        User user = userRepository.findById(loggedInUser.getId()).orElseThrow(() -> new EntityNotFoundException("Cannot find User entity. id : " + loggedInUser.getId()));
        return user.getCart().payContents(orderIds);
    }
}
