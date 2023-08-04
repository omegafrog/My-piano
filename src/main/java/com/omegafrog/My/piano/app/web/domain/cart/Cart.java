package com.omegafrog.My.piano.app.web.domain.cart;

import com.omegafrog.My.piano.app.web.domain.order.Order;
import com.omegafrog.My.piano.app.web.domain.order.SellableItem;
import com.omegafrog.My.piano.app.web.domain.sheet.Sheet;
import com.omegafrog.My.piano.app.web.exception.payment.PaymentException;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@Getter
public class Cart {

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
        contents.add(order);
        totalPrice += order.getTotalPrice();
    }

    public void deleteContent(Long orderId) throws EntityNotFoundException {
        boolean isRemoved = contents.removeIf(order -> order.getId().equals(orderId));
        if (!isRemoved)
            throw new EntityNotFoundException("Cannot find Order entity : " + orderId);
    }

    public void payContents() throws PaymentException {
        contents.forEach(content -> content.getBuyer().pay(content));
    }
}
