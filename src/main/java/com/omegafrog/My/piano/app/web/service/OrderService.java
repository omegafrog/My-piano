package com.omegafrog.My.piano.app.web.service;

import com.omegafrog.My.piano.app.utils.exception.message.ExceptionMessage;
import com.omegafrog.My.piano.app.web.domain.coupon.Coupon;
import com.omegafrog.My.piano.app.web.domain.coupon.CouponRepository;
import com.omegafrog.My.piano.app.web.domain.order.Order;
import com.omegafrog.My.piano.app.web.domain.order.SellableItemFactory;
import com.omegafrog.My.piano.app.web.domain.order.OrderRepository;
import com.omegafrog.My.piano.app.web.domain.order.SellableItem;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.domain.user.UserRepository;
import com.omegafrog.My.piano.app.web.dto.order.OrderDto;
import com.omegafrog.My.piano.app.web.dto.order.OrderRegisterDto;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {
    private final UserRepository userRepository;
    private final CouponRepository couponRepository;
    private final OrderRepository orderRepository;
    private final SellableItemFactory sellableItemFactory;
    private final String USER_ENTITY_NOT_FOUNT_ERROR_MSG = "Cannot find User entity : ";

    public OrderDto makePayment(OrderDto orderDto) throws PersistenceException {
        Order order = orderRepository.findById(orderDto.getId())
                .orElseThrow(() -> new EntityNotFoundException(ExceptionMessage.ENTITY_NOT_FOUND_ORDER + orderDto.getId()));
        User buyer = userRepository.findById(orderDto.getBuyer().getId())
                .orElseThrow(() -> new EntityNotFoundException(ExceptionMessage.ENTITY_NOT_FOUND_USER +
                        orderDto.getBuyer().getId()));
        User seller = userRepository.findById(orderDto.getSeller().getId())
                .orElseThrow(() -> new EntityNotFoundException(ExceptionMessage.ENTITY_NOT_FOUND_USER +
                        orderDto.getSeller().getId()));

        buyer.pay(order);
        seller.receiveCash(orderDto.getTotalPrice());
        orderDto.setBuyer(buyer.getUserProfile());
        orderDto.setSeller(seller.getUserProfile());
        return orderDto;
    }

    public void deleteOrder(Long orderId) throws PersistenceException {
        orderRepository.deleteById(orderId);
    }

    public List<OrderDto> getAllOrders(User user) {
        List<Order> byBuyerId = orderRepository.findByBuyer_id(user.getId());
        return byBuyerId.stream().map(Order::toDto).toList();
    }

    public OrderDto makeOrder(String mainResourceName, OrderRegisterDto dto) {
        User buyer = userRepository.findById(dto.getBuyerId())
                .orElseThrow(() -> new EntityNotFoundException(USER_ENTITY_NOT_FOUNT_ERROR_MSG
                        + dto.getBuyerId()));

        SellableItem item = sellableItemFactory.getDetailedItem(mainResourceName, dto.getItemId());

        // TODO : SheetPost의 sheet, artist property는 non-null로 validation 해야함.
        Order.OrderBuilder orderBuilder = Order.builder()
                .item(item)
                .buyer(buyer)
                .seller(item.getAuthor())
                .initialPrice(item.getPrice());

        if (dto.getCouponId() != null) {
            Coupon coupon = couponRepository.findById(dto.getCouponId())
                    .orElseThrow(() -> new EntityNotFoundException("Cannot find Coupon entity : "
                            + dto.getCouponId()));
            coupon.validate(buyer);
            orderBuilder = orderBuilder.coupon(coupon);
        }
        Order order = orderBuilder.build();
        order.calculateTotalPrice();
        return orderRepository.save(order).toDto();
    }
}
