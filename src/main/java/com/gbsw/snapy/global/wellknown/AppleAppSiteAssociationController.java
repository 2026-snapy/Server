package com.gbsw.snapy.global.wellknown;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AppleAppSiteAssociationController {

    private static final String APPLE_APP_SITE_ASSOCIATION = """
            {
              "applinks": {
                "apps": [],
                "details": [
                  {
                    "appID": "QLF22M2925.com.messiofcoding.snapy",
                    "paths": ["*"]
                  }
                ]
              }
            }
            """;

    @GetMapping(
            value = "/.well-known/apple-app-site-association",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<String> getAppleAppSiteAssociation() {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(APPLE_APP_SITE_ASSOCIATION);
    }
}
