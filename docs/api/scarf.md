# 목도리 API (F7, BE-B)

공통 규칙은 [common.md](common.md)를 봅니다. 모든 요청에 `X-Device-Token`이 필요합니다.

## 규칙

- 목도리 색은 **보내는 양의 털 색**으로 정해집니다. 색 이름은 `CREAM`, `PINK`, `YELLOW`, `MINT`, `SKY`, `LAVENDER`.
- 같은 친구(`from → to`)에게 하루 1회. 하루는 Asia/Seoul 기준 날짜입니다.
- 자기 자신에게는 못 보냅니다.
- 가장자리가 아닌 양에게도 보낼 수 있습니다.
- 같은 들판의 멤버끼리만 보낼 수 있습니다.
- 받은 목도리는 만료되지 않고 모두 보관됩니다. 개수, 보낸 사람, 보낸 시각은 화면에 내보내지 않습니다.
- 받으면 **가장 최근 것을 자동으로 두릅니다.** 받는 양의 `worn_scarf`가 새 목도리로 바뀝니다.
- 받은 목도리 목록은 별도 API 없이 들판 조회(`GET /api/pastures/{id}`)의 `myScarves`로 내려갑니다. (BE-A 응답에 BE-B 데이터를 포함, 아래 "들판 조회 응답")

## POST /api/sheep/{id}/scarves — 목도리 보내기

`{id}`는 **받는 양**입니다. 보내는 양은 토큰의 사용자가 그 들판에서 가진 양으로 정해집니다.

요청 본문 없음.

응답 `201`

```json
{ "color": "PINK" }
```

`color`는 보낸 목도리의 색(= 내 양의 털 색)입니다.

에러

| HTTP | code | 언제 |
|---|---|---|
| 400 | `SELF` | 내 양에게 보내려 함 |
| 403 | `NOT_PASTURE_MEMBER` | 받는 양의 들판 멤버가 아님 |
| 404 | `SHEEP_NOT_FOUND` | 없는 양 |
| 409 | `ALREADY_SENT_TODAY` | 오늘 이미 이 친구에게 보냄 |

```json
{ "code": "ALREADY_SENT_TODAY", "message": "오늘은 이미 목도리를 보냈어요." }
```

들판 조회의 `sentScarfToday`는 이 규칙으로 계산한 값입니다. 앱은 `true`인 친구의 목도리 버튼을 막으면 됩니다.

## PUT /api/sheep/{id}/scarf — 목도리 두르기·벗기

`{id}`는 **내 양**입니다. 내 양만 바꿀 수 있습니다.

요청

```json
{ "scarfId": 31 }
```

벗기:

```json
{ "scarfId": null }
```

| 필드 | 타입 | 설명 |
|---|---|---|
| `scarfId` | number \| null | 들판 조회의 `myScarves[].id`. `null`이면 벗음 |

응답 `200`

```json
{ "wornScarf": "MINT" }
```

벗었으면 `{ "wornScarf": null }`.

에러

| HTTP | code | 언제 |
|---|---|---|
| 403 | `NOT_MY_SHEEP` | 내 양이 아님 |
| 404 | `SHEEP_NOT_FOUND` | 없는 양 |
| 404 | `SCARF_NOT_FOUND` | 내 양이 받은 목도리가 아님 |

## 들판 조회 응답에 붙는 필드

`GET /api/pastures/{id}`에 아래가 붙습니다.

| 위치 | 필드 | 설명 |
|---|---|---|
| `sheep[]` 모든 양 | `wornScarf` | 지금 두른 목도리 색(`CREAM` 등). 안 둘렀으면 `null`. 친구에게도 보입니다. |
| `sheep[]` **내 양이 아닌** 양 | `sentScarfToday` | 오늘 내가 이 친구에게 이미 보냈으면 `true`. 내 양에는 이 필드가 없습니다. |
| 최상위 | `myScarves` | 내가 받은 목도리 `[{ "id": 31, "color": "MINT" }]`. 최근 것이 먼저, 없으면 `[]` |

```json
{
  "myScarves": [ { "id": 31, "color": "MINT" }, { "id": 27, "color": "CREAM" } ],
  "sheep": [
    { "id": 1, "name": "두부", "mine": true, "wornScarf": "MINT" },
    { "id": 2, "name": "콩이", "mine": false, "wornScarf": null, "sentScarfToday": true }
  ]
}
```

- **같은 색은 가장 최근 것 하나만** `myScarves`에 내려갑니다. 받은 개수가 드러나지 않게 하려는 것이라 목록은 최대 6개(색 수)입니다. 보낸 사람과 보낸 시각은 내려가지 않습니다.
- 받은 목도리는 서버에 모두 보관됩니다. `myScarves[].id`로 `PUT /api/sheep/{id}/scarf`를 호출하면 그 목도리를 두릅니다.
- 보내는 쪽의 `sentScarfToday`가 `true`가 되어도, 받는 쪽 화면에는 보낸 쪽이 보냈는지 드러나지 않습니다.

## 정해진 것과 열린 질문

- "하루"는 **Asia/Seoul 날짜** 기준입니다. 자정(00:00)이 지나면 다시 보낼 수 있습니다. (정해짐)
- 목도리를 벗어 둔 뒤 새 목도리를 받으면 **다시 자동으로 두릅니다.** (정해짐)
- 보내기 성공 응답은 `201`입니다. 명세에 상태 코드가 없어 `200`으로 바꿔도 됩니다.
