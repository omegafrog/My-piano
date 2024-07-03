package com.omegafrog.My.piano.app.web.exception.cart;

public class DuplicateItemOrderException extends CartException{
    public DuplicateItemOrderException(Long itemId, Long orderId) {
        super("Duplicated item "+itemId+ " in order "+orderId+".");
    }
    public DuplicateItemOrderException(Long itemId ) {
        super("Duplicated item "+itemId+" .");
    }
}
