package top.misec.bark;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.bouncycastle.util.encoders.Base64;
import org.junit.jupiter.api.Test;
import top.misec.bark.enums.SoundEnum;
import top.misec.bark.exception.BarkException;
import top.misec.bark.pojo.BarkCfg;
import top.misec.bark.pojo.Encryption;
import top.misec.bark.pojo.PushDetails;
import top.misec.bark.pojo.PushRequest;
import top.misec.bark.utils.AesUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        assertThrowsExactly(BarkException.class, () -> new BarkPush("not a url", "deviceKey"));
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
        assertThrowsExactly(BarkException.class, () -> new BarkPush("", "deviceKey", encryption));
    }

    @Test
    void encryptionConstructorRejectsInvalidEncryption() {
        assertThrowsExactly(BarkException.class,
                () -> new BarkPush(VALID_URL, "deviceKey", Encryption.builder().key(VALID_KEY).mode("CBC").iv("x").build()));
    }

    @Test
    void encryptionConstructorAcceptsValidArgs() {
        assertNotNull(new BarkPush(VALID_URL, "deviceKey", validEncryption()));
    }

    @Test
    void builderSupportsAuthAndTimeout() {
        BarkPush pusher = BarkPush.builder()
                .pushUrl(VALID_URL)
                .deviceKey("deviceKey")
                .username("user")
                .password("pass")
                .timeout(5000)
                .build();
        assertEquals("user", pusher.getUsername());
        assertEquals(5000, pusher.getTimeout());
    }

    @Test
    void batchPushRejectsEmptyDeviceKeys() {
        BarkPush pusher = new BarkPush(VALID_URL, "deviceKey");
        assertThrowsExactly(BarkException.class, () -> pusher.batchWithResp(List.of(), null));
    }

    @Test
    void pushRequestSerializesSnakeCaseKeys() {
        PushRequest request = PushRequest.builder()
                .deviceKey("dk")
                .deviceKeys(List.of("k1", "k2"))
                .badge(1)
                .ttl(3600)
                .build();
        JSONObject json = JSONObject.from(request);
        assertEquals("dk", json.getString("device_key"));
        assertEquals(2, json.getJSONArray("device_keys").size());
        assertEquals(1, json.getIntValue("badge"));
        assertEquals(3600, json.getIntValue("ttl"));
        assertTrue(json.getString("deviceKey") == null);
    }

    @Test
    void barkCfgValidAcceptsNormalUrls() {
        BarkCfg cfg = BarkCfg.builder().pushUrl(VALID_URL).deviceKey("deviceKey").build();
        cfg.valid();
        assertEquals(VALID_URL, cfg.getPushUrl());
    }

    @Test
    void barkCfgValidRejectsBadUrls() {
        assertThrowsExactly(BarkException.class,
                () -> BarkCfg.builder().pushUrl("ftp://api.day.app/push").deviceKey("k").build().valid());
        assertThrowsExactly(BarkException.class,
                () -> BarkCfg.builder().pushUrl("api.day.app/push").deviceKey("k").build().valid());
    }

    @Test
    void barkCfgConstructorBuildsPusher() {
        BarkCfg cfg = BarkCfg.builder()
                .pushUrl(VALID_URL)
                .deviceKey("deviceKey")
                .username("user")
                .password("pass")
                .timeout(3000)
                .build();
        BarkPush pusher = new BarkPush(cfg);
        assertEquals("user", pusher.getUsername());
        assertEquals(3000, pusher.getTimeout());
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
        assertThrowsExactly(BarkException.class,
                () -> Encryption.builder().key("12345678901234567").mode("ECB").build().valid());
    }

    @Test
    void encryptionValidAcceptsAes192Key() {
        Encryption e = Encryption.builder().key("123456789012345678901234").mode("ECB").build();
        e.valid();
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

    @Test
    void encryptionValidRejectsCbcWithoutIv() {
        assertThrowsExactly(BarkException.class,
                () -> Encryption.builder().key(VALID_KEY).mode("CBC").build().valid());
    }

    @Test
    void encryptionValidGcmIvRules() {
        Encryption noIv = Encryption.builder().key(VALID_KEY).mode("GCM").build();
        noIv.valid();
        Encryption twelveIv = Encryption.builder().key(VALID_KEY).mode("GCM").iv("0123456789ab").build();
        twelveIv.valid();
        assertThrowsExactly(BarkException.class,
                () -> Encryption.builder().key(VALID_KEY).mode("GCM").iv("1111111111111111").build().valid());
    }

    @Test
    void aesGcmRoundTrip() {
        String text = "{\"body\":\"gcm 测试\"}";
        byte[] key = VALID_KEY.getBytes(StandardCharsets.UTF_8);
        byte[] iv = "0123456789ab".getBytes(StandardCharsets.UTF_8);
        byte[] enc = AesUtils.encrypt(text.getBytes(StandardCharsets.UTF_8), key, AesUtils.MODE_GCM, iv);
        // JDK/BC 的 GCM 输出为 ciphertext || 16字节tag，与 Bark 客户端 CryptoSwift .combined 一致
        byte[] dec = AesUtils.decrypt(enc, key, AesUtils.MODE_GCM, iv);
        assertEquals(text, new String(dec, StandardCharsets.UTF_8));
    }

    @Test
    void aesEcbRoundTrip() {
        String text = "hello bark 中文";
        byte[] key = VALID_KEY.getBytes(StandardCharsets.UTF_8);
        byte[] enc = AesUtils.encrypt(text.getBytes(StandardCharsets.UTF_8), key, 0, null);
        byte[] dec = AesUtils.decrypt(enc, key, 0, null);
        assertEquals(text, new String(dec, StandardCharsets.UTF_8));
    }

    @Test
    void aesCbcRoundTrip() {
        String text = "{\"body\":\"hello\"}";
        byte[] key = VALID_KEY.getBytes(StandardCharsets.UTF_8);
        byte[] iv = "1111111111111111".getBytes(StandardCharsets.UTF_8);
        byte[] enc = AesUtils.encrypt(text.getBytes(StandardCharsets.UTF_8), key, 1, iv);
        byte[] dec = AesUtils.decrypt(enc, key, 1, iv);
        assertEquals(text, new String(dec, StandardCharsets.UTF_8));
    }

    @Test
    void encryptionPushProducesValidCiphertext() {
        Encryption encryption = validEncryption();
        String content = "hello";
        String plain = JSON.toJSONString(PushRequest.builder().deviceKey("deviceKey").body(content).build());
        byte[] enc = AesUtils.encrypt(plain.getBytes(StandardCharsets.UTF_8),
                encryption.getKey().getBytes(StandardCharsets.UTF_8), 1,
                encryption.getIv().getBytes(StandardCharsets.UTF_8));
        String ciphertext = Base64.toBase64String(enc);
        JSONObject request = new JSONObject();
        request.put("device_key", "deviceKey");
        request.put("iv", encryption.getIv());
        request.put("ciphertext", ciphertext);
        byte[] dec = AesUtils.decrypt(Base64.decode(ciphertext),
                encryption.getKey().getBytes(StandardCharsets.UTF_8), 1,
                encryption.getIv().getBytes(StandardCharsets.UTF_8));
        assertTrue(new String(dec, StandardCharsets.UTF_8).contains("\"body\":\"hello\""));
    }

    private static Encryption validEncryption() {
        return Encryption.builder()
                .key(VALID_KEY)
                .iv("1111111111111111")
                .mode("CBC")
                .build();
    }
}
