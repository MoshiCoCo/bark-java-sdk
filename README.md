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