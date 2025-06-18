package com.tektechno.payout.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    name = "beneficiary",
    schema = "tektechno",
    indexes = {
        @Index(name = "idx_beneficiary_created_at", columnList = "created_at"),
        @Index(name = "idx_beneficiary_bene_type", columnList = "bene_type"),
        @Index(name = "idx_beneficiary_status", columnList = "status"),
        @Index(name = "idx_beneficiary_beneficiary_id", columnList = "beneficiary_id"),
        @Index(name = "idx_beneficiary_member_id", columnList = "member_id"),
    })
public class Beneficiary {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "id", nullable = false)
  @JsonIgnore
  private Long id;

  @Column(name = "member_id")
  private String memberId;

  @Column(name = "beneficiary_id", nullable = false, unique = true)
  private String beneficiaryId;

  @Column(name = "bene_type", nullable = false)
  private String beneType;

  @Column(name = "account_number", nullable = false, unique = true)
  private String beneficiaryBankAccountNumber;

  @Column(name = "ifsc_code", nullable = false)
  private String beneficiaryBankIfscCode;

  @Column(name = "beneficiary_name", nullable = false)
  private String beneficiaryName;

  @Column(name = "beneficiary_bank_name", nullable = false)
  private String beneficiaryBankName;

  @Column(name = "beneficiary_email")
  private String beneficiaryEmail;

  @Column(name = "beneficiary_mobile_number")
  private String beneficiaryMobileNumber;

  @Column(name = "beneficiary_pan")
  private String beneficiaryPan;

  @Column(name = "beneficiary_aadhaar")
  private String beneficiaryAadhaar;

  @Column(name = "beneficiary_address", columnDefinition = "TEXT")
  private String beneficiaryAddress;

  @Column(name = "latitude", nullable = false)
  private Long latitude;

  @Column(name = "longitude", nullable = false)
  private Long longitude;

  @Column(name = "status", nullable = false)
  private boolean status;

  @Column(name = "created_at", nullable = false)
  private Date createdAt;

  @Column(name = "updated_at", nullable = false)
  private Date updatedAt;

  @PrePersist
  private void beforeInsert() {
    this.setCreatedAt(new Date());
    this.setUpdatedAt(new Date());
    this.setStatus(true);
  }

  @PreUpdate
  private void beforeUpdate() {
    this.setUpdatedAt(new Date());
  }

}
