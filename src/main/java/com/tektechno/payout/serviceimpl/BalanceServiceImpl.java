package com.tektechno.payout.serviceimpl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tektechno.payout.constant.CyrusApiConstant;
import com.tektechno.payout.dto.response.ApiResponse;
import com.tektechno.payout.response.BaseResponse;
import com.tektechno.payout.service.BalanceService;
import com.tektechno.payout.service.WalletBalanceService;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class BalanceServiceImpl implements BalanceService {

  @Value("${cyrus-api-member-id}")
  private String cyrusApiMemberId;

  @Value("${cyrus-recharge-api-key}")
  private String cyrusRechargeApiKey;

  @Value("${cyrus-recharge-api-endpoint}")
  private String cyrusRechargeApiEndpoint;

  @Autowired
  private BaseResponse baseResponse;

  @Autowired
  private RestTemplate restTemplate;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private WalletBalanceService walletBalanceService;

  private static final Logger logger = LoggerFactory.getLogger(BalanceServiceImpl.class);

  @Override
  public ResponseEntity<?> getBalance() {
    try {
      String url = cyrusRechargeApiEndpoint + CyrusApiConstant.GET_BALANCE_URL
          .replace("{memberId}", cyrusApiMemberId).replace("{pin}", cyrusRechargeApiKey);
      logger.info("Initiating GET request to Cyrus Get Balance API: {}", url);

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      HttpEntity<?> requestEntity = new HttpEntity<>(headers);

      logger.debug("Request Headers: {}", headers);

      ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);

      logger.info("Cyrus Get Balance API responded with status: {}", response.getStatusCode());
      logger.debug("Response Body from Cyrus Get Balance API: {}", response.getBody());

      List<ApiResponse> responseList = objectMapper.readValue(response.getBody(),
          new TypeReference<List<ApiResponse>>() {
          });

      if (!responseList.isEmpty()) {
        ApiResponse apiResponse = responseList.getFirst();
        List<ApiResponse.BalanceData> data = apiResponse.getData();

        if (data != null && !data.isEmpty()) {
          double balance = data.getFirst().getBalance();
          if (walletBalanceService.updateWalletBalance(cyrusApiMemberId, balance)) {
            logger.info("Successfully updated wallet balance for memberId: {}", cyrusApiMemberId);
          } else {
            logger.error("Failed to update wallet balance for memberId: {}", cyrusApiMemberId);
          }
        }
      }

      return baseResponse.successResponse(responseList);

    } catch (HttpClientErrorException ex) {
      logger.error("Client error while calling Cyrus Get Balance API: Status Code = {}, Response Body = {}",
          ex.getStatusCode(), ex.getResponseBodyAsString(), ex);
      return baseResponse.errorResponse((HttpStatus) ex.getStatusCode(),
          "Client error while fetching balance from Cyrus API");

    } catch (HttpServerErrorException ex) {
      logger.error("Server error while calling Cyrus Get Balance API: Status Code = {}, Response Body = {}",
          ex.getStatusCode(), ex.getResponseBodyAsString(), ex);
      return baseResponse.errorResponse(HttpStatus.BAD_GATEWAY,
          "Server error occurred while fetching balance from Cyrus API");

    } catch (Exception e) {
      logger.error("Unexpected error occurred while fetching balance from Cyrus API", e);
      return baseResponse.errorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
          "An unexpected error occurred while fetching balance from Cyrus API");
    }
  }

}
