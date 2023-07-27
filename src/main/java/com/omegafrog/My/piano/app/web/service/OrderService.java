package com.omegafrog.My.piano.app.web.service;

import com.omegafrog.My.piano.app.web.domain.coupon.Coupon;
import com.omegafrog.My.piano.app.web.domain.coupon.CouponRepository;
import com.omegafrog.My.piano.app.web.domain.lesson.Lesson;
import com.omegafrog.My.piano.app.web.domain.lesson.LessonRepository;
import com.omegafrog.My.piano.app.web.domain.order.Order;
import com.omegafrog.My.piano.app.web.domain.order.OrderRepository;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPost;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPostRepository;
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

    private final SheetPostRepository sheetPostRepository;
    private final UserRepository userRepository;
    private final LessonRepository lessonRepository;
    private final CouponRepository couponRepository;
    private final OrderRepository orderRepository;

    public OrderDto createSheetOrder(OrderRegisterDto orderRegisterDto)
            throws PersistenceException {
        SheetPost item = sheetPostRepository.findBySheetId(orderRegisterDto.getItemId())
                .orElseThrow(() -> new EntityNotFoundException("Cannot find Sheet entity : "
                        + orderRegisterDto.getItemId()));
        User buyer = userRepository.findById(orderRegisterDto.getBuyerId())
                .orElseThrow(() -> new EntityNotFoundException("Cannot find User entity : "
                        + orderRegisterDto.getBuyerId()));

        // TODO : SheetPost의 sheet, artist property는 non-null로 validation 해야함.
        Order.OrderBuilder orderBuilder = Order.builder()
                .item(item)
                .buyer(buyer)
                .seller(item.getArtist())
                .initialPrice(item.getPrice());

        if (orderRegisterDto.getCouponId() != null){
            Coupon coupon = couponRepository.findById(orderRegisterDto.getCouponId())
                    .orElseThrow(() -> new EntityNotFoundException("Cannot find Coupon entity : "
                            + orderRegisterDto.getCouponId()));
            orderBuilder = orderBuilder.coupon(coupon);
        }
        Order order = orderBuilder.build();
        order.calculateTotalPrice();
        return orderRepository.save(order).toDto();
    }

    public OrderDto createLessonOrder(OrderRegisterDto lessonOrderDto)
            throws PersistenceException {
        Lesson item = lessonRepository.findById(lessonOrderDto.getItemId())
                .orElseThrow(() -> new EntityNotFoundException("Cannot find Sheet entity : "
                        + lessonOrderDto.getItemId()));
        User buyer = userRepository.findById(lessonOrderDto.getBuyerId())
                .orElseThrow(() -> new EntityNotFoundException("Cannot find User entity : "
                        + lessonOrderDto.getBuyerId()));


        // TODO : SheetPost의 sheet, artist property는 non-null로 validation 해야함.
        Order.OrderBuilder orderBuilder = Order.builder()
                .item(item)
                .buyer(buyer)
                .seller(item.getLessonProvider())
                .initialPrice(item.getPrice());

        if (lessonOrderDto.getCouponId() != null){
            Coupon coupon = couponRepository.findById(lessonOrderDto.getCouponId())
                    .orElseThrow(() -> new EntityNotFoundException("Cannot find Coupon entity : "
                            + lessonOrderDto.getCouponId()));
            orderBuilder = orderBuilder.coupon(coupon);
        }
        Order order = orderBuilder.build();
        order.calculateTotalPrice();
        return orderRepository.save(order).toDto();
    }

    public OrderDto makePayment(OrderDto orderDto) throws PersistenceException{
        Order order = orderRepository.findById(orderDto.getId())
                .orElseThrow(() -> new EntityNotFoundException("Cannot find Order entity : " + orderDto.getId()));
        User buyer = userRepository.findById(orderDto.getBuyer().getId())
                .orElseThrow(() -> new EntityNotFoundException("Cannot find User entity : " +
                        orderDto.getBuyer().getId()));
        User seller = userRepository.findById(orderDto.getSeller().getId())
                .orElseThrow(() -> new EntityNotFoundException("Cannot find User entity : " +
                        orderDto.getSeller().getId()));

        buyer.pay(order);
        seller.receiveCash(orderDto.getTotalPrice());
        orderDto.setBuyer(buyer.getUserProfile());
        orderDto.setSeller(seller.getUserProfile());
        return orderDto;
    }

    public void deleteOrder(Long orderId) throws PersistenceException{
        orderRepository.deleteById(orderId);
    }

    public List<OrderDto> getAllOrders(User user){
        List<Order> byBuyerId = orderRepository.findByBuyer_id(user.getId());
        return byBuyerId.stream().map(Order::toDto).toList();
    }
}
