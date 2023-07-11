package com.omegafrog.My.piano.app.web.domain.cart;

import com.omegafrog.My.piano.app.web.domain.sheet.Sheet;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@Getter
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToMany
    @JoinTable(name = "cart_content",
            joinColumns = @JoinColumn(name = "CART_ID"),
            inverseJoinColumns = @JoinColumn(name = "SHEET_ID"))
    private List<Sheet> contents = new ArrayList<>();

}
