package com.ganjithon.handeulpan.demo;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 데모 API 키. 비어 있으면 데모 API는 항상 403(꺼짐).
 * 배포 서버에서는 환경변수 HANDEULPAN_DEMO_KEY로 넣습니다.
 */
@ConfigurationProperties("handeulpan.demo")
public record DemoProperties(String key) {
}
