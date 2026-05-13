package com.gbsw.snapy.infra.aligo;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AligoSendResponse(
        @JsonProperty("result_code") int resultCode,
        @JsonProperty("message") String message,
        @JsonProperty("msg_id") Long msgId,
        @JsonProperty("success_cnt") Integer successCnt,
        @JsonProperty("error_cnt") Integer errorCnt,
        @JsonProperty("msg_type") String msgType
) {
    public boolean isSuccess() {
        return resultCode == 1;
    }
}
