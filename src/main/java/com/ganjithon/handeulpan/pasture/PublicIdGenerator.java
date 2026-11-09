package com.ganjithon.handeulpan.pasture;

import java.security.SecureRandom;
import java.util.Base64;
import org.springframework.stereotype.Component;

/** 들판 링크용 ID: 128비트 랜덤 → URL에 안전한 22자. */
@Component
public class PublicIdGenerator {

    private final SecureRandom random = new SecureRandom();

    public String next() {
        byte[] bytes = new byte[16];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
