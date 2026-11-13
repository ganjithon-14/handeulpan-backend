# 말풍선 API (F4, BE-B)

공통 규칙은 [common.md](common.md)를 봅니다. 모든 요청에 `X-Device-Token`이 필요합니다.

## 규칙

- 양당 최신 1개만 유지합니다. 새로 올리면 이전 것을 교체합니다.
- 1~20자입니다. 앞뒤 공백은 떼고 세며, 공백만 있는 글은 안 됩니다. 이모지는 1글자로 셉니다.
- 24시간이 지나면 들판 조회 응답에서 빠집니다. 기준은 `bubbles.created_at`이고, 새로 올리면 시각도 새로 시작합니다. 필터는 BE-B가 `PastureViewExtension`으로 붙이는 `bubble` 필드에서 처리합니다.
- **내 양에만** 쓰고 지울 수 있습니다. 다른 사람 양이면 `403 NOT_MY_SHEEP`.
- 위험 키워드가 글에 포함되어 있으면 본인 확인 없이는 올리지 않습니다. 서버는 포함 여부만 봅니다.

## PUT /api/sheep/{id}/bubble — 말풍선 올리기

요청

```json
{ "text": "오늘 입사 100일", "confirm": false }
```

| 필드 | 타입 | 설명 |
|---|---|---|
| `text` | string | 1~20자 |
| `confirm` | boolean | 위험 키워드 안내를 보고도 그대로 올리겠다는 뜻. 생략하면 `false` |

응답 `200`

1. 일반 글 → 바로 게시

```json
{ "posted": true, "flagged": false }
```

2. 위험 키워드가 있고 `confirm: false` → **게시하지 않음**. 앱은 S8(도움 안내 카드)을 먼저 띄웁니다.

```json
{ "posted": false, "flagged": true }
```

3. 사용자가 "그대로 올릴래요"를 눌러 `confirm: true`로 다시 요청 → 게시되고 `flagged`로 남음

```json
{ "posted": true, "flagged": true }
```

`flagged: true`로 게시된 말풍선은 들판 조회 응답의 `bubble.flagged`에 `true`로 내려가고, 친구의 다가가기(S7)에서 말 걸기 가이드를 맨 위에 보여줍니다.

에러

| HTTP | code | 언제 |
|---|---|---|
| 400 | `BUBBLE_TEXT_INVALID` | 비었거나 20자 초과 |
| 403 | `NOT_MY_SHEEP` | 내 양이 아님 |
| 404 | `SHEEP_NOT_FOUND` | 없는 양 |

```json
{ "code": "BUBBLE_TEXT_INVALID", "message": "말풍선은 1~20자로 적어 주세요." }
```

## DELETE /api/sheep/{id}/bubble — 말풍선 지우기

요청 본문 없음. 응답 `204`, 본문 없음.

| HTTP | code | 언제 |
|---|---|---|
| 403 | `NOT_MY_SHEEP` | 내 양이 아님 |
| 404 | `SHEEP_NOT_FOUND` | 없는 양 |
| 404 | `BUBBLE_NOT_FOUND` | 지울 말풍선이 없음 (이미 지웠거나 24시간 지남) |

## 들판 조회 응답의 `bubble`

`GET /api/pastures/{id}`의 `sheep[]` 각 항목에 `bubble`이 붙습니다. 말풍선이 없거나 24시간이 지났으면 `null`입니다. 친구 양의 말풍선도 같은 형태로 내려갑니다.

```json
{
  "id": 3, "name": "콩이", "woolColor": "MINT", "distance": "CENTER", "mine": false,
  "bubble": { "text": "오늘 입사 100일", "flagged": false, "createdAt": "2026-11-10T00:00:00Z" }
}
```

## 정해진 것과 열린 질문

- 글자 수는 앞뒤 공백을 뗀 뒤 코드포인트(이모지 1글자)로 셉니다. (정해짐)
- 욕설·비방 필터링(기획서 9장)은 이 API에서 같은 방식으로 막을지, 위험 키워드와 분리할지 정해야 합니다. 정해지기 전에는 위험 키워드만 처리합니다.
- 위험 키워드 목록은 임시입니다. 팀이 정한 목록이 오면 교체합니다. 서버는 공백을 모두 없앤 글에서 포함 여부만 봅니다. (`죽고 싶다`와 `죽고싶다`는 같게 봅니다.)
- 응답에 메시지 본문은 돌려주지 않습니다. 앱은 방금 보낸 `text`를 그대로 씁니다.
