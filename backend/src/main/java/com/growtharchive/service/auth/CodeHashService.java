package com.growtharchive.service.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import org.springframework.stereotype.Service;

@Service
public class CodeHashService {

    public String hashInviteCode(String code) {
        try {
            String normalized = normalize(code);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(normalized.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to hash invite code", exception);
        }
    }

    public String normalize(String code) {
        return code == null ? "" : code.trim().toUpperCase();
    }

    public String preview(String code) {
        return code == null ? "" : code.trim();
    }
}
