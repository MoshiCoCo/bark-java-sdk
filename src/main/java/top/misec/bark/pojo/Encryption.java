package top.misec.bark.pojo;


import cn.hutool.core.util.StrUtil;
import lombok.Builder;
import lombok.Data;
import top.misec.bark.exception.BarkException;

import java.nio.charset.StandardCharsets;

@Builder
@Data
public class Encryption {
    private String algorithm;
    private String mode;
    private String padding;
    private String key;
    /**
     * 以原始字符串明文随请求发送（不是 base64）：CBC 须 16 字符，GCM 须 12 字符；
     * GCM 未设置时每次推送自动生成随机 iv
     */
    private String iv;

    public void valid() {

        if (StrUtil.isEmpty(this.padding)) {
            this.padding = "PKCS7Padding";
        }
        if (StrUtil.isEmpty(this.algorithm)) {
            this.algorithm = "AES";
        }

        if (StrUtil.isEmpty(this.mode)) {
            throw new BarkException("AES Mode is empty");
        }

        if (!"ECB".equals(this.mode) && !"CBC".equals(this.mode) && !"GCM".equals(this.mode)) {
            throw new BarkException("AES Mode is invalid, only support ECB, CBC or GCM");
        }

        if ("CBC".equals(this.mode) && StrUtil.isEmpty(this.iv)) {
            throw new BarkException("AES IV is required in CBC mode");
        }

        if (StrUtil.isNotEmpty(this.iv)) {
            int ivLen = this.iv.getBytes(StandardCharsets.UTF_8).length;
            int expected = "GCM".equals(this.mode) ? 12 : 16;
            if (ivLen != expected) {
                throw new BarkException("AES IV length is invalid, " + this.mode + " mode requires " + expected);
            }
        }

        if (StrUtil.isEmpty(this.key)) {
            throw new BarkException("AES Key is empty");
        }
        int keyLen = this.key.getBytes(StandardCharsets.UTF_8).length;
        if (keyLen != 16 && keyLen != 24 && keyLen != 32) {
            throw new BarkException("AES Key length is invalid, only support AES128, AES192, AES256");
        }
    }
}
