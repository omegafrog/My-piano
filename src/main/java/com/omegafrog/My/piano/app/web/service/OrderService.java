package com.omegafrog.My.piano.app.web.service;

import com.omegafrog.My.piano.app.utils.AuthenticationUtil;
import com.omegafrog.My.piano.app.web.exception.message.ExceptionMessage;
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
import com.omegafrog.My.piano.app.web.exception.order.AlreadyPurchasedItemException;
import com.omegafrog.My.piano.app.web.exception.order.DuplicateItemException;
import com.omegafrog.My.piano.app.web.exception.order.SamePartyException;
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
    private final AuthenticationUtil authenticationUtil;
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
        orderDto.setBuyer(buyer.getUserInfo());
        orderDto.setSeller(seller.getUserInfo());
        return orderDto;
    }

    public void deleteOrder(Long orderId){
        orderRepository.deleteById(orderId);
    }

    public List<OrderDto> getAllOrders() {
        User loggedInUser = authenticationUtil.getLoggedInUser();
        List<Order> byBuyerId = orderRepository.findByBuyer_id(loggedInUser.getId());
        return byBuyerId.stream().map(Order::toDto).toList();
    }

    public OrderDto makeOrder(String mainResourceName, OrderRegisterDto dto) {
        User buyer = userRepository.findById(dto.getBuyerId())
                .orElseThrow(() -> new EntityNotFoundException(USER_ENTITY_NOT_FOUNT_ERROR_MSG
                        + dto.getBuyerId()));

        SellableItem item = sellableItemFactory.createDetailedItem(mainResourceName, dto.getItemId());

        if(buyer.getCart().itemIsInCart(item.getId()))
            throw new DuplicateItemException(item.getId());

        if (buyer.equals(item.getAuthor()))
            throw new SamePartyException("구매자와 판매자가 같을 수 없습니다.");

        if(buyer.isPurchased(item)) throw new AlreadyPurchasedItemException(item.getId());

        Order order = buildOrder(dto, buyer, item);
        order.calculateTotalPrice();
        return orderRepository.save(order).toDto();
    }

    private Order buildOrder(OrderRegisterDto dto, User buyer, SellableItem item) {
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
        return orderBuilder.build();
    }

    public boolean isOrderedItem(String mainResource, Long id) {
        User loggedInUser = authenticationUtil.getLoggedInUser();
        SellableItem item = sellableItemFactory.createDetailedItem(mainResource, id);
        User user = userRepository.findById(loggedInUser.getId()).orElseThrow(() -> new EntityNotFoundException("Cannot find User entity : " + loggedInUser.getId()));
        return user.isPurchased(item);
    }
}
