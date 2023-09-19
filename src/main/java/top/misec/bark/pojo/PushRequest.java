package top.misec.bark.pojo;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Builder;
import lombok.Data;

/**
 * @author moshi
 */
@Builder
@Data
public class PushRequest {
    private String title;
    private String body;
    private String level;
    private String badge;
    private String autoCopy;
    private String copy;
    private String sound;
    private String icon;
    private String group;
    private String isArchive;
    private String category;
    private String url;
    @JSONField(name = "device_key")
    private String deviceKey;
}
