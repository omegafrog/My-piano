package com.omegafrog.My.piano.app.web.controller.advisor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.omegafrog.My.piano.app.utils.response.APIBadRequestResponse;
import com.omegafrog.My.piano.app.utils.response.APIInternalServerResponse;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class ExceptionAdvisor {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Object processValidationError(MethodArgumentNotValidException ex) {
        BindingResult bindingResult = ex.getBindingResult();
        StringBuilder builder = new StringBuilder();
        List<ObjectError> allErrors = bindingResult.getAllErrors();
        allErrors.forEach(objectError -> {
            FieldError fieldError = (FieldError) objectError;
            builder.append(fieldError.getField() + " " + fieldError.getDefaultMessage());
        });
        return new APIBadRequestResponse(builder.toString());
    }

    @ExceptionHandler(value = {JsonProcessingException.class, PersistenceException.class})
    public Object internalServerError(JsonProcessingException ex) {
        ex.printStackTrace();
        return new APIInternalServerResponse(ex.getMessage());
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public Object clientRequestError(EntityNotFoundException ex) {
        ex.printStackTrace();
        return new APIBadRequestResponse(ex.getMessage());
    }
}
