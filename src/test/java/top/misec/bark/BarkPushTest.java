package top.misec.bark;

import org.junit.jupiter.api.Test;
import top.misec.bark.enums.SoundEnum;
import top.misec.bark.pojo.PushDetails;

import static org.junit.jupiter.api.Assertions.*;

class BarkPushTest {

    @Test
    void testBuilder() {
        BarkPush builder = new BarkPush("", "");
        assertNotNull(builder);

        PushDetails pushDetails = PushDetails.builder().title("test").sound(SoundEnum.BLOOM.name()).body("test").build();

        builder.simpleWithResp(pushDetails);


    }

}