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
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.encoders.Base64;
import top.misec.bark.exception.BarkException;
import top.misec.bark.pojo.*;
import top.misec.bark.utils.AesUtils;


@Builder
@Slf4j
public class BarkPush {

    private String deviceKey;
    private String pushUrl;
    private Encryption encryption;

    public BarkPush(String pushUrl, String deviceKey) {
        if (StrUtil.isEmpty(pushUrl)) {
            throw new BarkException("pushUrl is empty");
        }
        if (StrUtil.isEmpty(deviceKey)) {
            throw new BarkException("deviceKey is empty");
        }
        if (!pushUrl.matches("^(https?)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]")) {
            throw new BarkException("pushUrl is invalid");
        }

        this.pushUrl = pushUrl;
        this.deviceKey = deviceKey;
    }

    /**
     * @param pushUrl    推送地址
     * @param deviceKey  设备Key
     * @param encryption 加密信息
     */
    public BarkPush(String deviceKey, String pushUrl, Encryption encryption) {
        if (StrUtil.isEmpty(pushUrl)) {
            throw new BarkException("pushUrl is empty");
        }
        if (StrUtil.isEmpty(deviceKey)) {
            throw new BarkException("deviceKey is empty");
        }
        if (!pushUrl.matches("^(https?)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]")) {
            throw new BarkException("pushUrl is invalid");
        }

        if (encryption == null) {
            throw new BarkException("encryption config lis null");
        }

        encryption.valid();
        this.pushUrl = pushUrl;
        this.deviceKey = deviceKey;
        this.encryption = encryption;
    }

    public BarkPush(BarkCfg cfg) {
        if (cfg == null) {
            throw new BarkException("cfg is null");
        }
        cfg.valid();
        this.pushUrl = cfg.getPushUrl();
        this.deviceKey = cfg.getDeviceKey();
    }

    /**
     * 推送简单文本信息 带结果返回
     *
     * @param content    推送内容
     * @param retryTimes 重试次数
     * @return BarkPushResp
     */
    public BarkPushResp simpleWithResp(String content, boolean retryTimes) {
        return this.executeWithResp(PushRequest.builder().deviceKey(this.deviceKey).body(content).build(), false);
    }

    public BarkPushResp simpleWithResp(String content) {
        return this.executeWithResp(PushRequest.builder().deviceKey(this.deviceKey).body(content).build(), false);
    }

    /**
     * 推送详细信息
     *
     * @param content content
     * @return BarkPushResp
     */
    public BarkPushResp encryptionPush(String content) {
        return this.executeWithResp(PushRequest.builder().deviceKey(this.deviceKey).body(content).build(), true);
    }


    private BarkPushResp executeWithResp(PushRequest pushRequest, boolean useEncrypt) {
        JSONObject request = new JSONObject();
        if (useEncrypt) {
            if (encryption == null) {
                throw new BarkException("encryption config lis null");
            }
            request.put("device_key", deviceKey);

            String content = JSON.toJSONString(pushRequest);
            byte[] encrypt = null;
            if (encryption.getMode().equals("ECB")) {
                encrypt = AesUtils.encrypt(content.getBytes(), encryption.getKey().getBytes(), 0, null);
            } else if (encryption.getMode().equals("CBC")) {
                encrypt = AesUtils.encrypt(content.getBytes(), encryption.getKey().getBytes(), 1, encryption.getIv().getBytes());
                request.put("iv", encryption.getIv());
            }
            assert encrypt != null;
            String ciphertext = Base64.toBase64String(encrypt);
            request.put("ciphertext", ciphertext);
        } else {
            request = JSONObject.from(pushRequest);
        }

        log.info("bark request url:{},request body :{}", pushUrl, request.toJSONString());
        HttpResponse httpResponse = HttpRequest.post(pushUrl).body(request.toJSONString()).header(Header.CONTENT_TYPE, ContentType.JSON.getValue()).execute();
        log.info("bark push response: {}", httpResponse);
        return JSON.parseObject(httpResponse.body(), BarkPushResp.class);
    }

    public BarkPushResp simpleWithResp(PushDetails obj) {
        PushRequest dto = PushRequest.builder().deviceKey(this.deviceKey).build();
        BeanUtil.copyProperties(obj, dto);
        return this.executeWithResp(dto, false);
    }

}
