package com.tektechno.payout.serviceimpl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tektechno.payout.constant.CyrusApiConstant;
import com.tektechno.payout.dto.request.AddBeneficiaryRequestDto;
import com.tektechno.payout.dto.request.SendMoneyRequestDto;
import com.tektechno.payout.dto.response.AddBeneficiaryResponseDto;
import com.tektechno.payout.dto.response.BeneficiaryDetailsDto;
import com.tektechno.payout.dto.response.SendMoneyResponseDto;
import com.tektechno.payout.model.Beneficiary;
import com.tektechno.payout.model.SendMoneyHistory;
import com.tektechno.payout.repository.BeneficiaryRepository;
import com.tektechno.payout.repository.SendMoneyHistoryRepo;
import com.tektechno.payout.response.BaseResponse;
import com.tektechno.payout.service.PayoutService;
import com.tektechno.payout.utilities.StringUtils;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
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
      formData.add("beneficiary_bank_account_number", requestDto.getBeneficiaryMobileNumber());
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

      logger.debug("Sending request to Cyrus API with payload: {}", formData);

      // Make API call
      ResponseEntity<String> apiResponse = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
      logger.info("Received response from Cyrus API: {}", apiResponse.getBody());

      // Convert response
      AddBeneficiaryResponseDto responseDto = objectMapper.readValue(apiResponse.getBody(), AddBeneficiaryResponseDto.class);

      // Process response
      if ("SUCCESS".equalsIgnoreCase(responseDto.getData().getStatus())) {
        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setBeneficiaryId(responseDto.getData().getBeneficiaryId());
        beneficiary.setBeneType(requestDto.getBeneType());
        beneficiary.setBeneficiaryBankAccountNumber(requestDto.getBeneficiaryMobileNumber());
        beneficiary.setBeneficiaryBankIfscCode(requestDto.getBeneficiaryName()); // Validate name != IFSC
        beneficiary.setBeneficiaryBankName(requestDto.getBeneficiaryName());
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
      return baseResponse.successResponse(sendMoneyHistories.getContent());

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
      return baseResponse.successResponse(beneficiaries.getContent());

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
      return baseResponse.successResponse(sendMoneyHistories.getContent());
    } catch (Exception e) {
      return baseResponse.errorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
          "An unexpected error occurred while fetching transaction details");
    }
  }


}
