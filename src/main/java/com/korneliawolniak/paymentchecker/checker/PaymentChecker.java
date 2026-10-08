package com.korneliawolniak.paymentchecker.checker;

import com.korneliawolniak.paymentchecker.kafka.PaymentValidationResultPublisher;
import com.korneliawolniak.paymentprocessing.avro.PaymentValidationRequest;
import com.korneliawolniak.paymentprocessing.avro.PaymentValidationResult;
import com.korneliawolniak.paymentprocessing.validation.BankAccountValidator;
import com.korneliawolniak.paymentprocessing.validation.ValidationReason;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentChecker {
  private static final Logger log = LoggerFactory.getLogger(PaymentChecker.class);
  private final PaymentValidationResultPublisher publisher;

  public PaymentChecker(PaymentValidationResultPublisher publisher) {
    this.publisher = publisher;
  }

  @KafkaListener(topics = "payment-validation-request", groupId = "payment-checker")
  public void handle(PaymentValidationRequest request) {
    List<CharSequence> reasons = new ArrayList<>();
    if (request.getDebtorName() == null || request.getDebtorName().toString().isBlank())
      reasons.add(ValidationReason.DEBTOR_NAME_REQUIRED.name());
    if (!BankAccountValidator.isValid(
        request.getDebtorAccountNumber() == null
            ? null
            : request.getDebtorAccountNumber().toString()))
      reasons.add(ValidationReason.DEBTOR_ACCOUNT_INVALID.name());
    if (request.getTransactionCount() < 1 || request.getTransactionCount() > 5)
      reasons.add(ValidationReason.TRANSACTION_COUNT_OUT_OF_RANGE.name());

    String status = reasons.isEmpty() ? "OK" : "NOT_OK";
    PaymentValidationResult result =
        PaymentValidationResult.newBuilder()
            .setPaymentId(request.getPaymentId())
            .setStatus(status)
            .setReasonCodes(reasons)
            .build();
    publisher.publish(result);
    log.info("Payment {} validation result: {} ({})", request.getPaymentId(), status, reasons);
  }
}
