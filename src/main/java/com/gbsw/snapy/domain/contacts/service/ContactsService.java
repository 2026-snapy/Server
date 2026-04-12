package com.gbsw.snapy.domain.contacts.service;

import com.gbsw.snapy.domain.contacts.dto.request.ContactSyncRequest;
import com.gbsw.snapy.domain.contacts.dto.response.ContactSyncResponse;
import com.gbsw.snapy.domain.contacts.dto.response.ContactSyncResponse.ContactUserResponse;
import com.gbsw.snapy.domain.users.entity.User;
import com.gbsw.snapy.domain.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ContactsService {

    private final UserRepository userRepository;

    public ContactSyncResponse sync(ContactSyncRequest request) {
        List<User> users = userRepository.findTop10ByPhoneIn(request.phones());

        List<ContactUserResponse> contacts = new ArrayList<>();
        for (User user : users) {
            contacts.add(ContactUserResponse.from(user));
        }

        return new ContactSyncResponse(contacts);
    }
}
