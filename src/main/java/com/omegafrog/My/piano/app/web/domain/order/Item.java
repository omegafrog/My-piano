package com.omegafrog.My.piano.app.web.domain.order;

import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn
@NoArgsConstructor
@Getter
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime createdAt;

    private int price;

    private Long discountRate;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Item item = (Item) o;

        return Objects.equals(id, item.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    public Item(int price,LocalDateTime createdAt) {
        this.createdAt = createdAt;
        this.price = price;
        this.discountRate = 0L;
    }

    public void updatePrice(int price){
        this.price = price;
    }
    public void updateDiscountRate(Long discountRate){ this.discountRate = discountRate;}
}
