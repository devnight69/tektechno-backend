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
 * This Is a bulk payment history Entity class.
 *
 * @author Kousik Manik
 */
@Entity
@Getter
@Setter
@Table(
    name = "bulk_payment_history",
    schema = "tektechno",
    indexes = {
        @Index(name = "idx_bulk_payment_history_created_at", columnList = "created_at"),
        @Index(name = "idx_bulk_payment_history_transaction_id", columnList = "transaction_id"),
        @Index(name = "idx_bulk_payment_history_member_id", columnList = "member_id"),
        @Index(name = "idx_bulk_payment_history_status", columnList = "status"),
    })
public class BulkPaymentHistory {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "id", nullable = false)
  @JsonIgnore
  private Long id;

  @Column(name = "member_id", nullable = false)
  private String memberId;

  @Column(name = "transaction_id", nullable = false, unique = true)
  private String transactionId;

  @Column(name = "status", nullable = false)
  @Enumerated(EnumType.STRING)
  private BulkPaymentStatus status;

  @Column(name = "created_at", nullable = false)
  private Date createdAt;

  @Column(name = "updated_at", nullable = false)
  private Date updatedAt;

  @PrePersist
  private void beforeInsert() {
    this.setCreatedAt(new Date());
    this.setUpdatedAt(new Date());
    this.setStatus(BulkPaymentStatus.PENDING);
  }

  @PreUpdate
  private void beforeUpdate() {
    this.setUpdatedAt(new Date());
  }

}
