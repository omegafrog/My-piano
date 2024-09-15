package com.omegafrog.My.piano.app.web.domain.cart;

import com.omegafrog.My.piano.app.web.domain.order.Order;
import com.omegafrog.My.piano.app.web.exception.cart.DuplicateItemOrderException;
import com.omegafrog.My.piano.app.web.exception.payment.PaymentException;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Entity
@NoArgsConstructor
@Getter
public class Cart implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int totalPrice = 0;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.REMOVE, mappedBy = "cart", orphanRemoval = true)
    private List<Order> contents = new ArrayList<>();

    public void addContent(Order order) {
        boolean duplicated = contents.stream().anyMatch(o -> o.getItem().equals(order.getItem()));
        if (duplicated) throw new DuplicateItemOrderException(order.getItem().getId(), order.getId());
        contents.add(order);
        totalPrice += order.getTotalPrice();
    }

    public void deleteContent(Long orderId) throws EntityNotFoundException {
        Order founded = contents.stream().filter(item -> item.getId().equals(orderId)).findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Cannot find Order entity : " + orderId));

        founded.setCart(null);
        contents.removeIf(order -> order.getId().equals(orderId));
    }

    public void payAllContents() throws PaymentException {
        contents.forEach(content -> content.getBuyer().pay(content));
    }

    public int payContents(Set<Long> orderIds) {
        List<Order> ordersToPay = new ArrayList<>();

        orderIds.forEach(orderId -> {
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
