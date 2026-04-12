package com.gbsw.snapy.domain.contacts.controller;

import com.gbsw.snapy.domain.contacts.dto.request.ContactSyncRequest;
import com.gbsw.snapy.domain.contacts.dto.response.ContactSyncResponse;
import com.gbsw.snapy.domain.contacts.service.ContactsService;
import com.gbsw.snapy.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/contacts")
public class ContactsController {

    private final ContactsService contactsService;

    @PostMapping("/sync")
    public ResponseEntity<ApiResponse<ContactSyncResponse>> sync(
            @Valid @RequestBody ContactSyncRequest request
    ) {
        ContactSyncResponse response = contactsService.sync(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
