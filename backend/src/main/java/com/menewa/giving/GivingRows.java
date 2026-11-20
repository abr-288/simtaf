package com.menewa.giving;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;
import java.time.OffsetDateTime;

/** Lignes des tables du module (mappées par JdbcClient, sérialisées telles quelles sauf champs internes). */
final class GivingRows {
    private GivingRows() {}

    static final String REQUEST_COLS = """
            id, ref, user_id, kind, title, description, category, target_name, target_zone, beneficiary_id, partner_id, status,
            rejection_reason, info_request, amount_per_occurrence, occurrences, total_amount, payment_mode, recurrence, start_date,
            annex_services, annex_cost, execution_budget, other_fees, created_at, updated_at, completed_at, closed_at""";

    record RequestRow(Long id, String ref, Long userId, String kind, String title, String description, String category,
            String targetName, String targetZone, Long beneficiaryId, Long partnerId, String status, String rejectionReason,
            String infoRequest, int amountPerOccurrence, int occurrences, int totalAmount, String paymentMode, String recurrence,
            OffsetDateTime startDate, @JsonRawValue String annexServices, int annexCost, int executionBudget, int otherFees,
            OffsetDateTime createdAt, OffsetDateTime updatedAt, OffsetDateTime completedAt, OffsetDateTime closedAt) {}

    record ItemRow(Long id, Long requestId, Long catalogItemId, String label, int quantity, int unitPrice, int costEstimate, String target) {}

    record HistoryRow(Long id, Long requestId, String status, String note, Long actorId, OffsetDateTime createdAt) {}

    record InstallmentRow(Long id, Long requestId, int seq, OffsetDateTime dueDate, int amount, String status, OffsetDateTime notifiedAt) {}

    static final String MISSION_COLS = """
            id, request_id, provider_id, title, instructions, status, refusal_reason, due_date, provider_fee, compensation_mode,
            exchange_description, exchange_status, exception_reason, reassigned_from, created_by, created_at, updated_at, completed_at, validated_at""";

    record MissionRow(Long id, Long requestId, Long providerId, String title, String instructions, String status, String refusalReason,
            OffsetDateTime dueDate, int providerFee, String compensationMode, String exchangeDescription, String exchangeStatus,
            String exceptionReason, Long reassignedFrom, Long createdBy, OffsetDateTime createdAt, OffsetDateTime updatedAt,
            OffsetDateTime completedAt, OffsetDateTime validatedAt) {
        boolean open() { return "proposed".equals(status) || "accepted".equals(status) || "in_progress".equals(status); }
        @JsonProperty("late") boolean late() { return dueDate != null && dueDate.isBefore(OffsetDateTime.now()) && open(); }
    }

    static final String PROOF_COLS = """
            id, request_id, mission_id, type, file_key, file_name, mime, size, caption, uploaded_by, status, rejection_reason,
            reviewed_by, reviewed_at, visible_to_donor, created_at""";

    record ProofRow(Long id, Long requestId, Long missionId, String type, @JsonIgnore String fileKey, String fileName, String mime, long size,
            String caption, Long uploadedBy, String status, String rejectionReason, Long reviewedBy, OffsetDateTime reviewedAt,
            boolean visibleToDonor, OffsetDateTime createdAt) {}

    static final String PAYMENT_COLS = """
            id, request_id, mission_id, service_order_id, installment_id, user_id, direction, kind, mode, method, amount_planned,
            amount_validated, amount_paid, status, reference, receipt_key, receipt_name, receipt_mime, exchange_description,
            exchange_status, due_date, note, created_at, paid_at""";

    record PaymentRow(Long id, Long requestId, Long missionId, Long serviceOrderId, Long installmentId, Long userId, String direction,
            String kind, String mode, String method, int amountPlanned, Integer amountValidated, int amountPaid, String status,
            String reference, @JsonIgnore String receiptKey, String receiptName, @JsonIgnore String receiptMime,
            String exchangeDescription, String exchangeStatus, OffsetDateTime dueDate, String note, OffsetDateTime createdAt,
            OffsetDateTime paidAt) {
        @JsonProperty("hasReceipt") boolean hasReceipt() { return receiptKey != null; }
        @JsonProperty("remaining") int remaining() {
            return "exchange".equals(mode) ? 0 : Math.max((amountValidated == null ? amountPlanned : amountValidated) - amountPaid, 0);
        }
    }

    static final String DISPUTE_COLS = """
            id, ref, request_id, mission_id, opened_by, against_user_id, subject, description, status, responsible_id, decision, created_at, closed_at""";

    record DisputeRow(Long id, String ref, Long requestId, Long missionId, Long openedBy, Long againstUserId, String subject,
            String description, String status, Long responsibleId, String decision, OffsetDateTime createdAt, OffsetDateTime closedAt) {}

    record DisputeMessageRow(Long id, Long disputeId, Long authorId, String body, @JsonIgnore String attachmentKey,
            String attachmentName, @JsonIgnore String attachmentMime, OffsetDateTime createdAt) {}

    record BeneficiaryRow(Long id, String fullName, String phone, String zone, String address, String needs, String notes,
            boolean consent, Long userId, Long createdBy, OffsetDateTime archivedAt, OffsetDateTime createdAt) {}

    record CartRow(Long id, Long userId, Long catalogItemId, int quantity, String target, OffsetDateTime createdAt) {}
}
