package top.misec.bark.pojo;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author moshi
 */

@Builder
@Data
public class PushRequest {
    private String id;
    @JSONField(name = "device_key")
    private String deviceKey;
    @JSONField(name = "device_keys")
    private List<String> deviceKeys;
    private String title;
    private String subtitle;
    private String body;
    private String sound;
    private String icon;
    private String group;
    private String url;
    private String action;
    private String level;
    private String volume;
    private String call;
    private String copy;
    private String autoCopy;
    private String isArchive;
    private String category;
    private Integer badge;
    private Integer ttl;
    private String ciphertext;
    private String iv;
}
