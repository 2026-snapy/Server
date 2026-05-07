package com.gbsw.snapy.domain.auth.dto.internal;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class ApplePublicKeys {
    private List<Key> keys;

    @Getter
    @NoArgsConstructor
    public static class Key {
        private String kty;
        private String kid;
        private String use;
        private String alg;
        private String n;
        private String e;
    }
}
