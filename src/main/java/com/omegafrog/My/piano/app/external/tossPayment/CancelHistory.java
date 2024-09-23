package com.omegafrog.My.piano.app.external.tossPayment;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class CancelHistory {
    private double cancelAmount;
    private String cancelReason;
    private double taxFreeAmount;
    private int taxExemptionAmount;
    private double refundableAmount;
    private double easyPayDiscountAmount;
    private String canceledAt;
    private String transactionKey;
    private String receiptKey;
    private String cancelStatus;
    private String cancelRequestId;
}
