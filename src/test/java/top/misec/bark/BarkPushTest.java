package top.misec.bark;

import org.junit.jupiter.api.Test;
import top.misec.bark.enums.SoundEnum;
import top.misec.bark.exception.BarkException;
import top.misec.bark.pojo.Encryption;
import top.misec.bark.pojo.PushDetails;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

class BarkPushTest {

    private static final String VALID_URL = "https://api.day.app/push";
    private static final String VALID_KEY = "12345678901234561234567890123456";

    @Test
    void constructorRejectsEmptyPushUrl() {
        assertThrowsExactly(BarkException.class, () -> new BarkPush("", "deviceKey"));
    }

    @Test
    void constructorRejectsEmptyDeviceKey() {
        assertThrowsExactly(BarkException.class, () -> new BarkPush(VALID_URL, ""));
    }

    @Test
    void constructorRejectsInvalidPushUrl() {
        assertThrowsExactly(BarkException.class, () -> new BarkPush("ftp://api.day.app/push", "deviceKey"));
    }

    @Test
    void constructorAcceptsValidArgs() {
        assertNotNull(new BarkPush(VALID_URL, "deviceKey"));
    }

    @Test
    void pushDetailsBuilder() {
        PushDetails pushDetails = PushDetails.builder()
                .title("test")
                .sound(SoundEnum.BLOOM.getSoundName())
                .body("test")
                .build();
        assertEquals("bloom.caf", pushDetails.getSound());
    }

    @Test
    void encryptionConstructorRejectsEmptyPushUrl() {
        Encryption encryption = validEncryption();
        assertThrowsExactly(BarkException.class, () -> new BarkPush("deviceKey", "", encryption));
    }

    @Test
    void encryptionConstructorRejectsNullEncryption() {
        assertThrowsExactly(BarkException.class, () -> new BarkPush("deviceKey", VALID_URL, null));
    }

    @Test
    void encryptionConstructorAcceptsValidArgs() {
        assertNotNull(new BarkPush("deviceKey", VALID_URL, validEncryption()));
    }

    @Test
    void encryptionValidDefaults() {
        Encryption encryption = Encryption.builder()
                .key(VALID_KEY)
                .mode("ECB")
                .build();
        encryption.valid();
        assertEquals("AES", encryption.getAlgorithm());
        assertEquals("PKCS7Padding", encryption.getPadding());
    }

    @Test
    void encryptionValidRejectsEmptyKey() {
        assertThrowsExactly(BarkException.class,
                () -> Encryption.builder().mode("ECB").build().valid());
    }

    @Test
    void encryptionValidRejectsInvalidKeyLength() {
        assertThrowsExactly(BarkException.class,
                () -> Encryption.builder().key("short").mode("ECB").build().valid());
    }

    @Test
    void encryptionValidRejectsInvalidMode() {
        assertThrowsExactly(BarkException.class,
                () -> Encryption.builder().key(VALID_KEY).mode("AES").build().valid());
    }

    @Test
    void encryptionValidRejectsInvalidIvLength() {
        assertThrowsExactly(BarkException.class,
                () -> Encryption.builder().key(VALID_KEY).iv("short").mode("CBC").build().valid());
    }

    private static Encryption validEncryption() {
        return Encryption.builder()
                .key(VALID_KEY)
                .iv("1111111111111111")
                .mode("CBC")
                .build();
    }
}
