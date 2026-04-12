package com.gbsw.snapy.domain.contacts.dto.request;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record ContactSyncRequest(
        @NotEmpty
        List<String> phones
) {
}
