package top.misec.bark.pojo;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

import java.util.List;

/**
 * @author moshi
 */
@Data
public class BarkPushResp {
    private Integer code;
    private String message;
    private Long timestamp;
    /**
     * 批量推送时服务端返回的逐设备结果，单设备推送为 null
     */
    private List<BatchResult> data;

    @Data
    public static class BatchResult {
        private Integer code;
        private String message;
        @JSONField(name = "device_key")
        private String deviceKey;
    }
}
