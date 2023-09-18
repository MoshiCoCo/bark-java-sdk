package top.misec.bark.pojo;


import cn.hutool.core.util.StrUtil;
import lombok.Builder;
import lombok.Data;
import top.misec.bark.exception.BarkException;

@Data
@Builder
public class BarkCfg {
    private String pushUrl;
    private String deviceKey;
    private Encryption encryption;

    public void valid() {
        if (StrUtil.isEmpty(pushUrl)) {
            throw new BarkException("pushUrl is empty");
        }
        if (StrUtil.isEmpty(deviceKey)) {
            throw new BarkException("deviceKey is empty");
        }
        if (!pushUrl.matches("^(https?)://[\\\\w.-]+\\\\.\\\\w{2,4}(/.*)?$")) {
            throw new BarkException("pushUrl is invalid");
        }
    }
}
