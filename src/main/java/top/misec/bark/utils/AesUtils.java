package top.misec.bark.utils;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.security.Security;

public class AesUtils {

    public static String KEY_ALGORITHM = "AES";

    public static final int MODE_ECB = 0;
    public static final int MODE_CBC = 1;
    public static final int MODE_GCM = 2;

    private static final int GCM_TAG_BITS = 128;

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static byte[] encrypt(byte[] originalContent, byte[] encryptKey, int model, byte[] ivByte) {
        return doFinal(Cipher.ENCRYPT_MODE, originalContent, encryptKey, model, ivByte);
    }

    public static byte[] decrypt(byte[] content, byte[] aesKey, int model, byte[] ivByte) {
        return doFinal(Cipher.DECRYPT_MODE, content, aesKey, model, ivByte);
    }

    private static byte[] doFinal(int cipherMode, byte[] data, byte[] aesKey, int model, byte[] ivByte) {
        try {
            Cipher cipher = Cipher.getInstance(getTransformation(model));
            Key secretKeySpec = new SecretKeySpec(aesKey, KEY_ALGORITHM);
            if (model == MODE_ECB) {
                cipher.init(cipherMode, secretKeySpec);
            } else if (model == MODE_GCM) {
                cipher.init(cipherMode, secretKeySpec, new GCMParameterSpec(GCM_TAG_BITS, ivByte));
            } else {
                cipher.init(cipherMode, secretKeySpec, new IvParameterSpec(ivByte));
            }
            return cipher.doFinal(data);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String getTransformation(int model) {
        return switch (model) {
            case MODE_ECB -> "AES/ECB/PKCS7Padding";
            case MODE_GCM -> "AES/GCM/NoPadding";
            default -> "AES/CBC/PKCS7Padding";
        };
    }
}
