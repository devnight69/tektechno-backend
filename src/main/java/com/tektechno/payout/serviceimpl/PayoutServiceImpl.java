package com.tektechno.payout.serviceimpl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tektechno.payout.constant.CyrusApiConstant;
import com.tektechno.payout.dto.request.AddBeneficiaryRequestDto;
import com.tektechno.payout.dto.request.AddBulkBeneficiaryRequestDto;
import com.tektechno.payout.dto.request.SendMoneyRequestDto;
import com.tektechno.payout.dto.response.AddBeneficiaryResponseDto;
import com.tektechno.payout.dto.response.BeneficiaryDetailsDto;
import com.tektechno.payout.dto.response.SendMoneyHistoryResponseDto;
import com.tektechno.payout.dto.response.SendMoneyResponseDto;
import com.tektechno.payout.enums.BulkPaymentStatus;
import com.tektechno.payout.model.Beneficiary;
import com.tektechno.payout.model.BulkPaymentHistory;
import com.tektechno.payout.model.BulkPaymentTransactionHistory;
import com.tektechno.payout.model.SendMoneyHistory;
import com.tektechno.payout.model.WalletBalance;
import com.tektechno.payout.projection.BeneficiaryIdNameProjection;
import com.tektechno.payout.repository.BeneficiaryRepository;
import com.tektechno.payout.repository.BulkPaymentHistoryRepo;
import com.tektechno.payout.repository.BulkPaymentTransactionHistoryRepo;
import com.tektechno.payout.repository.SendMoneyHistoryRepo;
import com.tektechno.payout.repository.WalletBalanceRepository;
import com.tektechno.payout.response.BaseResponse;
import com.tektechno.payout.service.PayoutService;
import com.tektechno.payout.utilities.ExcelHelper;
import com.tektechno.payout.utilities.StringUtils;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Service
public class PayoutServiceImpl implements PayoutService {

  @Value("${cyrus-api-member-id}")
  private String cyrusApiMemberId;

  @Value("${cyrus-payout-api-key}")
  private String cyrusPayoutApiKey;

  @Value("${cyrus-recharge-api-endpoint}")
  private String cyrusRechargeApiEndpoint;

  @Autowired
  private BaseResponse baseResponse;

  @Autowired
  private BeneficiaryRepository beneficiaryRepository;

  @Autowired
  private SendMoneyHistoryRepo sendMoneyHistoryRepo;

  @Autowired
  private WalletBalanceRepository walletBalanceRepository;

  @Autowired
  private BulkPaymentHistoryRepo bulkPaymentHistoryRepo;

  @Autowired
  private BulkPaymentTransactionHistoryRepo bulkPaymentTransactionHistoryRepo;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private RestTemplate restTemplate;

  private static final Logger logger = LoggerFactory.getLogger(PayoutServiceImpl.class);

  @Override
  public ResponseEntity<?> getBeneType() {
    try {
      String url = cyrusRechargeApiEndpoint + CyrusApiConstant.ADD_BENEFICIARY_URL;
      logger.info("Calling Cyrus BENE_TYPE API at URL: {}", url);

      MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
      formData.add("MerchantID", cyrusApiMemberId);
      formData.add("MerchantKey", cyrusPayoutApiKey);
      formData.add("MethodName", "BENE_TYPE");

      logger.debug("Form Data: {}", formData);

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.MULTIPART_FORM_DATA);

      HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(formData, headers);

      logger.info("Sending POST request to Cyrus API...");

      ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);

      logger.info("Received response with status code: {}", response.getStatusCode());
      logger.debug("Raw Response Body: {}", response.getBody());

      Object resp = objectMapper.readValue(response.getBody(), Object.class);

