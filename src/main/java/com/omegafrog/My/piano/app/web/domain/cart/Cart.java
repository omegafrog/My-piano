package com.omegafrog.My.piano.app.web.domain.cart;

import com.omegafrog.My.piano.app.web.exception.cart.DuplicateItemOrderException;
import com.omegafrog.My.piano.app.web.domain.order.Order;
import com.omegafrog.My.piano.app.web.exception.payment.PaymentException;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.*;

@Entity
@NoArgsConstructor
@Getter
public class Cart implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int totalPrice = 0;

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
    @JoinTable(name = "cart_content",
            joinColumns = @JoinColumn(name = "CART_ID"),
            inverseJoinColumns = @JoinColumn(name = "ORDER_ID"))
    private List<Order> contents = new ArrayList<>();

    public void addContent(Order order) {
        boolean removed = contents.stream().anyMatch(o -> o.getItem().equals(order.getItem()));
        if(removed) throw new DuplicateItemOrderException(order.getItem().getId(), order.getId());
        contents.add(order);
        totalPrice += order.getTotalPrice();
    }

    public void deleteContent(Long orderId) throws EntityNotFoundException {
        boolean isRemoved = contents.removeIf(order -> order.getId().equals(orderId));
        if (!isRemoved)
            throw new EntityNotFoundException("Cannot find Order entity : " + orderId);
    }

    public void payAllContents() throws PaymentException {
        contents.forEach(content -> content.getBuyer().pay(content));
    }
    public int payContents(Set<Long> orderIds){
        List<Order> ordersToPay = new ArrayList<>();

        orderIds.forEach(orderId ->{
                    Order orderToPay = contents.stream().filter(order -> order.getId().equals(orderId)).findFirst()
                            .orElseThrow(() -> new EntityNotFoundException("Cannot find Order entity in cart. id : " + orderId));
                    ordersToPay.add(orderToPay);
                    contents.remove(orderToPay);
                }
        );
        ordersToPay.forEach(order -> order.getBuyer().pay(order));
        return ordersToPay.size();
    }

    public boolean itemIsInCart(Long id) {
        return contents.stream().anyMatch(order -> order.getId().equals(id));
    }

}
