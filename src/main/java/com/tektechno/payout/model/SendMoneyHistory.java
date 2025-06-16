package com.tektechno.payout.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

@Entity
@Getter
@Setter
@Table(
    name = "send_money_history",
    schema = "tektechno",
    indexes = {
        @Index(name = "idx_send_money_history_created_at", columnList = "created_at"),
        @Index(name = "idx_send_money_history_beneficiary_id", columnList = "beneficiary_id"),
        @Index(name = "idx_send_money_history_order_id", columnList = "order_id")
    })
public class SendMoneyHistory {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "id", nullable = false)
  private Long id;

  @Column(name = "status")
  private String status;

  @Column(name = "beneficiary_id", nullable = false)
  private String beneficiaryId;

  @Column(name = "order_id", nullable = false)
  private String orderId;

  @Column(name = "cyrus_order_id")
  private String cyrusOrderId;

  @Column(name = "cyrus_id")
  private String cyrusId;

  @Column(name = "rrn_number")
  private String rrnNumber;

  @Column(name = "opening_balance")
  private String openingBalance;

  @Column(name = "locked_amount")
  private String lockedAmount;

  @Column(name = "charged_amount")
  private String chargedAmount;

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
