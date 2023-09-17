package top.misec.bark;

import org.junit.jupiter.api.Test;
import top.misec.bark.enums.SoundEnum;
import top.misec.bark.pojo.BarkPushResp;
import top.misec.bark.pojo.PushDetails;

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
    void simplePush() {
        BarkPush pusher = new BarkPush("", "");
        assertNotNull(pusher);
        pusher.simplePush("hello word");
    }

    @Test
    void simpleWithResp() {
        BarkPush pusher = new BarkPush("", "");
        assertNotNull(pusher);
        BarkPushResp resp = pusher.simpleWithResp("hello word");
        System.out.println(resp.toString());
    }


}