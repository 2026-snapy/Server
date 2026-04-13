package com.gbsw.snapy.domain.friend.controller;

import com.gbsw.snapy.domain.friend.service.FriendService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/friends")
public class FriendController {

    private final FriendService friendService;

}
