package com.omegafrog.My.piano.app.web.exception.order;

public class DuplicateItemException extends OrderException{
    public DuplicateItemException(Long itemId) {
        super("Duplicated item "+itemId+".");
    }

}
