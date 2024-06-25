package com.omegafrog.My.piano.app.web.exception.order;

public class AlreadyPurchasedItemException extends OrderException {
    public AlreadyPurchasedItemException(Long itemId) {
        super("Already purchased item " + itemId + ".");
    }
}
