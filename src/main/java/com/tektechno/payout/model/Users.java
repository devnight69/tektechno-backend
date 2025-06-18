package com.tektechno.payout.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tektechno.payout.enums.UserType;
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
    name = "users",
    schema = "tektechno",
    indexes = {
        @Index(name = "idx_users_created_at", columnList = "created_at"),
        @Index(name = "idx_users_mobile_number", columnList = "mobile_number"),
        @Index(name = "idx_users_email", columnList = "email"),
        @Index(name = "idx_users_member_id", columnList = "member_id"),
    })
public class Users {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  @JsonIgnore
  private Long id;

  @Column(name = "member_id")
  private String memberId;

  @Column(name = "full_name", nullable = false)
  private String fullName;

  @Column(name = "email", nullable = false, unique = true)
  private String email;

  @Column(name = "mobile_number", nullable = false, unique = true)
  private String mobileNumber;

  @Column(name = "password", nullable = false)
  private String password;

  @Column(name = "user_type", nullable = false)
  private UserType userType;

  @Column(name = "status", nullable = false)
  private boolean status;

  @Column(name = "created_at", nullable = false)
  private Date createdAt;

  @Column(name = "updated_at", nullable = false)
  private Date updatedAt;

  @PrePersist
  private void beforeInsert() {
    this.setStatus(true);
    this.setCreatedAt(new Date());
    this.setUpdatedAt(new Date());
  }

  @PreUpdate
  private void beforeUpdate() {
    this.setUpdatedAt(new Date());
  }

}
