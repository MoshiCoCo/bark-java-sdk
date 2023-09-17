package top.misec.bark.pojo;


import lombok.Builder;
import lombok.Data;
import top.misec.bark.exception.BarkException;

@Data
@Builder
public class BarkCfg {
    private String pushUrl;
    private String deviceKey;

    public void valid() {
        if (pushUrl == null || deviceKey == null) {
            throw new BarkException("pushUrl or deviceKey is null");
        }
        if (!pushUrl.startsWith("https://") && !pushUrl.startsWith("http://")) {
            throw new BarkException("pushUrl must start with https:// or http://");
        }
    }
}
