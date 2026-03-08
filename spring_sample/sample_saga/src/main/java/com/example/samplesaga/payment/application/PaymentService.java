package com.example.samplesaga.payment.application;

import com.example.samplesaga.order.domain.SagaOrder;
import com.example.samplesaga.payment.domain.PaymentRecord;
import com.example.samplesaga.payment.domain.PaymentRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 결제 승인과 보상 취소를 담당하는 서비스다.
 */
@Service
public class PaymentService {

    private final PaymentRecordRepository paymentRecordRepository;

    public PaymentService(PaymentRecordRepository paymentRecordRepository) {
        this.paymentRecordRepository = paymentRecordRepository;
    }

    /**
     * 주문 금액에 대한 결제를 승인한다.
     *
     * @param order Saga 주문
     */
    @Transactional(noRollbackFor = IllegalStateException.class)
    public void approvePayment(SagaOrder order) {
        if ("FAIL-PAYMENT".equalsIgnoreCase(order.getCustomerId())) {
            throw new IllegalStateException("결제 승인이 거절되었습니다.");
        }

        paymentRecordRepository.save(new PaymentRecord(order.getId(), order.getCustomerId(), order.getTotalAmount()));
    }

    /**
     * 이미 승인된 결제를 보상 취소한다.
     *
     * @param orderId 주문 식별자
     */
    @Transactional
    public void cancelPayment(String orderId) {
        PaymentRecord paymentRecord = paymentRecordRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalStateException("취소할 결제 정보가 없습니다. orderId=" + orderId));
        paymentRecord.cancel();
    }
}
