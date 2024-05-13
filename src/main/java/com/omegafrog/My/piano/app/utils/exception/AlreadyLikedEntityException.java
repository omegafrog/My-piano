package com.omegafrog.My.piano.app.utils.exception;

public class AlreadyLikedEntityException extends RuntimeException{
    public AlreadyLikedEntityException() {
        super("이미 좋아요를 누른 entity입니다.");
    }
}
