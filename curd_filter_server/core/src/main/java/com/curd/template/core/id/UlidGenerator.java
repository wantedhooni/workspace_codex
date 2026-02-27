package com.curd.template.core.id;

import java.security.SecureRandom;
import java.time.Clock;
import org.springframework.stereotype.Component;

@Component
public class UlidGenerator implements IdGenerator {
    private static final char[] C32 = "0123456789ABCDEFGHJKMNPQRSTVWXYZ".toCharArray();

    private final SecureRandom random = new SecureRandom();
    private final Clock clock;

    public UlidGenerator() {
        this(Clock.systemUTC());
    }

    UlidGenerator(Clock clock) {
        this.clock = clock;
    }

    @Override
    public String nextId() {
        long timestamp = clock.millis();
        byte[] bytes = new byte[16];

        bytes[0] = (byte) ((timestamp >>> 40) & 0xFF);
        bytes[1] = (byte) ((timestamp >>> 32) & 0xFF);
        bytes[2] = (byte) ((timestamp >>> 24) & 0xFF);
        bytes[3] = (byte) ((timestamp >>> 16) & 0xFF);
        bytes[4] = (byte) ((timestamp >>> 8) & 0xFF);
        bytes[5] = (byte) (timestamp & 0xFF);

        byte[] randomBytes = new byte[10];
        random.nextBytes(randomBytes);
        System.arraycopy(randomBytes, 0, bytes, 6, randomBytes.length);

        return toBase32(bytes);
    }

    private String toBase32(byte[] bytes) {
        char[] chars = new char[26];

        chars[0] = C32[(bytes[0] & 224) >>> 5];
        chars[1] = C32[bytes[0] & 31];
        chars[2] = C32[(bytes[1] & 248) >>> 3];
        chars[3] = C32[((bytes[1] & 7) << 2) | ((bytes[2] & 192) >>> 6)];
        chars[4] = C32[(bytes[2] & 62) >>> 1];
        chars[5] = C32[((bytes[2] & 1) << 4) | ((bytes[3] & 240) >>> 4)];
        chars[6] = C32[((bytes[3] & 15) << 1) | ((bytes[4] & 128) >>> 7)];
        chars[7] = C32[(bytes[4] & 124) >>> 2];
        chars[8] = C32[((bytes[4] & 3) << 3) | ((bytes[5] & 224) >>> 5)];
        chars[9] = C32[bytes[5] & 31];

        chars[10] = C32[(bytes[6] & 248) >>> 3];
        chars[11] = C32[((bytes[6] & 7) << 2) | ((bytes[7] & 192) >>> 6)];
        chars[12] = C32[(bytes[7] & 62) >>> 1];
        chars[13] = C32[((bytes[7] & 1) << 4) | ((bytes[8] & 240) >>> 4)];
        chars[14] = C32[((bytes[8] & 15) << 1) | ((bytes[9] & 128) >>> 7)];
        chars[15] = C32[(bytes[9] & 124) >>> 2];
        chars[16] = C32[((bytes[9] & 3) << 3) | ((bytes[10] & 224) >>> 5)];
        chars[17] = C32[bytes[10] & 31];
        chars[18] = C32[(bytes[11] & 248) >>> 3];
        chars[19] = C32[((bytes[11] & 7) << 2) | ((bytes[12] & 192) >>> 6)];
        chars[20] = C32[(bytes[12] & 62) >>> 1];
        chars[21] = C32[((bytes[12] & 1) << 4) | ((bytes[13] & 240) >>> 4)];
        chars[22] = C32[((bytes[13] & 15) << 1) | ((bytes[14] & 128) >>> 7)];
        chars[23] = C32[(bytes[14] & 124) >>> 2];
        chars[24] = C32[((bytes[14] & 3) << 3) | ((bytes[15] & 224) >>> 5)];
        chars[25] = C32[bytes[15] & 31];

        return new String(chars);
    }
}
