package com.tektechno.payout.controller;

import com.tektechno.payout.dto.request.AddBeneficiaryRequestDto;
import com.tektechno.payout.dto.request.AddBulkBeneficiaryRequestDto;
import com.tektechno.payout.dto.request.SendMoneyRequestDto;
import com.tektechno.payout.service.PayoutService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/payout")
@Validated
public class PayoutController {

  @Autowired
  private PayoutService payoutService;

  @GetMapping("/beneficiary-type")
  public ResponseEntity<?> getBeneType() {
    return payoutService.getBeneType();
  }

  @GetMapping("/pay-reason")
  public ResponseEntity<?> getPayReason() {
    return payoutService.getPayReason();
  }

  @PostMapping("/add/beneficiary")
  public ResponseEntity<?> addBeneficiary(@Valid @RequestBody AddBeneficiaryRequestDto addBeneficiaryRequestDto) {
    return payoutService.addBeneficiary(addBeneficiaryRequestDto);
  }

  @PostMapping("/update/beneficiary")
  public ResponseEntity<?> updateBeneficiary(@RequestParam String beneficiaryIfscCode,
                                             @RequestParam String beneficiaryId) {
    return payoutService.updateBeneficiary(beneficiaryIfscCode, beneficiaryId);
  }

  @GetMapping("/beneficiary-details")
  public ResponseEntity<?> getBeneficiaryDetails(@RequestParam String beneficiaryMobileNumber) {
    return payoutService.getBeneficiaryDetails(beneficiaryMobileNumber);
  }

  @PostMapping("/send-money")
  public ResponseEntity<?> sendMoney(@Valid @RequestBody SendMoneyRequestDto sendMoneyRequestDto) {
    return payoutService.sendMoney(sendMoneyRequestDto);
  }

  @GetMapping("/transaction-details")
  public ResponseEntity<?> getTransactionDetails(@RequestParam String beneficiaryId,
                                                 @RequestParam int pageNumber,
                                                 @RequestParam int pageSize) {
    return payoutService.getTransactionDetails(beneficiaryId, pageNumber, pageSize);
  }

  @GetMapping("/check-status")
  public ResponseEntity<?> checkStatus(@RequestParam String orderId) {
    return payoutService.checkStatus(orderId);
  }

  @GetMapping("/beneficiary-list")
  public ResponseEntity<?> getBeneficiaryList(@RequestParam int pageNumber,
                                              @RequestParam int pageSize) {
    return payoutService.getBeneficiaryList(pageNumber, pageSize);
  }

  @GetMapping("/all-payout-transaction")
  public ResponseEntity<?> getAllPayoutTransaction(@RequestParam int pageNumber,
                                                   @RequestParam int pageSize) {
    return payoutService.getAllPayoutTransaction(pageNumber, pageSize);
  }

  @PostMapping("/beneficiaries/bulk-upload")
  public ResponseEntity<?> uploadBulkBeneficiary(
      @RequestPart("file") MultipartFile file,
      @Valid @RequestPart("data") AddBulkBeneficiaryRequestDto addBulkBeneficiaryRequestDto) {
    return payoutService.uploadBulkBeneficiary(file, addBulkBeneficiaryRequestDto);
  }


}