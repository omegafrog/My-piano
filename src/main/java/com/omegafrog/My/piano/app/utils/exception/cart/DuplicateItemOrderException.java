package com.omegafrog.My.piano.app.utils.exception.cart;

public class DuplicateItemOrderException extends CartException{
    public DuplicateItemOrderException(Long itemId, Long orderId) {
        super("Duplicated item "+itemId+ " in order "+orderId+".");
    }
}
