# 들판 API (F1, BE-A)

공통 규칙·에러 형식은 [common.md](common.md)를 따릅니다.

토큰은 가입·로그인으로 받습니다. 받은 값을 브라우저에 저장하고 이후 모든 요청에 `X-Device-Token` 헤더로 보냅니다. → [auth.md](auth.md)
양 이름은 가입할 때가 아니라 **들판을 만들거나 합류할 때** 정합니다.

## POST /api/pastures — 들판 만들기

들판과 **만든 사람의 양**을 함께 만듭니다. 토큰 필요.

요청
```json
{ "name": "대학 동기", "sheepName": "두부", "woolColor": "CREAM", "agreed": true }
```

| 필드 | 타입 | 설명 |
|---|---|---|
| `name` | string \| null | 들판 이름, 0~20자, 생략 가능 |
| `sheepName` | string | 내 양 이름, 1~10자 |
| `woolColor` | string | `CREAM` `PINK` `YELLOW` `MINT` `SKY` `LAVENDER` |
| `agreed` | boolean | "멀어지면 들판 친구들이 알 수 있어요" 동의. `true`여야 함 |

응답 `201`
```json
{ "pastureId": "o76WTwo0Bc2ygE3Hpx9kkQ", "sheepId": 1 }
```
`pastureId`는 추측할 수 없는 22자 공개 ID입니다. 초대 링크는 `/p/{pastureId}`.

## GET /api/pastures/{pastureId}/preview — 링크로 구경하기

**토큰 없이** 호출합니다. 합류 전 화면용이라 양 id는 내보내지 않습니다.

응답 `200`
```json
{
  "pastureId": "o76WTwo0Bc2ygE3Hpx9kkQ",
  "name": "대학 동기",
  "maxSheep": 6,
  "full": false,
  "sheep": [
    { "name": "두부", "woolColor": "CREAM", "distance": "CENTER", "napping": false }
  ]
}
```

## POST /api/pastures/{pastureId}/sheep — 합류 (내 양 풀어놓기)

요청
```json
{ "sheepName": "콩이", "woolColor": "MINT", "agreed": true }
```
응답 `201`
```json
{ "pastureId": "o76WTwo0Bc2ygE3Hpx9kkQ", "sheepId": 2 }
```

| HTTP | code | 언제 |
|---|---|---|
| 400 | `VALIDATION_FAILED` | 이름 비었음/10자 초과, 털 색 오류, `agreed`가 `false` |
| 404 | `PASTURE_NOT_FOUND` | 없는 들판 |
| 409 | `ALREADY_JOINED` | 이미 이 들판에 양이 있음 |
| 409 | `PASTURE_FULL` | 6마리 가득 |

## GET /api/pastures/{pastureId} — 들판 조회 (화면 전체)

토큰 필요, 그 들판 멤버만. 이 응답 하나로 들판 화면을 그립니다.

응답 `200`
```json
{
  "pastureId": "o76WTwo0Bc2ygE3Hpx9kkQ",
  "name": "대학 동기",
  "maxSheep": 6,
  "mySheepId": 1,
  "sheep": [
    { "id": 1, "name": "두부", "woolColor": "CREAM", "distance": "CENTER", "mine": true, "pattedToday": true, "napping": false },
    { "id": 2, "name": "콩이", "woolColor": "MINT", "distance": "EDGE", "mine": false, "napping": false }
  ]
}
```

| 필드 | 설명 |
|---|---|
| `sheep[]` | 합류한 순서. 내 양 포함 |
| `distance` | `CENTER` 가운데 / `WALK` 산책 중 / `EDGE` 가장자리 → 친구 화면에 배너(F6) |
| `mine` | 내 양이면 `true` |
| `pattedToday` | **내 양에만** 있음. 오늘(Asia/Seoul) 쓰다듬었는지 |
| `napping` | 낮잠 중이면 `true`. 친구에게도 보임. 끝나는 시각은 내보내지 않음 → [sheep.md](sheep.md) |

- 마지막 쓰다듬은 시각, 접속 시각은 **내보내지 않습니다**. (기획서 9장 감시 방지)
- 말풍선(`bubble`), 목도리(`wornScarf`, `sentScarfToday`, `myScarves`)는 BE-B 기능이 들어오면 같은 응답에 붙습니다. → [bubble.md](bubble.md), [scarf.md](scarf.md)

| HTTP | code | 언제 |
|---|---|---|
| 403 | `NOT_PASTURE_MEMBER` | 이 들판 멤버가 아님 |
| 404 | `PASTURE_NOT_FOUND` | 없는 들판 |

## GET /api/pastures — 내 들판 목록 (홈)

최근 합류 순.

응답 `200`
```json
[
  {
    "pastureId": "o76WTwo0Bc2ygE3Hpx9kkQ",
    "name": "대학 동기",
    "mySheepId": 1,
    "myDistance": "CENTER",
    "pattedToday": true,
    "napping": false,
    "edgeFriendNames": ["콩이"]
  }
]
```
`edgeFriendNames`: 가장자리에 간 친구 양 이름. 앱을 열 때 "콩이 양이 조금 멀리 갔어요. 연락해볼까요?" 배너에 씁니다.

---

## BE-B 연동 (말풍선·목도리 붙이기)

BE-A 코드를 고치지 않고 붙일 수 있게 열어 둔 것들입니다.

| 쓰는 것 | 어디 | 용도 |
|---|---|---|
| `@CurrentUser User me` | `user.auth` | 컨트롤러 파라미터에 붙이면 토큰으로 찾은 사용자. 없으면 401 |
| `SheepFinder` | `sheep` | `requireMine(me, id)` 내 양만 / `requireVisibleTo(me, id)` 같은 들판 멤버의 양 / `requireMineInSamePasture(me, sheep)` 그 들판에서의 내 양 |
| `PastureViewExtension` | `pasture` | 들판 조회 응답에 필드 덧붙이기. 자기 패키지에 `@Component`로 구현 |
| `Clock` 빈 | `common.config` | Asia/Seoul. "하루 1회", 24시간 만료 계산에 사용 |
| `WoolColor` | `sheep` | 목도리 색도 이 enum 그대로 |
| `CoreError` | `pasture` | BE-A 에러 코드. 공통 에러 핸들러가 생기면 `ResponseStatusException.reason`(=code)을 `{code, message}`로 바꿔 주세요. message는 `CoreError.valueOf(code).message()` |
