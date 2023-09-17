package top.misec.bark.pojo;

import lombok.Data;

/**
 * @author moshi
 */
@Data
public class BarkPushResp {
    private Integer code;
    private String message;
    private Integer timestamp;
}
