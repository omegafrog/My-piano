package com.omegafrog.My.piano.app.external.tossPayment;

import com.omegafrog.My.piano.app.utils.exception.payment.CashOrderConfirmFailedException;
import com.omegafrog.My.piano.app.web.domain.cash.CashOrder;
import lombok.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

public class TossPaymentInstance {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${payment.toss.client-key}")
    private String clientKey;

    @Value("${payment.toss.secret-key}")
    private String secretKey;

    @Value("${payment.toss.api-url}")
    private String baseURL;

    @Data
    @NoArgsConstructor
    @Setter
    @Getter
    @AllArgsConstructor
    private static class RequestBody{
        private String paymentKey;
        private String orderId;
        private Integer amount;
    }

    public Payment requestCashOrder(CashOrder order){

        RequestBody requestBody = new RequestBody(order.getPaymentKey(), order.getOrderId(), order.getAmount());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(secretKey,"");

        HttpEntity<RequestBody> entity = new HttpEntity<>(requestBody, headers);
        System.out.println("entity = " + entity);

        ResponseEntity<Payment> exchange = restTemplate.exchange(baseURL + "/confirm", HttpMethod.POST, entity, Payment.class);
        if(exchange.getStatusCode().isError())
            throw new CashOrderConfirmFailedException("결제 승인 실패.");

        return exchange.getBody();
    }

}
