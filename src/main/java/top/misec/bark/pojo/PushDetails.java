package top.misec.bark.pojo;

import lombok.Builder;
import lombok.Data;

/**
 * @author moshi
 */
@Data
@Builder
public class PushDetails {
    private String id;
    private String title;
    private String subtitle;
    private String body;
    private String level;
    private Integer badge;
    private String volume;
    private String call;
    private String autoCopy;
    private String copy;
    private String sound;
    private String icon;
    private String group;
    private String isArchive;
    private String category;
    private String url;
    private String action;
    private Integer ttl;
}
