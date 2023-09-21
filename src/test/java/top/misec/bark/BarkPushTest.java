package top.misec.bark;

import org.junit.jupiter.api.Test;
import top.misec.bark.enums.SoundEnum;
import top.misec.bark.pojo.BarkPushResp;
import top.misec.bark.pojo.Encryption;
import top.misec.bark.pojo.PushDetails;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class BarkPushTest {

    @Test
    void detailPushWithResp() {
        BarkPush builder = new BarkPush("", "");
        assertNotNull(builder);
        PushDetails pushDetails = PushDetails.builder().title("test").sound(SoundEnum.BLOOM.name()).body("test").build();
        builder.simpleWithResp(pushDetails);
    }


    @Test
    void simpleWithResp() {
        BarkPush pusher = new BarkPush("", "");
        assertNotNull(pusher);
        BarkPushResp resp = pusher.simpleWithResp("hello word");
        System.out.println(resp.toString());
    }

    @Test
    void simpleWithRespBuild() {
        BarkPush pusher = new BarkPush("https://api.day.app/push", "xyz");
        assertNotNull(pusher);
        BarkPushResp resp = pusher.simpleWithResp("hello word");
        assertEquals(400, resp.getCode());
        assertEquals("failed to get device token: failed to get [xyz] device token from database", resp.getMessage());
    }

    @Test
    void encryptionPush() {

        Encryption encryption = Encryption.builder()
                .key("12345678901234561234567890123456")
                .iv("1111111111111111")
                .mode("CBC")
                .build();

        BarkPush pusher = new BarkPush("", "", encryption);
        BarkPushResp resp = pusher.encryptionPush("123");

    }

    @Test
    void encryptionPushECB() {

        Encryption encryption = Encryption.builder()
                .key("12345678901234561234567890123456")
                .iv("1111111111111111")
                .mode("ECB")
                .build();
        BarkPush pusher = new BarkPush("", "", encryption);
        BarkPushResp resp = pusher.encryptionPush("123");
    }


}