      return baseResponse.successResponse(resp);

    } catch (Exception e) {
      logger.error("Error occurred while calling Cyrus BENE_TYPE API", e);
      return baseResponse.errorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
          "Something went wrong while fetching beneficiary types");
    }
  }

  @Override
  public ResponseEntity<?> getPayReason() {
    try {
      String url = cyrusRechargeApiEndpoint + CyrusApiConstant.ADD_BENEFICIARY_URL;
      logger.info("Calling Cyrus PAY_REASON API at URL: {}", url);

      // Prepare form data
      MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
      formData.add("MerchantID", cyrusApiMemberId);
      formData.add("MerchantKey", cyrusPayoutApiKey);
      formData.add("MethodName", "PAY_REASON");

      logger.debug("Prepared form data for PAY_REASON: {}", formData);

      // Set headers
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.MULTIPART_FORM_DATA);

      HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(formData, headers);

      logger.info("Sending POST request to Cyrus PAY_REASON API...");

      ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);

      logger.info("Received response from PAY_REASON API with status code: {}", response.getStatusCode());
      logger.debug("PAY_REASON API raw response body: {}", response.getBody());

      Object resp = objectMapper.readValue(response.getBody(), Object.class);

      return baseResponse.successResponse(resp);

    } catch (Exception e) {
      logger.error("Exception occurred while calling Cyrus PAY_REASON API", e);
      return baseResponse.errorResponse(
          HttpStatus.INTERNAL_SERVER_ERROR,
          "Something went wrong while fetching pay reasons from Cyrus"
      );
    }
  }


  @Transactional
  @Override
  public ResponseEntity<?> addBeneficiary(AddBeneficiaryRequestDto requestDto) {
    String url = cyrusRechargeApiEndpoint + CyrusApiConstant.ADD_BENEFICIARY_URL;
    logger.info("Initiating Add Beneficiary process. Endpoint: {}", url);

    try {

      String address = objectMapper.writeValueAsString(requestDto.getAddress());

      // Prepare form data
      MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
      formData.add("MerchantID", cyrusApiMemberId);
      formData.add("MerchantKey", cyrusPayoutApiKey);
      formData.add("MethodName", "GET_BENEFICIARY");
      formData.add("pay_type", "account_number");
      formData.add("beneficiary_bank_account_number", requestDto.getBeneficiaryAccountNumber());
      formData.add("beneficiary_bank_ifsc_code", requestDto.getBeneficiaryIfscCode());
      formData.add("beneficiary_name", requestDto.getBeneficiaryName());
      formData.add("beneficiary_email", requestDto.getBeneficiaryEmail());
      formData.add("beneficiary_phone", requestDto.getBeneficiaryMobileNumber());
      formData.add("beneficiary_pan", requestDto.getBeneficiaryPanNumber());
      formData.add("beneficiary_aadhar", requestDto.getBeneficiaryAadhaarNumber());
      formData.add("is_agreement_with_beneficiary", "YES");
      formData.add("beneficiary_verification_status", "YES");
      formData.add("beneficiary_address", address);
      formData.add("bene_type", requestDto.getBeneType());
      formData.add("latlong", requestDto.getLatitude() + "," + requestDto.getLongitude());
      // Set headers
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.MULTIPART_FORM_DATA);
      HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(formData, headers);

      logger.info("Sending request to Cyrus API with payload: {}", formData);

      // Make API call
      ResponseEntity<String> apiResponse = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
      logger.info("Received response from Cyrus API: {}", apiResponse.getBody());

      // Convert response
      AddBeneficiaryResponseDto responseDto = objectMapper.readValue(apiResponse.getBody(), AddBeneficiaryResponseDto.class);

      // Process response
      if ("SUCCESS".equalsIgnoreCase(responseDto.getData().getStatus())) {
        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setMemberId(cyrusApiMemberId);
        beneficiary.setBeneficiaryId(responseDto.getData().getBeneficiaryId());
        beneficiary.setBeneType(requestDto.getBeneType());
        beneficiary.setBeneficiaryBankAccountNumber(requestDto.getBeneficiaryAccountNumber());
        beneficiary.setBeneficiaryBankIfscCode(requestDto.getBeneficiaryIfscCode()); // Validate name != IFSC
        beneficiary.setBeneficiaryBankName(requestDto.getBeneficiaryBankName());
        beneficiary.setBeneficiaryName(requestDto.getBeneficiaryName());
        beneficiary.setBeneficiaryEmail(requestDto.getBeneficiaryEmail());
        beneficiary.setBeneficiaryMobileNumber(requestDto.getBeneficiaryMobileNumber());
        beneficiary.setBeneficiaryPan(requestDto.getBeneficiaryPanNumber());
        beneficiary.setBeneficiaryAadhaar(requestDto.getBeneficiaryAadhaarNumber());
        beneficiary.setBeneficiaryAddress(address);
        beneficiary.setLatitude(requestDto.getLatitude());
        beneficiary.setLongitude(requestDto.getLongitude());

        beneficiaryRepository.save(beneficiary);
        logger.info("Beneficiary saved to DB successfully. ID: {}", beneficiary.getBeneficiaryId());

        return baseResponse.successResponse(responseDto);
      }

      logger.warn("Failed to add beneficiary. Response: {}", responseDto);
      return baseResponse.errorResponse(HttpStatus.BAD_REQUEST, "Failed to add beneficiary");

    } catch (Exception e) {
      logger.error("Exception occurred while processing Add Beneficiary request", e);
      return baseResponse.errorResponse(
          HttpStatus.INTERNAL_SERVER_ERROR,
          "Internal error occurred while adding beneficiary"
      );
    }
  }

  @Override
  public ResponseEntity<?> updateBeneficiary(String beneficiaryIfscCode, String beneficiaryId) {
    String url = cyrusRechargeApiEndpoint + CyrusApiConstant.ADD_BENEFICIARY_URL;
    logger.info("Initiating update IFSC request for Beneficiary ID: {}", beneficiaryId);

    try {
      // Prepare form data
      MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
      formData.add("MerchantID", cyrusApiMemberId);
      formData.add("MerchantKey", cyrusPayoutApiKey);
      formData.add("MethodName", "UPDATE_IFSC");
      formData.add("beneficiary_bank_ifsc_code", beneficiaryIfscCode);
      formData.add("beneficiary_id", beneficiaryId);

      logger.debug("Form data for UPDATE_IFSC request: {}", formData);

      // Set headers
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.MULTIPART_FORM_DATA);
      HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(formData, headers);

      // Call external API
      ResponseEntity<String> apiResponse = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);

      // Deserialize response
      AddBeneficiaryResponseDto responseDto = objectMapper.readValue(apiResponse.getBody(), AddBeneficiaryResponseDto.class);

      if ("UPDATED".equalsIgnoreCase(responseDto.getData().getStatus())) {
        Optional<Beneficiary> optionalBeneficiary = beneficiaryRepository.findByBeneficiaryId(beneficiaryId);

        if (optionalBeneficiary.isPresent()) {
          Beneficiary beneficiary = optionalBeneficiary.get();
          beneficiary.setBeneficiaryBankIfscCode(beneficiaryIfscCode);
          beneficiaryRepository.save(beneficiary);
          logger.info("Beneficiary IFSC code updated successfully. ID: {}", beneficiaryId);
        } else {
          logger.warn("Beneficiary not found in DB for update. ID: {}", beneficiaryId);
          return baseResponse.errorResponse(HttpStatus.BAD_REQUEST,
              "Beneficiary not found with the provided ID");
        }
      } else {
        logger.warn("Failed to update IFSC. Response STATUS: {}", responseDto.getData().getStatus());
      }

      return baseResponse.successResponse(responseDto);

    } catch (Exception e) {
      logger.error("Exception occurred while updating beneficiary IFSC. ID: {}", beneficiaryId, e);
      return baseResponse.errorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
          "Internal error while updating beneficiary");
    }
  }

  @Override
  public ResponseEntity<?> getBeneficiaryDetails(String beneficiaryMobileNumber) {
    try {
      logger.info("Fetching beneficiary details for mobile number: {}", beneficiaryMobileNumber);

      String url = cyrusRechargeApiEndpoint + CyrusApiConstant.ADD_BENEFICIARY_URL;

      // Prepare form data
      MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
      formData.add("MerchantID", cyrusApiMemberId);
      formData.add("MerchantKey", cyrusPayoutApiKey);
      formData.add("MethodName", "BENEFICIARY_DETAILS");
      formData.add("beneficiary_phone", beneficiaryMobileNumber);

      // Set headers
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.MULTIPART_FORM_DATA);
      HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(formData, headers);

      // Call external API
      ResponseEntity<String> apiResponse = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);

      BeneficiaryDetailsDto response = objectMapper.readValue(apiResponse.getBody(), BeneficiaryDetailsDto.class);

      //      Beneficiary beneficiary = new Beneficiary();
      //      beneficiary.setBeneficiaryId(response.getData().getFirst().getBeneficiaryId());
      //      beneficiary.setBeneType(response.getData().getFirst().getBeneType());
      //      beneficiary.setBeneficiaryBankAccountNumber(response.getData().getFirst().getBankAccountNumber());
      //      beneficiary.setBeneficiaryBankIfscCode(response.getData().getFirst().getBankIfscCode()); // Validate name != IFSC
      //      beneficiary.setBeneficiaryBankName("");
      //      beneficiary.setBeneficiaryName(response.getData().getFirst().getAccountHolderName());
      //      beneficiary.setBeneficiaryEmail(response.getData().getFirst().getEmail());
      //      beneficiary.setBeneficiaryMobileNumber(response.getData().getFirst().getPhone());
      //      beneficiary.setBeneficiaryPan(response.getData().getFirst().getPan());
      //      beneficiary.setBeneficiaryAadhaar(response.getData().getFirst().getAadhar());
      //      String latLong = response.getData().getFirst().getLatLong();
      //      if (latLong != null && latLong.contains(",")) {
      //        String[] parts = latLong.split(",");
      //        if (parts.length == 2) {
      //          beneficiary.setLatitude(Long.valueOf(parts[0].trim()));
      //          beneficiary.setLongitude(Long.valueOf(parts[1].trim()));
      //        } else {
      //          // Handle unexpected format
      //          logger.warn("Invalid latLong format: {}", latLong);
      //        }
      //      } else {
      //        // Handle null or missing latLong
      //        logger.warn("latLong is null or does not contain ','");
      //      }
      //
      //      beneficiary.setBeneficiaryAddress(response.getData().getFirst().getBeneficiaryAddress());
      //      beneficiaryRepository.save(beneficiary);

      return baseResponse.successResponse(response);

    } catch (Exception e) {
      logger.error("Exception occurred while fetching beneficiary details for mobile number: {}", beneficiaryMobileNumber, e);
      return baseResponse.errorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
          "An unexpected error occurred while fetching beneficiary details");
    }
  }

  @Override
  @Transactional
  public ResponseEntity<?> sendMoney(SendMoneyRequestDto sendMoneyRequestDto) {
    String beneficiaryId = sendMoneyRequestDto.getBeneficiaryId();
    String url = cyrusRechargeApiEndpoint + CyrusApiConstant.SEND_MONEY_URL;
    String generatedOrderId = UUID.randomUUID().toString();

    try {
      logger.info("🚀 Initiating 'Send Money' for Beneficiary ID: {}", beneficiaryId);
      logger.debug("Cyrus API Endpoint: {}", url);

      // Prepare form data
      MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
      formData.add("MerchantID", cyrusApiMemberId);
      formData.add("MerchantKey", cyrusPayoutApiKey);
      formData.add("MethodName", "sendmoney");
      formData.add("orderId", generatedOrderId);
      formData.add("Name", sendMoneyRequestDto.getBeneficiaryName());
      formData.add("amount", String.valueOf(sendMoneyRequestDto.getAmount()));
      formData.add("MobileNo", sendMoneyRequestDto.getBeneficiaryMobileNumber());
      formData.add("comments", sendMoneyRequestDto.getComment());
      formData.add("TransferType", sendMoneyRequestDto.getTransferType());
      formData.add("beneficiaryid", beneficiaryId);
      formData.add("remarks", sendMoneyRequestDto.getRemarks());

      logger.debug("Form data for 'Send Money': {}", formData);

      // Set headers
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.MULTIPART_FORM_DATA);
      HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(formData, headers);

      // Call external API
      logger.info("📡 Sending request to Cyrus API...");
      ResponseEntity<String> apiResponse = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
      logger.info("✅ Received response: {}", apiResponse.getBody());

      // Deserialize response
      SendMoneyResponseDto responseDto = objectMapper.readValue(apiResponse.getBody(), SendMoneyResponseDto.class);

      if (StringUtils.isNotNullAndNotEmpty(responseDto.getData().getOrderId())) {
        logger.info("💸 Money sent successfully. Order ID: {}", responseDto.getData().getOrderId());

        // Save to history
        SendMoneyHistory sendMoneyHistory = new SendMoneyHistory();
        sendMoneyHistory.setMemberId(cyrusApiMemberId);
        sendMoneyHistory.setBeneficiaryId(beneficiaryId);
        sendMoneyHistory.setStatus(responseDto.getStatus());
        sendMoneyHistory.setOrderId(responseDto.getData().getOrderId());
        sendMoneyHistory.setCyrusOrderId(responseDto.getData().getCyrusOrderId());
        sendMoneyHistory.setCyrusId(responseDto.getData().getCyrus_id());
        sendMoneyHistory.setRrnNumber(responseDto.getData().getRrn());
        sendMoneyHistory.setOpeningBalance(responseDto.getData().getOpening_bal());
        sendMoneyHistory.setLockedAmount(responseDto.getData().getLocked_amt());
        sendMoneyHistory.setChargedAmount(responseDto.getData().getCharged_amt());

        sendMoneyHistoryRepo.save(sendMoneyHistory);

        if (walletBalanceRepository.count() == 0) {
          WalletBalance walletBalance = new WalletBalance();
          walletBalance.setMemberId(cyrusApiMemberId);
          walletBalance.setBalance(Double.parseDouble(responseDto.getData().getOpening_bal()));
          walletBalanceRepository.save(walletBalance);
        }

        logger.info("📝 Transaction details saved to history successfully.");
      } else {
        logger.warn("⚠️ 'Send Money' API response did not contain a valid Order ID. Full Response: {}", responseDto);
      }

      return baseResponse.successResponse(responseDto);

    } catch (Exception e) {
      logger.error("❌ Exception occurred while processing 'Send Money' request for Beneficiary ID: {}", beneficiaryId, e);
      return baseResponse.errorResponse(
          HttpStatus.INTERNAL_SERVER_ERROR,
          "An unexpected error occurred while processing send money request"
      );
    }
  }

  @Override
  public ResponseEntity<?> getTransactionDetails(String beneficiaryId, int pageNumber, int pageSize) {
    try {
      logger.info("📥 Request received to fetch transaction details for beneficiary ID: {}", beneficiaryId);

      // Validate and normalize pagination inputs
      if (pageNumber < 0) {
        logger.warn("⚠️ Page number {} is less than 0. Resetting to 0.", pageNumber);
        pageNumber = 0;
      }

      if (pageSize <= 0 || pageSize > 100) {
        logger.warn("⚠️ Invalid page size {}. Resetting to default size 10.", pageSize);
        pageSize = 10;
      }

      Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));

      logger.debug("Fetching data for Beneficiary ID: {}, Page: {}, Size: {}", beneficiaryId, pageNumber, pageSize);

      Page<SendMoneyHistory> sendMoneyHistories =
          sendMoneyHistoryRepo.findByBeneficiaryId(beneficiaryId.trim(), pageable);

      if (sendMoneyHistories.isEmpty()) {
        logger.info("ℹ️ No transaction records found for Beneficiary ID: {}", beneficiaryId);
        return baseResponse.successResponse("No transactions found.", List.of());
      }

      logger.info("✅ Fetched {} transaction(s) for Beneficiary ID: {}",
          sendMoneyHistories.getNumberOfElements(), beneficiaryId);

      Map<String, Object> response = new HashMap<>();
      response.put("transactions", sendMoneyHistories.getContent());
      response.put("totalPages", sendMoneyHistories.getTotalPages());
      response.put("totalElements", sendMoneyHistories.getTotalElements());
      return baseResponse.successResponse(response);

    } catch (Exception e) {
      logger.error("❌ Exception occurred while fetching transaction details for beneficiary ID: {}", beneficiaryId, e);
      return baseResponse.errorResponse(
          HttpStatus.INTERNAL_SERVER_ERROR,
          "An unexpected error occurred while fetching transaction details"
      );
    }
  }

  @Override
  public ResponseEntity<?> checkStatus(String orderId) {
    String url = cyrusRechargeApiEndpoint + CyrusApiConstant.SEND_MONEY_URL;

    try {
      logger.info("🔎 Initiating transaction status check for Order ID: {}", orderId);
      logger.debug("Cyrus API Endpoint: {}", url);

      // Prepare form data
      MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
      formData.add("MerchantID", cyrusApiMemberId);
      formData.add("MerchantKey", cyrusPayoutApiKey);
      formData.add("MethodName", "checkstatus");
      formData.add("orderId", orderId);
      logger.debug("Form data for status check: {}", formData);

      // Set headers
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.MULTIPART_FORM_DATA);
      HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(formData, headers);

      // Call external API
      logger.info("📡 Sending status check request to Cyrus API...");
      ResponseEntity<String> apiResponse = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
      logger.debug("✅ Received response for Order ID {}: {}", orderId, apiResponse.getBody());

      // Convert response to generic object or use specific DTO if available
      Object response = objectMapper.readValue(apiResponse.getBody(), Object.class);
      logger.info("ℹ️ Status check completed for Order ID: {}", orderId);

      return baseResponse.successResponse(response);

    } catch (Exception e) {
      logger.error("❌ Exception occurred while checking transaction status for Order ID: {}", orderId, e);
      return baseResponse.errorResponse(
          HttpStatus.INTERNAL_SERVER_ERROR,
          "An unexpected error occurred while fetching transaction details"
      );
    }
  }

  @Override
  public ResponseEntity<?> getBeneficiaryList(int pageNumber, int pageSize) {
    try {
      logger.info("📥 Request received to fetch beneficiary list. Page: {}, Size: {}", pageNumber, pageSize);

      // Validate pagination inputs
      if (pageNumber < 0) {
        logger.warn("⚠️ Invalid page number {} received. Resetting to 0.", pageNumber);
        pageNumber = 0;
      }

      if (pageSize <= 0 || pageSize > 100) {
        logger.warn("⚠️ Invalid page size {} received. Resetting to default 10.", pageSize);
        pageSize = 10;
      }

      Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));

      Page<Beneficiary> beneficiaries = beneficiaryRepository.findAll(pageable);

      if (beneficiaries.isEmpty()) {
        logger.info("ℹ️ No beneficiaries found for the given page parameters.");
        return baseResponse.successResponse("No beneficiaries found.", List.of());
      }

      logger.info("✅ Fetched {} beneficiary record(s).", beneficiaries.getNumberOfElements());

      Map<String, Object> response = new HashMap<>();
      response.put("beneficiaries", beneficiaries.getContent());
      response.put("totalPages", beneficiaries.getTotalPages());
      response.put("totalElements", beneficiaries.getTotalElements());

      return baseResponse.successResponse(response);

    } catch (Exception e) {
      logger.error("❌ Exception occurred while fetching beneficiary list", e);
      return baseResponse.errorResponse(
          HttpStatus.INTERNAL_SERVER_ERROR,
          "An unexpected error occurred while fetching beneficiary list"
      );
    }
  }

  @Override
  public ResponseEntity<?> getAllPayoutTransaction(int pageNumber, int pageSize) {
    try {

      logger.info("📥 Request received to fetch All Payment list. Page: {}, Size: {}", pageNumber, pageSize);

      // Validate pagination inputs
      if (pageNumber < 0) {
        logger.warn("⚠️ Invalid page number In All Payout {} received. Resetting to 0.", pageNumber);
        pageNumber = 0;
      }

      if (pageSize <= 0 || pageSize > 100) {
        logger.warn("⚠️ Invalid page size In All Payout {} received. Resetting to default 10.", pageSize);
        pageSize = 10;
      }

      Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));

      Page<SendMoneyHistory> sendMoneyHistories = sendMoneyHistoryRepo.findAll(pageable);

      if (sendMoneyHistories.isEmpty()) {
        logger.info("ℹ️ No Payout Transaction found for the given page parameters.");
        return baseResponse.successResponse("No beneficiaries found.", List.of());
      }
      logger.info("✅ Fetched {} Payout Transaction Details.", sendMoneyHistories.getNumberOfElements());
      Map<String, Object> response = new HashMap<>();
      response.put("transactions", createSendMoneyHistoryResponseDto(sendMoneyHistories.getContent()));
      response.put("totalPages", sendMoneyHistories.getTotalPages());
      response.put("totalElements", sendMoneyHistories.getTotalElements());
      return baseResponse.successResponse(response);
    } catch (Exception e) {
      return baseResponse.errorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
          "An unexpected error occurred while fetching transaction details");
    }
  }

  @Override
  public ResponseEntity<?> uploadBulkBeneficiary(MultipartFile file,
                                                 AddBulkBeneficiaryRequestDto addBulkBeneficiaryRequestDto) {
    try {
      if (file.isEmpty()) {
        return baseResponse.errorResponse(HttpStatus.BAD_REQUEST, "File is empty");
      }

      List<Map<String, String>> beneficiaries = ExcelHelper.readExcelFile(file);

      if (beneficiaries.isEmpty()) {
        return baseResponse.errorResponse(HttpStatus.BAD_REQUEST, "No beneficiaries found in the file");
      }

      saveBeneficiaryDetails(beneficiaries, addBulkBeneficiaryRequestDto);

      return baseResponse.successResponse("File uploaded successfully", beneficiaries);

    } catch (Exception e) {
      return baseResponse.errorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
          "An unexpected error occurred while uploading the file");
    }
  }

  @Transactional
  private void saveBeneficiaryDetails(List<Map<String, String>> beneficiaries,
                                      AddBulkBeneficiaryRequestDto addBulkBeneficiaryRequestDto) {
    try {
      // Generate unique transaction ID
      String transactionId;
      do {
        transactionId = UUID.randomUUID().toString();
      } while (bulkPaymentHistoryRepo.existsByTransactionId(transactionId));

      // Save bulk payment header
      BulkPaymentHistory bulkPaymentHistory = new BulkPaymentHistory();
      bulkPaymentHistory.setMemberId(cyrusApiMemberId);
      bulkPaymentHistory.setTransactionId(transactionId);
      bulkPaymentHistoryRepo.save(bulkPaymentHistory);

      List<BulkPaymentTransactionHistory> transactionHistoryList = new ArrayList<>();

      for (Map<String, String> beneficiary : beneficiaries) {
        String accountNumber = beneficiary.get("Beneficiary A/c No.");
        if (!StringUtils.isNotNullAndNotEmpty(accountNumber)) {
          logger.warn("Skipping: Beneficiary account number is empty.");
          continue;
        }

        long amount = parseAmount(beneficiary.get("Transaction Amount"));

        Optional<Beneficiary> optionalBeneficiary = beneficiaryRepository
            .findByBeneficiaryBankAccountNumberAndStatus(accountNumber, true);

        Long beneficiaryId;
        String beneficiaryCyrusId;
        String beneficiaryName;
        String beneficiaryMobileNumber;
        String comment = "Payout Of " + new Date(); // Common for all
        String remarks = "Vendor Payments";
        String transferType = getSafeValue(beneficiary, "Transaction Type", "IMPS");

        if (optionalBeneficiary.isPresent()) {
          Beneficiary existing = optionalBeneficiary.get();
          logger.warn("Beneficiary account number {} already exists. Using existing ID.", accountNumber);

          beneficiaryId = existing.getId();
          beneficiaryCyrusId = existing.getBeneficiaryId();
          beneficiaryName = existing.getBeneficiaryName();
          beneficiaryMobileNumber = existing.getBeneficiaryMobileNumber();
        } else {
          logger.info("Beneficiary account number {} not found. Creating new beneficiary...", accountNumber);

          AddBeneficiaryRequestDto dto = new AddBeneficiaryRequestDto();
          dto.setBeneficiaryAccountNumber(accountNumber);
          dto.setBeneficiaryName(getSafeValue(beneficiary, "Beneficiary Name"));
          dto.setBeneficiaryMobileNumber(getSafeValue(beneficiary, "Beneficiary Mobile No"));
          dto.setBeneficiaryEmail(getSafeValue(beneficiary, "Beneficiary Email ID", addBulkBeneficiaryRequestDto.getBeneficiaryEmail()));
          dto.setBeneficiaryIfscCode(getSafeValue(beneficiary, "IFSC Code"));
          dto.setBeneficiaryPanNumber(getSafeValue(beneficiary, "Pan No"));
          dto.setBeneficiaryAadhaarNumber(addBulkBeneficiaryRequestDto.getBeneficiaryAadhaarNumber());
          dto.setBeneficiaryBankName(addBulkBeneficiaryRequestDto.getBeneficiaryBankName());
          dto.setBeneType(addBulkBeneficiaryRequestDto.getBeneType());
          dto.setLatitude(addBulkBeneficiaryRequestDto.getLatitude());
          dto.setLongitude(addBulkBeneficiaryRequestDto.getLongitude());
          dto.setAddress(addBulkBeneficiaryRequestDto.getAddress());

          Beneficiary saved = addBeneficiaryForBulkUpload(dto);
          beneficiaryId = saved.getId();
          beneficiaryCyrusId = saved.getBeneficiaryId();
          beneficiaryName = saved.getBeneficiaryName();
          beneficiaryMobileNumber = saved.getBeneficiaryMobileNumber();
        }

        BulkPaymentTransactionHistory txHistory = new BulkPaymentTransactionHistory();
        txHistory.setTransactionId(transactionId);
        txHistory.setMemberId(cyrusApiMemberId);
        txHistory.setBeneficiaryId(beneficiaryId);
        txHistory.setBeneficiaryCyrusId(beneficiaryCyrusId);
        txHistory.setBeneficiaryName(beneficiaryName);
        txHistory.setBeneficiaryMobileNumber(beneficiaryMobileNumber);
        txHistory.setComment(comment);
        txHistory.setRemarks(remarks);
        txHistory.setTransactionType(transferType);
        txHistory.setAmount(amount);
        txHistory.setStatus(BulkPaymentStatus.PENDING);

        transactionHistoryList.add(txHistory);
      }

      if (!transactionHistoryList.isEmpty()) {
        bulkPaymentTransactionHistoryRepo.saveAll(transactionHistoryList);
      }
    } catch (Exception e) {
      logger.error("Error while saving bulk payment details: {}", e.getMessage(), e);
      throw e; // Rethrow to ensure transaction rollback
    }
  }

  private List<SendMoneyHistoryResponseDto> createSendMoneyHistoryResponseDto(List<SendMoneyHistory> sendMoneyHistories) {
    List<SendMoneyHistoryResponseDto> responseDtos = new ArrayList<>();

    // 1. Collect all beneficiary IDs
    Set<String> beneficiaryIds = sendMoneyHistories.stream()
        .map(SendMoneyHistory::getBeneficiaryId)
        .collect(Collectors.toSet());

    // 2. Fetch all names in one query into a Map
    Map<String, String> beneficiaryNameMap = findBeneficiaryNamesByBeneficiaryIds(beneficiaryIds);

    // 3. Map entities to DTOs
    for (SendMoneyHistory history : sendMoneyHistories) {
      SendMoneyHistoryResponseDto dto = objectMapper.convertValue(history, SendMoneyHistoryResponseDto.class);
      dto.setBeneficiaryName(beneficiaryNameMap.get(history.getBeneficiaryId()));
      responseDtos.add(dto);
    }

    return responseDtos;
  }

  private Map<String, String> findBeneficiaryNamesByBeneficiaryIds(Set<String> beneficiaryIds) {
    return beneficiaryRepository
        .findAllByBeneficiaryIdIn(beneficiaryIds).stream()
        .collect(Collectors.toMap(BeneficiaryIdNameProjection::getId, BeneficiaryIdNameProjection::getName));
  }

  @Transactional
  public Beneficiary addBeneficiaryForBulkUpload(AddBeneficiaryRequestDto requestDto) {
    String url = cyrusRechargeApiEndpoint + CyrusApiConstant.ADD_BENEFICIARY_URL;
    logger.info("Initiating Add Beneficiary process. Endpoint In Bulk Upload: {}", url);

    try {

      String address = objectMapper.writeValueAsString(requestDto.getAddress());

      // Prepare form data
      MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
      formData.add("MerchantID", cyrusApiMemberId);
      formData.add("MerchantKey", cyrusPayoutApiKey);
      formData.add("MethodName", "GET_BENEFICIARY");
      formData.add("pay_type", "account_number");
      formData.add("beneficiary_bank_account_number", requestDto.getBeneficiaryAccountNumber());
      formData.add("beneficiary_bank_ifsc_code", requestDto.getBeneficiaryIfscCode());
      formData.add("beneficiary_name", requestDto.getBeneficiaryName());
      formData.add("beneficiary_email", requestDto.getBeneficiaryEmail());
      formData.add("beneficiary_phone", requestDto.getBeneficiaryMobileNumber());
      formData.add("beneficiary_pan", requestDto.getBeneficiaryPanNumber());
      formData.add("beneficiary_aadhar", requestDto.getBeneficiaryAadhaarNumber());
      formData.add("is_agreement_with_beneficiary", "YES");
      formData.add("beneficiary_verification_status", "YES");
      formData.add("beneficiary_address", address);
      formData.add("bene_type", requestDto.getBeneType());
      formData.add("latlong", requestDto.getLatitude() + "," + requestDto.getLongitude());
      // Set headers
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.MULTIPART_FORM_DATA);
      HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(formData, headers);

      logger.info("Sending request to Cyrus API with payload In Bulk Upload: {}", formData);

      // Make API call
      ResponseEntity<String> apiResponse = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
      logger.info("Received response from Cyrus API In Bulk Upload: {}", apiResponse.getBody());

      // Convert response
      AddBeneficiaryResponseDto responseDto = objectMapper.readValue(apiResponse.getBody(), AddBeneficiaryResponseDto.class);

      // Process response
      if ("SUCCESS".equalsIgnoreCase(responseDto.getData().getStatus())) {
        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setMemberId(cyrusApiMemberId);
        beneficiary.setBeneficiaryId(responseDto.getData().getBeneficiaryId());
        beneficiary.setBeneType(requestDto.getBeneType());
        beneficiary.setBeneficiaryBankAccountNumber(requestDto.getBeneficiaryAccountNumber());
        beneficiary.setBeneficiaryBankIfscCode(requestDto.getBeneficiaryIfscCode()); // Validate name != IFSC
        beneficiary.setBeneficiaryBankName(requestDto.getBeneficiaryBankName());
        beneficiary.setBeneficiaryName(requestDto.getBeneficiaryName());
        beneficiary.setBeneficiaryEmail(requestDto.getBeneficiaryEmail());
        beneficiary.setBeneficiaryMobileNumber(requestDto.getBeneficiaryMobileNumber());
        beneficiary.setBeneficiaryPan(requestDto.getBeneficiaryPanNumber());
        beneficiary.setBeneficiaryAadhaar(requestDto.getBeneficiaryAadhaarNumber());
        beneficiary.setBeneficiaryAddress(address);
        beneficiary.setLatitude(requestDto.getLatitude());
        beneficiary.setLongitude(requestDto.getLongitude());

        beneficiary = beneficiaryRepository.save(beneficiary);
        logger.info("Beneficiary saved to DB successfully. ID: {}", beneficiary.getBeneficiaryId());

        return beneficiary;
      }

      logger.warn("Failed to add beneficiary. Response: {}", responseDto);
      return null;

    } catch (Exception e) {
      logger.error("Exception occurred while processing Add Beneficiary request", e);
      return null;
    }
  }

  private long parseAmount(String amountStr) {
    if (StringUtils.isNotNullAndNotEmpty(amountStr)) {
      try {
        return Long.parseLong(amountStr);
      } catch (NumberFormatException e) {
        logger.error("Invalid transaction amount: '{}'. Defaulting to 0.", amountStr);
      }
    }
    return 0L;
  }

  private String getSafeValue(Map<String, String> map, String key) {
    return getSafeValue(map, key, "");
  }

  private String getSafeValue(Map<String, String> map, String key, String defaultValue) {
    String value = map.get(key);
    return StringUtils.isNotNullAndNotEmpty(value) ? value : defaultValue;
  }

  /**
   * Retrieves paginated bulk upload transaction history for a specific member.
   *
   * @param pageNo   the page number (zero-based)
   * @param pageSize the number of records per page
   * @param memberId the member ID to filter transaction history
   * @return ResponseEntity containing paginated transaction details or error response
   */
  @Override
  public ResponseEntity<?> getBulkUploadTransactionIds(int pageNo, int pageSize, String memberId) {
    logger.info("Fetching bulk upload transaction history for memberId: {}, pageNo: {}, pageSize: {}",
        memberId, pageNo, pageSize);

    try {
      Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("createdAt").descending());
      Page<BulkPaymentHistory> bulkPaymentHistoryPage = bulkPaymentHistoryRepo.findByMemberIdOrderByCreatedAtDesc(
          memberId, pageable);

      Map<String, Object> response = new HashMap<>();
      response.put("transactionHistory", bulkPaymentHistoryPage.getContent());
      response.put("totalPages", bulkPaymentHistoryPage.getTotalPages());
      response.put("totalElements", bulkPaymentHistoryPage.getTotalElements());
      response.put("currentPage", bulkPaymentHistoryPage.getNumber());

      logger.info("Successfully fetched {} records for memberId: {}",
          bulkPaymentHistoryPage.getNumberOfElements(), memberId);
      return baseResponse.successResponse("Transaction history fetched successfully", response);

    } catch (Exception e) {
      logger.error("Error occurred while fetching bulk upload transaction IDs for memberId: {}", memberId, e);
      return baseResponse.errorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
          "Failed to fetch transaction history. Please try again later.");
    }
  }

  @Override
  public ResponseEntity<?> getBulkUploadAmountDetailsUsingTransactionId(String transactionId, String memberId) {
    try {
      List<BulkPaymentTransactionHistory> transactions =
          bulkPaymentTransactionHistoryRepo.findByTransactionIdAndMemberIdOrderByCreatedAtDesc(transactionId, memberId);

      if (transactions.isEmpty()) {
        return baseResponse.successResponse("Beneficiary Details Not Found", List.of());
      }

      return baseResponse.successResponse(transactions);

    } catch (Exception e) {
      return baseResponse.errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "");
    }
  }

  @Override
  public ResponseEntity<?> acceptOrDeniedBulkPayment(String transactionId, String memberId, boolean status) {
    try {

      if (!status) {
        updateBulkPaymentStatus(memberId, transactionId, BulkPaymentStatus.DENIED);
        return baseResponse.successResponse("All Payment Denied Successfully");
      }

      List<BulkPaymentTransactionHistory> transactionHistoryList =
          bulkPaymentTransactionHistoryRepo.findByTransactionIdAndMemberIdOrderByCreatedAtDesc(transactionId, memberId);

      if (transactionHistoryList.isEmpty()) {
        logger.warn("❗ No bulk payment transaction history found for transactionId: {} and memberId: {}",
            transactionId, memberId);
        return baseResponse.errorResponse(HttpStatus.NOT_FOUND, "No bulk payment records found.");
      }

      logger.info("🧾 Processing {} bulk payment transactions for transactionId: {}",
          transactionHistoryList.size(), transactionId);

      for (BulkPaymentTransactionHistory transaction : transactionHistoryList) {
        SendMoneyRequestDto requestDto = new SendMoneyRequestDto();
        requestDto.setAmount(transaction.getAmount());
        requestDto.setBeneficiaryId(transaction.getBeneficiaryCyrusId());
        requestDto.setBeneficiaryName(transaction.getBeneficiaryName());
        requestDto.setBeneficiaryMobileNumber(transaction.getBeneficiaryMobileNumber());
        requestDto.setTransferType(transaction.getTransactionType());
        requestDto.setComment(transaction.getComment());
        requestDto.setRemarks(transaction.getRemarks());

        boolean success = sendMoneyBulk(requestDto);

        transaction.setStatus(success ? BulkPaymentStatus.COMPLETED : BulkPaymentStatus.FAILED);
        bulkPaymentTransactionHistoryRepo.save(transaction); // Optional: batch save after loop
      }

      logger.info("✅ Completed processing bulk payment for transactionId: {}", transactionId);
      return baseResponse.successResponse("Bulk payment processed successfully.");

    } catch (Exception e) {
      logger.error("❌ Error while processing bulk payment for transactionId: {} - {}",
          transactionId, e.getMessage(), e);
      return baseResponse.errorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
          "Failed to process bulk payment.");
    }
  }

  @Transactional
  public boolean sendMoneyBulk(SendMoneyRequestDto requestDto) {
    String beneficiaryId = requestDto.getBeneficiaryId();
    String url = cyrusRechargeApiEndpoint + CyrusApiConstant.SEND_MONEY_URL;
    String generatedOrderId = UUID.randomUUID().toString();

    try {
      logger.info("🚀 Sending money to Beneficiary ID: {}", beneficiaryId);

      // Prepare form data
      MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
      formData.add("MerchantID", cyrusApiMemberId);
      formData.add("MerchantKey", cyrusPayoutApiKey);
      formData.add("MethodName", "sendmoney");
      formData.add("orderId", generatedOrderId);
      formData.add("Name", requestDto.getBeneficiaryName());
      formData.add("amount", String.valueOf(requestDto.getAmount()));
      formData.add("MobileNo", requestDto.getBeneficiaryMobileNumber());
      formData.add("comments", requestDto.getComment());
      formData.add("TransferType", requestDto.getTransferType());
      formData.add("beneficiaryid", beneficiaryId);
      formData.add("remarks", requestDto.getRemarks());

      logger.debug("📨 Form Data for Cyrus API: {}", formData);

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.MULTIPART_FORM_DATA);
      HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(formData, headers);

      logger.info("📡 Calling Cyrus API at: {}", url);
      ResponseEntity<String> apiResponse = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);

      logger.info("✅ Received response from Cyrus API: {}", apiResponse.getBody());

      SendMoneyResponseDto responseDto = objectMapper.readValue(apiResponse.getBody(), SendMoneyResponseDto.class);

      if (responseDto.getData() != null && StringUtils.isNotNullAndNotEmpty(responseDto.getData().getOrderId())) {
        logger.info("💸 Money sent successfully. Order ID: {}", responseDto.getData().getOrderId());

        SendMoneyHistory history = new SendMoneyHistory();
        history.setMemberId(cyrusApiMemberId);
        history.setBeneficiaryId(beneficiaryId);
        history.setStatus(responseDto.getStatus());
        history.setOrderId(responseDto.getData().getOrderId());
        history.setCyrusOrderId(responseDto.getData().getCyrusOrderId());
        history.setCyrusId(responseDto.getData().getCyrus_id());
        history.setRrnNumber(responseDto.getData().getRrn());
        history.setOpeningBalance(responseDto.getData().getOpening_bal());
        history.setLockedAmount(responseDto.getData().getLocked_amt());
        history.setChargedAmount(responseDto.getData().getCharged_amt());

        sendMoneyHistoryRepo.save(history);
        logger.info("📝 Saved SendMoneyHistory successfully.");

        if (walletBalanceRepository.count() == 0) {
          WalletBalance walletBalance = new WalletBalance();
          walletBalance.setMemberId(cyrusApiMemberId);
          walletBalance.setBalance(Double.parseDouble(responseDto.getData().getOpening_bal()));
          walletBalanceRepository.save(walletBalance);
          logger.info("💰 Wallet balance initialized successfully.");
        }

        return true;
      } else {
        logger.warn("⚠️ No valid Order ID received from API. Full Response: {}", responseDto);
        return false;
      }

    } catch (Exception e) {
      logger.error("❌ Exception during sendMoneyBulk for Beneficiary ID: {} - {}", beneficiaryId, e.getMessage(), e);
      return false;
    }
  }

  @Transactional
  public void updateBulkPaymentStatus(String memberId, String transactionId, BulkPaymentStatus status) {
    int updated = bulkPaymentHistoryRepo.updateStatusByMemberIdAndTransactionId(status, memberId, transactionId);
    if (updated > 0) {
      logger.info("✅ Updated bulk payment status to {} for transactionId: {}, memberId: {}",
          status, transactionId, memberId);
    } else {
      logger.warn("⚠️ No record found to update status for transactionId: {}, memberId: {}", transactionId, memberId);
    }
  }


}
