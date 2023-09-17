package top.misec.bark;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.http.*;
import com.alibaba.fastjson2.JSON;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import top.misec.bark.exception.BarkException;
import top.misec.bark.pojo.BarkCfg;
import top.misec.bark.pojo.PushRequest;
import top.misec.bark.pojo.BarkPushResp;
import top.misec.bark.pojo.PushDetails;


@Builder
@Slf4j
public class BarkPush {

    private String deviceKey;
    private String pushUrl;

    BarkPush(String pushUrl, String deviceKey) {
        if (pushUrl == null || deviceKey == null) {
            throw new BarkException("pushUrl or deviceKey is null");
        }
        // pushUrl 必须以 https:// 或 http:// 开头
        if (!pushUrl.startsWith("https://") && !pushUrl.startsWith("http://")) {
            throw new BarkException("pushUrl must start with https:// or http://");
        }
        this.pushUrl = pushUrl;
        this.deviceKey = deviceKey;
    }

    BarkPush(BarkCfg cfg) {
        if (cfg == null) {
            throw new BarkException("cfg is null");
        }
        cfg.valid();
        this.pushUrl = cfg.getPushUrl();
        this.deviceKey = cfg.getDeviceKey();
    }

    /**
     * 推送简单文本信息
     *
     * @param content 推送内容
     */
    public void simplePush(String content) {
        boolean res = this.execute(PushRequest.builder().deviceKey(this.deviceKey).url(this.pushUrl).body(content).build());
    }

    /**
     * 推送简单文本信息
     *
     * @param content    推送内容
     * @param retryTimes 重试次数
     */
    public void simplePush(String content, boolean retryTimes) {
        boolean res = this.execute(PushRequest.builder().deviceKey(this.deviceKey).url(this.pushUrl).body(content).build());
    }

    /**
     * 推送简单文本信息 带结果返回
     *
     * @param content    推送内容
     * @param retryTimes 重试次数
     * @return BarkPushResp
     */
    public BarkPushResp simpleWithResp(String content, boolean retryTimes) {
        return this.executeWithResp(PushRequest.builder().deviceKey(this.deviceKey).url(this.pushUrl).body(content).build());
    }

    public BarkPushResp simpleWithResp(String content) {
        return this.executeWithResp(PushRequest.builder().deviceKey(this.deviceKey).url(this.pushUrl).body(content).build());
    }

    private boolean execute(PushRequest pushRequest) {
        log.info("bark request url:{},request body :{}", pushUrl, JSON.toJSONString(pushRequest));
        HttpResponse httpResponse = HttpRequest.post(pushUrl)
                .body(JSON.toJSONString(pushRequest))
                .header(Header.CONTENT_TYPE, ContentType.JSON.getValue())
                .execute();
        log.info("bark push response: {}", httpResponse);
        if (httpResponse.isOk()) {
            BarkPushResp barkPushResp = JSON.parseObject(httpResponse.body(), BarkPushResp.class);
            if (barkPushResp.getCode() == HttpStatus.HTTP_OK) {
                log.info("bark push success");
                return true;
            } else {
                log.info("bark push failed");
                return false;
            }
        }
        return false;
    }

    private BarkPushResp executeWithResp(PushRequest pushRequest) {
        log.info("bark request url:{},request body :{}", pushUrl, JSON.toJSONString(pushRequest));
        HttpResponse httpResponse = HttpRequest.post(pushUrl)
                .body(JSON.toJSONString(pushRequest))
                .header(Header.CONTENT_TYPE, ContentType.JSON.getValue())
                .execute();
        log.info("bark push response: {}", httpResponse);

        return JSON.parseObject(httpResponse.body(), BarkPushResp.class);
    }

    public BarkPushResp simpleWithResp(PushDetails obj) {
        PushRequest dto = PushRequest.builder().deviceKey(this.deviceKey).url(this.pushUrl).build();
        BeanUtil.copyProperties(obj, dto);
        return this.executeWithResp(dto);
    }

}
