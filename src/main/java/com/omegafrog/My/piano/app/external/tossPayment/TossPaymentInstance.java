package com.omegafrog.My.piano.app.external.tossPayment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.omegafrog.My.piano.app.utils.MapperUtil;
import com.omegafrog.My.piano.app.utils.exception.payment.CashOrderConfirmFailedException;
import com.omegafrog.My.piano.app.utils.exception.payment.TossAPIException;
import com.omegafrog.My.piano.app.web.domain.cash.CashOrder;
import lombok.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@RequiredArgsConstructor
public class TossPaymentInstance {

    private final RestTemplate restTemplate;

    @Value("${payment.toss.client-key}")
    private String clientKey;

    @Value("${payment.toss.secret-key}")
    private String secretKey;

    @Value("${payment.toss.api-url}")
    private String baseURL;

    private final MapperUtil mapperUtil;

    @Data
    @NoArgsConstructor
    @Setter
    @Getter
    @AllArgsConstructor
    private static class RequestBody {
        private String paymentKey;
        private String orderId;
        private Integer amount;
    }

    public Payment requestCashOrder(CashOrder order) throws JsonProcessingException, CashOrderConfirmFailedException {

        RequestBody requestBody = new RequestBody(order.getPaymentKey(), order.getOrderId(), order.getAmount());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(secretKey, "");

        HttpEntity<RequestBody> entity = new HttpEntity<>(requestBody, headers);


        try{
            ResponseEntity<String> exchange = restTemplate.exchange(baseURL + "/confirm", HttpMethod.POST, entity, String.class);
            if (exchange.getStatusCode().is2xxSuccessful())
                // DONE
                return mapperUtil.parsePayment(exchange.getBody());
            else throw new HttpClientErrorException(exchange.getStatusCode());
        }catch (HttpClientErrorException e){
            if(e.getStatusCode().is4xxClientError()){
                // ABORTED
                TossError tossError = mapperUtil.parseTossError(e.getLocalizedMessage());
                throw new TossAPIException(tossError.message());
            }else{
                // ABORTED
                throw new CashOrderConfirmFailedException("현금 결제 승인 서버에 요청 전송 실패.");
            }
        }
    }
}
