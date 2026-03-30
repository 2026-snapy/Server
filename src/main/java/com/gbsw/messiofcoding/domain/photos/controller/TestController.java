package com.gbsw.messiofcoding.domain.photos.controller;

import com.gbsw.messiofcoding.domain.photos.dto.response.PhotoUploadResponse;
import com.gbsw.messiofcoding.domain.photos.service.PhotoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class TestController {

    private final PhotoService photoService;

    @PostMapping("/upload")
    public PhotoUploadResponse upload(
            @RequestParam("file") MultipartFile file) {
        return photoService.upload(file, 1L); // userId 하드코딩
    }
}