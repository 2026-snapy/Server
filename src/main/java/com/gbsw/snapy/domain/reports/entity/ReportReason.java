package com.gbsw.snapy.domain.reports.entity;

public enum ReportReason {
    SPAM_OR_SCAM("스팸 또는 사기"),
    NUDITY_OR_SEXUAL_CONTENT("나체 또는 성적 콘텐츠"),
    HATE_SPEECH_OR_SYMBOL("혐오 발언 또는 상징"),
    VIOLENCE_OR_DANGEROUS_ORGANIZATION("폭력 또는 위험한 단체"),
    FALSE_INFORMATION("거짓 정보"),
    BULLYING_OR_HARASSMENT("따돌림 또는 괴롭힘"),
    INTELLECTUAL_PROPERTY_INFRINGEMENT("지식재산권 침해"),
    OTHER("기타");

    private final String description;

    ReportReason(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
