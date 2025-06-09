package com.tektechno.payout.service;

import com.tektechno.payout.dto.request.AddBeneficiaryRequestDto;
import com.tektechno.payout.dto.request.SendMoneyRequestDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface PayoutService {

  public ResponseEntity<?> getBeneType();

  public ResponseEntity<?> getPayReason();

  public ResponseEntity<?> addBeneficiary(AddBeneficiaryRequestDto addBeneficiaryRequestDto);

  public ResponseEntity<?> updateBeneficiary(String beneficiaryIfscCode, String beneficiaryId);

  public ResponseEntity<?> getBeneficiaryDetails(String beneficiaryMobileNumber);

  public ResponseEntity<?> sendMoney(SendMoneyRequestDto sendMoneyRequestDto);

  public ResponseEntity<?> getTransactionDetails(String beneficiaryId, int pageNumber, int pageSize);

  public ResponseEntity<?> checkStatus(String orderId);

  public ResponseEntity<?> getBeneficiaryList(int pageNumber, int pageSize);

  public ResponseEntity<?> getAllPayoutTransaction(int pageNumber, int pageSize);

  public ResponseEntity<?> uploadBulkBeneficiary( MultipartFile file);


}
