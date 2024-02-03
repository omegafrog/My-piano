package com.omegafrog.My.piano.app.external.tossPayment;

public record Payment(String paymentKey, String type, String orderId, String orderName, String mId, String currency,
                      String method, Long totalAmount, String status) {
    @Override
    public String toString() {
        return "Payment{" +
                "paymentKey='" + paymentKey + '\'' +
                ", type='" + type + '\'' +
                ", orderId='" + orderId + '\'' +
                ", orderName='" + orderName + '\'' +
                ", mId='" + mId + '\'' +
                ", currency='" + currency + '\'' +
                ", method='" + method + '\'' +
                ", totalAmount=" + totalAmount +
                ", status='" + status + '\'' +
                '}';
    }
}
