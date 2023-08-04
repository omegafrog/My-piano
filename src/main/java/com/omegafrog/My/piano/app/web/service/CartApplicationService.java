package com.omegafrog.My.piano.app.web.service;

import com.omegafrog.My.piano.app.web.domain.order.Order;
import com.omegafrog.My.piano.app.web.domain.order.OrderRepository;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.domain.user.UserRepository;
import com.omegafrog.My.piano.app.web.dto.order.OrderDto;
import com.omegafrog.My.piano.app.web.dto.order.OrderRegisterDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartApplicationService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    public List<OrderDto> addToCart(OrderDto dto, User loggedInUser)
            throws EntityNotFoundException {
        User user = userRepository.findById(loggedInUser.getId())
                .orElseThrow(() -> new EntityNotFoundException("Cannot find User entity : " + loggedInUser.getId()));
        Order order = orderRepository.findById(dto.getId())
                .orElseThrow(() -> new EntityNotFoundException("Cannot find Order entity : " + dto.getId()));
        user.getCart().addContent(order);
        return userRepository.save(user).getCart().getContents().stream().map(Order::toDto).toList();
    }


    public void deleteFromCart(Long id, User loggedInUser) {
        User user = userRepository.findById(loggedInUser.getId())
                .orElseThrow(() -> new EntityNotFoundException("Cannot find User entity : " + loggedInUser.getId()));
        user.getCart().deleteContent(id);
        orderRepository.deleteById(id);
    }

    public List<OrderDto> getAllContentFromCart(User loggedInUser) {
        User user = userRepository.findById(loggedInUser.getId())
                .orElseThrow(() -> new EntityNotFoundException("Cannot find User entity : " + loggedInUser.getId()));
        return user.getCart().getContents().stream().map(Order::toDto).toList();
    }
}
