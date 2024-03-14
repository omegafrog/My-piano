package com.omegafrog.My.piano.app.external.tossPayment;

import com.fasterxml.jackson.core.JsonProcessingException;

public interface TossWebHookResultFactory {
    TossWebHookResult parse(String json) throws JsonProcessingException;
}
