package top.misec.bark;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.ContentType;
import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.encoders.Base64;
import top.misec.bark.exception.BarkException;
import top.misec.bark.pojo.*;
import top.misec.bark.utils.AesUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.List;
import java.util.HexFormat;


@Getter
@Slf4j
public class BarkPush {

    private static final int RETRY_TIMES = 3;
    private static final long RETRY_INTERVAL_MS = 500;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final String deviceKey;
    private final String pushUrl;
    private final Encryption encryption;
    /**
     * 服务端启用 Basic Auth 时必填
     */
    private final String username;
    private final String password;
    /**
     * HTTP 超时时间（毫秒），null 或 <=0 表示不超时
     */
    private final Integer timeout;

    @Builder
    private BarkPush(String pushUrl, String deviceKey, Encryption encryption,
                     String username, String password, Integer timeout) {
        checkUrl(pushUrl);
        checkNotEmpty(deviceKey, "deviceKey is empty");
        if (encryption != null) {
            encryption.valid();
        }
        this.pushUrl = pushUrl;
        this.deviceKey = deviceKey;
        this.encryption = encryption;
        this.username = username;
        this.password = password;
        this.timeout = timeout;
    }

    public BarkPush(String pushUrl, String deviceKey) {
        this(pushUrl, deviceKey, null, null, null, null);
    }

    /**
     * @param pushUrl    推送地址
     * @param deviceKey  设备Key
     * @param encryption 加密信息
     */
    public BarkPush(String pushUrl, String deviceKey, Encryption encryption) {
        this(pushUrl, deviceKey, encryption, null, null, null);
    }

    public BarkPush(BarkCfg cfg) {
        if (cfg == null) {
            throw new BarkException("cfg is null");
        }
        cfg.valid();
        this.pushUrl = cfg.getPushUrl();
        this.deviceKey = cfg.getDeviceKey();
        this.encryption = cfg.getEncryption();
        this.username = cfg.getUsername();
        this.password = cfg.getPassword();
        this.timeout = cfg.getTimeout();
    }

    /**
     * 推送简单文本信息 带结果返回
     *
     * @param content 推送内容
     * @param retry   网络异常时是否重试（最多 {@value #RETRY_TIMES} 次）
     * @return BarkPushResp
     */
    public BarkPushResp simpleWithResp(String content, boolean retry) {
        return this.executeWithResp(PushRequest.builder().deviceKey(this.deviceKey).body(content).build(), false, retry);
    }

    public BarkPushResp simpleWithResp(String content) {
        return this.executeWithResp(PushRequest.builder().deviceKey(this.deviceKey).body(content).build(), false, false);
    }

    /**
     * 推送详细信息
     *
     * @param obj 推送详情
     * @return BarkPushResp
     */
    public BarkPushResp simpleWithResp(PushDetails obj) {
        PushRequest dto = baseRequest();
        BeanUtil.copyProperties(obj, dto);
        return this.executeWithResp(dto, false, false);
    }

    /**
     * 批量推送（服务端 device_keys 能力），返回结果中 data 字段为逐设备结果
     *
     * @param deviceKeys 设备Key列表
     * @param obj        推送详情
     * @return BarkPushResp
     */
    public BarkPushResp batchWithResp(List<String> deviceKeys, PushDetails obj) {
        if (deviceKeys == null || deviceKeys.isEmpty()) {
            throw new BarkException("deviceKeys is empty");
        }
        PushRequest dto = PushRequest.builder().deviceKeys(deviceKeys).build();
        if (obj != null) {
            BeanUtil.copyProperties(obj, dto);
        }
        return this.executeWithResp(dto, false, false);
    }

    /**
     * 加密推送
     *
     * @param content content
     * @return BarkPushResp
     */
    public BarkPushResp encryptionPush(String content) {
        return this.executeWithResp(PushRequest.builder().deviceKey(this.deviceKey).body(content).build(), true, false);
    }

    private PushRequest baseRequest() {
        return PushRequest.builder().deviceKey(this.deviceKey).build();
    }

    private BarkPushResp executeWithResp(PushRequest pushRequest, boolean useEncrypt, boolean retry) {
        JSONObject request;
        if (useEncrypt) {
            if (encryption == null) {
                throw new BarkException("encryption config lis null");
            }
            request = new JSONObject();
            request.put("device_key", deviceKey);

            String content = JSON.toJSONString(pushRequest);
            byte[] plainBytes = content.getBytes(StandardCharsets.UTF_8);
            byte[] keyBytes = encryption.getKey().getBytes(StandardCharsets.UTF_8);
            switch (encryption.getMode()) {
                case "ECB" -> request.put("ciphertext", Base64.toBase64String(
                        AesUtils.encrypt(plainBytes, keyBytes, AesUtils.MODE_ECB, null)));
                case "CBC" -> {
                    byte[] ivBytes = encryption.getIv().getBytes(StandardCharsets.UTF_8);
                    request.put("iv", encryption.getIv());
                    request.put("ciphertext", Base64.toBase64String(
                            AesUtils.encrypt(plainBytes, keyBytes, AesUtils.MODE_CBC, ivBytes)));
                }
                case "GCM" -> {
                    String iv = StrUtil.isNotEmpty(encryption.getIv()) ? encryption.getIv() : randomGcmIv();
                    byte[] ivBytes = iv.getBytes(StandardCharsets.UTF_8);
                    request.put("iv", iv);
                    request.put("ciphertext", Base64.toBase64String(
                            AesUtils.encrypt(plainBytes, keyBytes, AesUtils.MODE_GCM, ivBytes)));
                }
                default -> throw new BarkException("AES Mode is invalid, only support ECB, CBC or GCM");
            }
        } else {
            request = JSONObject.from(pushRequest);
        }

        String body = request.toJSONString();
        log.info("bark request url:{},request body :{}", pushUrl, body);

        int attempts = retry ? RETRY_TIMES : 1;
        RuntimeException lastError = null;
        for (int i = 0; i < attempts; i++) {
            try {
                HttpResponse httpResponse = buildHttpRequest(body).execute();
                log.info("bark push response: {}", httpResponse);
                return JSON.parseObject(httpResponse.body(), BarkPushResp.class);
            } catch (RuntimeException e) {
                lastError = e;
                if (i < attempts - 1) {
                    log.warn("bark push failed, retry {}/{}: {}", i + 2, attempts, e.getMessage());
                    try {
                        Thread.sleep(RETRY_INTERVAL_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
        throw lastError;
    }

    private HttpRequest buildHttpRequest(String body) {
        HttpRequest httpRequest = HttpRequest.post(pushUrl)
                .body(body)
                .header(Header.CONTENT_TYPE, ContentType.JSON.getValue());
        if (StrUtil.isNotEmpty(username)) {
            httpRequest.basicAuth(username, password);
        }
        if (timeout != null && timeout > 0) {
            httpRequest.timeout(timeout);
        }
        return httpRequest;
    }

    private static String randomGcmIv() {
        byte[] bytes = new byte[6];
        SECURE_RANDOM.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    private static void checkUrl(String pushUrl) {
        checkNotEmpty(pushUrl, "pushUrl is empty");
        URI uri;
        try {
            uri = new URI(pushUrl);
        } catch (URISyntaxException e) {
            throw new BarkException("pushUrl is invalid");
        }
        if (uri.getHost() == null || !("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()))) {
            throw new BarkException("pushUrl is invalid");
        }
    }

    private static void checkNotEmpty(String value, String message) {
        if (StrUtil.isEmpty(value)) {
            throw new BarkException(message);
        }
    }
}
