package com.omegafrog.My.piano.app.utils.exception.article;

public class ArticleException extends RuntimeException {
    public ArticleException(String message) {
        super(message);
    }

    public ArticleException(String message, Throwable cause){
        super(message, cause);
    }

}
