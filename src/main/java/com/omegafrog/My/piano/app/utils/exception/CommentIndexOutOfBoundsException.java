package com.omegafrog.My.piano.app.utils.exception;

public class CommentIndexOutOfBoundsException extends RuntimeException {
    public CommentIndexOutOfBoundsException(IndexOutOfBoundsException e, String s) {
        super(s, e);
    }
}
