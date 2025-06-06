package com.tektechno.payout.serviceimpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tektechno.payout.dto.response.SendMoneyResponseDto;
import com.tektechno.payout.model.SendMoneyHistory;
import com.tektechno.payout.repository.SendMoneyHistoryRepo;
import com.tektechno.payout.service.CallbackService;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CallbackServiceImpl implements CallbackService {

  @Autowired
  private SendMoneyHistoryRepo sendMoneyHistoryRepo;

  @Autowired
  private ObjectMapper objectMapper;

  private static final Logger logger = LoggerFactory.getLogger(CallbackServiceImpl.class);

  @Override
  public void callbackForPayout(String statuscode, String status, String data) {
    logger.info("Received payout callback with statuscode: {}, status: {}, data: {}", statuscode, status, data);

    try {
      // Decode the data
      String decodedData = URLDecoder.decode(data, StandardCharsets.UTF_8);
      logger.debug("Decoded data: {}", decodedData);

      // Parse JSON to DTO
      SendMoneyResponseDto.Data webhookData = objectMapper.readValue(decodedData, SendMoneyResponseDto.Data.class);
      logger.info("Parsed webhook data for orderId: {}", webhookData.getOrderId());

      // Fetch SendMoneyHistory by orderId
      Optional<SendMoneyHistory> optionalSendMoneyHistory = sendMoneyHistoryRepo.findByOrderId(webhookData.getOrderId());

      if (optionalSendMoneyHistory.isPresent()) {
        SendMoneyHistory sendMoneyHistory = optionalSendMoneyHistory.get();
        logger.debug("Updating SendMoneyHistory record for orderId: {}", webhookData.getOrderId());

        // Update entity fields
        sendMoneyHistory.setStatus(status);
        sendMoneyHistory.setCyrusOrderId(webhookData.getCyrusOrderId());
        sendMoneyHistory.setCyrusId(webhookData.getCyrus_id());
        sendMoneyHistory.setOpeningBalance(webhookData.getOpening_bal());
        sendMoneyHistory.setLockedAmount(webhookData.getLocked_amt());
        sendMoneyHistory.setChargedAmount(webhookData.getCharged_amt());
        sendMoneyHistory.setRrnNumber(webhookData.getRrn());

        // Save updated record
        sendMoneyHistoryRepo.save(sendMoneyHistory);
        logger.info("SendMoneyHistory record updated and saved for orderId: {}", webhookData.getOrderId());
      } else {
        logger.warn("No SendMoneyHistory found for orderId: {}", webhookData.getOrderId());
      }

    } catch (JsonProcessingException e) {
      logger.error("Failed to parse webhook data JSON: {}", e.getMessage(), e);
    } catch (Exception e) {
      logger.error("Unexpected error during payout callback processing", e);
    }
  }

}
