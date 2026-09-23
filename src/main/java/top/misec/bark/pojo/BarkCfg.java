package top.misec.bark.pojo;


import cn.hutool.core.util.StrUtil;
import lombok.Builder;
import lombok.Data;
import top.misec.bark.exception.BarkException;

import java.net.URI;
import java.net.URISyntaxException;

@Data
@Builder
public class BarkCfg {
    private String pushUrl;
    private String deviceKey;
    private Encryption encryption;
    /**
     * 服务端启用 Basic Auth（ARK_USER/ARK_PASSWORD）时必填
     */
    private String username;
    private String password;
    /**
     * HTTP 超时时间（毫秒），0 或负数表示不超时
     */
    private Integer timeout;

    public void valid() {
        if (StrUtil.isEmpty(pushUrl)) {
            throw new BarkException("pushUrl is empty");
        }
        if (StrUtil.isEmpty(deviceKey)) {
            throw new BarkException("deviceKey is empty");
        }
        URI uri;
        try {
            uri = new URI(pushUrl);
        } catch (URISyntaxException e) {
            throw new BarkException("pushUrl is invalid: " + e.getMessage());
        }
        if (uri.getHost() == null || !("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()))) {
            throw new BarkException("pushUrl is invalid");
        }
    }
}
