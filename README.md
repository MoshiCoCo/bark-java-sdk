<div align="center">
<h1 align="center">Bark-Java-SDK</h1>

[![GitHub stars](https://img.shields.io/github/stars/MoshiCoCo/bark-java-sdk?style=flat-square)](https://github.com/MoshiCoCo/bark-java-sdk/stargazers)
[![GitHub forks](https://img.shields.io/github/forks/MoshiCoCo/bark-java-sdk?style=flat-square)](https://github.com/MoshiCoCo/bark-java-sdk/network)
[![GitHub issues](https://img.shields.io/github/issues/MoshiCoCo/bark-java-sdk?style=flat-square)](https://github.com/MoshiCoCo/bark-java-sdk/issues)
[![GitHub license](https://img.shields.io/github/license/MoshiCoCo/bark-java-sdk?style=flat-square)](https://github.com/MoshiCoCo/bark-java-sdk/blob/main/LICENSE)
[![GitHub All Releases](https://img.shields.io/github/downloads/MoshiCoCo/bark-java-sdk/total?style=flat-square)](https://github.com/MoshiCoCo/bark-java-sdk/releases)
[![GitHub release (latest SemVer)](https://img.shields.io/github/v/release/MoshiCoCo/bark-java-sdk?style=flat-square)](https://github.com/MoshiCoCo/bark-java-sdk/releases)
[![Maven Central](https://img.shields.io/maven-central/v/top.misec/bark-java-sdk?style=flat-square)](https://github.com/MoshiCoCo/bark-java-sdk/packages)
[![Hits](https://hits.seeyoufarm.com/api/count/incr/badge.svg?url=https%3A%2F%2Fgithub.com%2FMoshiCoCo%2Fbark-java-sdk&count_bg=%2379C83D&title_bg=%23555555&icon=&icon_color=%23E7E7E7&title=hits&edge_flat=true)](https://hits.seeyoufarm.com)
</div>

## Using

```xml

<dependency>
    <groupId>top.misec</groupId>
    <artifactId>bark-java-sdk</artifactId>
    <version>0.0.5</version>
</dependency>
```

## Quick Start

### SimplePush

```java
BarkPush pusher = new BarkPush("https://xxx.xxx.xx/push", "xxxxx");
pusher.simpleWithResp("hello world");
```

### DetailPush

```java
PushDetails details = PushDetails.builder()
        .title("title")
        .subtitle("subtitle")
        .body("body")
        .sound(SoundEnum.BLOOM.getSoundName())
        .level("active")          // critical / active / timeSensitive / passive
        .badge(1)
        .ttl(3600)
        .url("https://mritd.com")
        .build();
pusher.simpleWithResp(details);
```

### BatchPush（多设备）

```java
BarkPushResp resp = pusher.batchWithResp(List.of("key1", "key2"), details);
resp.getData();  // 逐设备推送结果
```

### Basic Auth 与超时

服务端启用 `ARK_USER`/`ARK_PASSWORD` 时，使用 Builder 或 `BarkCfg`：

```java
BarkPush pusher = BarkPush.builder()
        .pushUrl("https://xxx.xxx.xx/push")
        .deviceKey("xxxxx")
        .username("user")
        .password("pass")
        .timeout(5000)   // 毫秒，不设置则不超时
        .build();
```

### EncryptedPush

支持 `ECB` / `CBC` / `GCM` 三种模式（与服务端及 Bark App 一致，密钥为 UTF-8 字符串原始字节，iv 以明文原样传输而非 base64）：

```java
// ECB：16/24/32 字符 key，无需 iv
Encryption ecb = Encryption.builder().mode("ECB").key("12345678901234561234567890123456").build();

// CBC：必须提供 16 字符 iv
Encryption cbc = Encryption.builder().mode("CBC")
        .key("12345678901234561234567890123456").iv("1111111111111111").build();

// GCM：可不提供 iv（每次推送自动生成 12 字符随机 iv），提供则须为 12 字符
Encryption gcm = Encryption.builder().mode("GCM")
        .key("12345678901234561234567890123456").build();

BarkPush pusher = new BarkPush("https://xxx.xxx.xx/push", "xxxxx", cbc);
pusher.encryptionPush("hello world");
```