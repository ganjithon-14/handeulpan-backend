package com.ganjithon.handeulpan.bubble.dto;

/**
 * @param text    말풍선 글. 비었는지·길이는 서비스에서 확인합니다. (에러 코드를 BUBBLE_TEXT_INVALID로 통일하려고)
 * @param confirm 위험 키워드 안내를 보고도 그대로 올리겠다는 뜻. 생략하면 false.
 *                (원시 타입 boolean이면 생략했을 때 JSON 파싱이 실패해서 Boolean으로 받습니다.)
 */
public record BubbleRequest(String text, Boolean confirm) {

    public boolean confirmed() {
        return Boolean.TRUE.equals(confirm);
    }
}
