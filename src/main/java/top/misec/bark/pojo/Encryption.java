package top.misec.bark.pojo;


import cn.hutool.core.util.StrUtil;
import lombok.Builder;
import lombok.Data;
import top.misec.bark.exception.BarkException;

@Builder
@Data
public class Encryption {
    private String algorithm;
    private String mode;
    private String padding;
    private String key;
    private String iv;

    public void valid() {

        if (StrUtil.isEmpty(this.padding)) {
            this.padding = "PKC7Padding";
        }
        if (StrUtil.isEmpty(this.algorithm)) {
            this.mode = "AES";
        }

        if (StrUtil.isEmpty(this.mode)) {
            throw new BarkException("AES Mode is empty");
        }

        if (!"ECB".equals(this.mode) && !"CBC".equals(this.mode)) {
            throw new BarkException("AES Mode is invalid, only support ECB or CBC");
        }

        if (StrUtil.isNotEmpty(this.iv)) {
            if (this.iv.length() != 16) {
                throw new BarkException("AES IV length is invalid, only support 16");
            }
        }

        if (StrUtil.isEmpty(this.key)) {
            if (this.key.length() % 16 != 0) {
                throw new BarkException("AES Key length is invalid, only support AES128, AES192, AES256");
            }
        }
    }
}
