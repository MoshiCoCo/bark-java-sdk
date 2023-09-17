package top.misec.bark.pojo;

import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class PushDetails {
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
}
