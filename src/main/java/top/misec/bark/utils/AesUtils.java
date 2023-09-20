package top.misec.bark.utils;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.security.Security;

public class AesUtils {

    public static String KEY_ALGORITHM = "AES";

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static byte[] encrypt(byte[] originalContent, byte[] encryptKey, int model, byte[] ivByte) {

        try {
            Cipher cipher = Cipher.getInstance(getTransformation(model));
            SecretKeySpec secretKeySpec = new SecretKeySpec(encryptKey, KEY_ALGORITHM);
            if (model == 0) {
                cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            }
            if (model == 1) {
                cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, new IvParameterSpec(ivByte));
            }
            return cipher.doFinal(originalContent);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String getTransformation(int model) {

        if (model == 0) {
            return "AES/ECB/PKCS7Padding";
        }
        return "AES/CBC/PKCS7Padding";
    }


    public static byte[] decrypt(byte[] content, byte[] aesKey, int model, byte[] ivByte) {

        try {
            Cipher cipher = Cipher.getInstance(getTransformation(model));
            Key secretKeySpec = new SecretKeySpec(aesKey, KEY_ALGORITHM);
            if (model == 0) {
                cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            }
            if (model == 1) {
                cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, new IvParameterSpec(ivByte));
            }
            return cipher.doFinal(content);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
