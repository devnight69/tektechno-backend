package com.tektechno.payout.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tektechno.payout.enums.BulkPaymentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

/**
 * This is a bulk payment transaction history entity.
 *
 * @author Kousik Manik
 */
@Entity
@Getter
@Setter
@Table(
    name = "bulk_payment_transaction_history",
    schema = "tektechno",
    indexes = {
        @Index(name = "idx_bulk_payment_transaction_history_created_at", columnList = "created_at"),
        @Index(name = "idx_bulk_payment_transaction_history_transaction_id", columnList = "transaction_id"),
        @Index(name = "idx_bulk_payment_transaction_history_member_id", columnList = "member_id"),
        @Index(name = "idx_bulk_payment_transaction_history_status", columnList = "status")
    })
public class BulkPaymentTransactionHistory {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "id", nullable = false)
  @JsonIgnore
  private Long id;

  @Column(name = "member_id", nullable = false)
  private String memberId;

  @Column(name = "transaction_id", nullable = false)
  private String transactionId;

  @Column(name = "beneficiary_id", nullable = false)
  private Long beneficiaryId;

  @Column(name = "transaction_type", nullable = false)
  private String transactionType;

  @Column(name = "beneficiary_cyrus_id", nullable = false)
  private String beneficiaryCyrusId;

  @Column(name = "beneficiary_name", nullable = false)
  private String beneficiaryName;

  @Column(name = "beneficiary_mobile_number", nullable = false)
  private String beneficiaryMobileNumber;

  @Column(name = "status", nullable = false)
  @Enumerated(EnumType.STRING)
  private BulkPaymentStatus status;

  @Column(name = "comment", nullable = false)
  private String comment;

  @Column(name = "remarks", nullable = false)
  private String remarks;

  @Column(name = "amount", nullable = false)
  private Long amount;

  @Column(name = "created_at", nullable = false)
  private Date createdAt;

  @Column(name = "updated_at", nullable = false)
  private Date updatedAt;

  @PrePersist
  private void beforeInsert() {
    this.setCreatedAt(new Date());
    this.setUpdatedAt(new Date());
  }

  @PreUpdate
  private void beforeUpdate() {
    this.setUpdatedAt(new Date());
  }

}
