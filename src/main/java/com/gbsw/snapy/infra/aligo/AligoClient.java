package com.gbsw.snapy.infra.aligo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gbsw.snapy.global.exception.CustomException;
import com.gbsw.snapy.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
@RequiredArgsConstructor
@Slf4j
public class AligoClient {

    private final RestClient aligoRestClient;
    private final AligoProperties properties;
    private final ObjectMapper objectMapper;

    public AligoSendResponse sendSms(String receiver, String message) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("key", properties.getKey());
        form.add("user_id", properties.getUserId());
        form.add("sender", properties.getSender());
        form.add("receiver", receiver);
        form.add("msg", message);
        form.add("msg_type", "SMS");
        form.add("testmode_yn", properties.isTestMode() ? "Y" : "N");

        String rawBody;
        try {
            rawBody = aligoRestClient.post()
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(String.class);
        } catch (RestClientException e) {
            log.error("Aligo SMS 호출 실패 - receiver: {}, cause: {}", receiver, e.getMessage(), e);
            throw new CustomException(ErrorCode.SMS_SEND_FAILED);
        }

        AligoSendResponse response;
        try {
            response = objectMapper.readValue(rawBody, AligoSendResponse.class);
        } catch (JsonProcessingException e) {
            log.error("Aligo SMS 응답 파싱 실패 - receiver: {}, body: {}", receiver, rawBody, e);
            throw new CustomException(ErrorCode.SMS_SEND_FAILED);
        }

        if (!response.isSuccess()) {
            log.error("Aligo SMS 응답 실패 - receiver: {}, response: {}", receiver, response);
            throw new CustomException(ErrorCode.SMS_SEND_FAILED);
        }

        log.info("Aligo SMS 전송 완료 - receiver: {}, msgId: {}", receiver, response.msgId());
        return response;
    }
}
