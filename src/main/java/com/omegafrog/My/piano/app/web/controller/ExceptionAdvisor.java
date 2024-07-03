package com.omegafrog.My.piano.app.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.omegafrog.My.piano.app.security.exception.DuplicatePropertyException;
import com.omegafrog.My.piano.app.web.exception.payment.PaymentException;
import com.omegafrog.My.piano.app.web.response.APIBadRequestResponse;
import com.omegafrog.My.piano.app.web.response.APIInternalServerResponse;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class ExceptionAdvisor {

    /**
     * validation exception을 처리하는 handler
     *
     * @param ex 발생한 validation exception.
     * @return APIBadRequestResponse : exception에서 얻은 bindingResult의 message내용을 담아 리턴함.
     */
    @ExceptionHandler(BindException.class)
    public Object controllerViolationProcessing(BindException ex) {
        BindingResult bindingResult = ex.getBindingResult();
        StringBuilder builder = new StringBuilder();
        List<ObjectError> allErrors = bindingResult.getAllErrors();
        allErrors.forEach(objectError -> {
            FieldError fieldError = (FieldError) objectError;
            builder.append(fieldError.getField()).append(" ").append(fieldError.getDefaultMessage());
        });
        return new APIBadRequestResponse(builder.toString());
    }


    /**
     * 500 에러에 대한 handler
     *
     * @param ex JsonProcessingException, PersistenceException
     * @return APIInternalServerResponse : exception의 메세지를 담아 리턴함.
     */

    @ExceptionHandler(value = {JsonProcessingException.class, PersistenceException.class})
    public Object internalServerError(Throwable ex) {
        ex.printStackTrace();
        return new APIInternalServerResponse(ex.getMessage());
    }

    /**
     * 400 에러에 대한 handler
     *
     * @param ex EntityNotFoundException
     * @return APIBadRequestResponse : exception의 메세지를 담아 리턴함.
     */

    @ExceptionHandler({
            EntityNotFoundException.class,
            EntityExistsException.class, PaymentException.class,
            DuplicatePropertyException.class})
    public Object clientRequestError(RuntimeException ex) {
        ex.printStackTrace();
        return new APIBadRequestResponse(ex.getMessage());
    }

    @ExceptionHandler({ConstraintViolationException.class })
    public Object JDBCBindingViolationExceptionHandler(ConstraintViolationException ex) {
        ex.printStackTrace();
        StringBuilder builder = new StringBuilder();
        ex.getConstraintViolations().forEach(
                violation-> builder.append(violation.getMessage()).append("\n")
        );
        return new APIBadRequestResponse(builder.toString());
    }
    @ExceptionHandler(DataIntegrityViolationException.class)
    public Object JDBCBindingViolationExceptionHandler(DataIntegrityViolationException ex) {
        ex.printStackTrace();
        return new APIBadRequestResponse(ex.getCause().getCause().getMessage());
    }

    @ExceptionHandler(Exception.class)
    public Object exceptionHandler(Exception ex) {
        ex.printStackTrace();
        return new APIInternalServerResponse(ex.getMessage());
    }

}
