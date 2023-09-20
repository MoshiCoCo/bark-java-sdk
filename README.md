# bark-java-sdk

## Using

```xml

<dependency>
    <groupId>top.misec</groupId>
    <artifactId>bark-java-sdk</artifactId>
    <version>0.0.3</version>
</dependency>
```

## Quick Start

### SimplePush

```shell
BarkPush pusher=new BarkPush("https://xxx.xxx.xx/push","xxxxx");
assertNotNull(pusher);
pusher.simplePush("hello word");
```

### EncryptedPush

```shell
Encryption encryption = Encryption.builder()
        .mode("ECB")
        .key("12345678901234561234567890123456")
        .build();
BarkPush pusher = new BarkPush("", "", encryption);
pusher.encryptionPush("123");
```

```shell
Encryption encryption = Encryption.builder()
        .mode("CBC")
        .key("12345678901234561234567890123456")
        .iv("1111111111111111")
        .build();

BarkPush pusher = new BarkPush("", "", encryption);
pusher.encryptionPush("123");
```