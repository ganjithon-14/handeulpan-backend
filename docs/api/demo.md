# 데모 모드 API (D)

시연에서 "며칠 지나면 멀어진다"를 몇 초 만에 보여주기 위한 API입니다.

## POST /api/demo/sheep/{sheepId}/rewind — 시간 되감기

헤더 `X-Demo-Key` 필요 (기기 토큰은 필요 없음). 그 양의 마지막 쓰다듬은 시각을 `days`일 과거로 옮깁니다.

요청
```json
{ "days": 4 }
```
`days`: 1~30

응답 `200`
```json
{ "sheepId": 2, "distance": "EDGE" }
```

| HTTP | code | 언제 |
|---|---|---|
| 403 | `INVALID_DEMO_KEY` | 키가 없거나 틀림, 또는 서버에 데모 키가 설정되지 않음 |
| 404 | `SHEEP_NOT_FOUND` | 없는 양 |
| 400 | `VALIDATION_FAILED` | `days`가 1~30 밖 |

되돌리기: 그 양의 주인이 쓰다듬으면 바로 가운데로 돌아옵니다.

## POST /api/demo/seed — 시연 데이터 만들기·초기화 (BE-B)

헤더 `X-Demo-Key` 필요 (기기 토큰은 필요 없음). 데모 계정과 시연용 들판을 만듭니다. **이미 있으면 지우고 같은 상태로 다시 만들기** 때문에 몇 번을 불러도 결과가 같습니다. 시연 직전에 한 번 부르면 됩니다.

응답 `201`
```json
{
  "pastureId": "k3x9...",
  "password": "demo1234!",
  "members": [
    { "email": "doobu@demo.test", "sheepName": "두부", "sheepId": 1, "distance": "CENTER", "napping": false, "me": true },
    { "email": "kong@demo.test",  "sheepName": "콩이", "sheepId": 2, "distance": "CENTER", "napping": false, "me": false },
    ...
  ]
}
```

모든 데모 계정의 비밀번호는 `password` 값 하나입니다. 로그인은 평소처럼 `POST /api/auth/login`으로 합니다.

기획서 7장 시연 시나리오 A→B→C의 **시작 상태**입니다. (동기 4명, 폰 3대에 친구 양을 띄움)

| 양 | 이메일 | 털 색 | 상태 |
|---|---|---|---|
| 두부 | `doobu@demo.test` | CREAM | 가운데. 말풍선 "요즘 좀 지침"(위험 표시). 시연에서 가장자리로 보낼 양 (`me: true`) |
| 콩이 | `kong@demo.test` | MINT | 가운데. 말풍선 "오늘 입사 100일" |
| 보리 | `bori@demo.test` | PINK | 가운데 |
| 별이 | `byeol@demo.test` | YELLOW | 가운데 |

- 양은 4마리라 **두 자리가 비어 있습니다.** 링크(`/p/{pastureId}`)로 합류하는 장면을 보여 줄 수 있습니다.
- **목도리는 하나도 보내 두지 않았습니다.** B에서 친구 셋이 직접 보내고, C에서 두부가 받은 것 중 민트색을 고르는 장면을 그대로 보여 주기 위해서입니다.
- 말풍선은 24시간 뒤 사라집니다. 시연 직전에 이 API를 부르세요.
- 이름·털 색·말풍선을 바꾸려면 `DemoSeedService`를 고칩니다.

시연 흐름
1. **A** 친구들 폰으로 로그인해 들판을 보여 주고, 쓰다듬기·말풍선을 보여 줍니다.
2. **B** 두부를 4일 되감기(`POST /api/demo/sheep/{두부 sheepId}/rewind` `{"days":4}`)하면 친구들 홈에 "두부 양이 조금 멀리 갔어요" 배너가 뜹니다. 친구 셋이 각자 목도리를 보냅니다.
3. **C** 두부로 로그인하면 받은 목도리 셋이 보이고, 민트색을 골라 두른 뒤 쓰다듬으면 가운데로 돌아옵니다.

시연을 다시 하려면 이 API를 한 번 더 부르면 처음 상태로 돌아갑니다.

| HTTP | code | 언제 |
|---|---|---|
| 403 | `INVALID_DEMO_KEY` | 키가 없거나 틀림, 또는 서버에 데모 키가 설정되지 않음 |

## DELETE /api/demo/seed — 시연 데이터 지우기

헤더 `X-Demo-Key` 필요. 응답 `204`. 데모 계정(이메일이 `@demo.test`로 끝나는 계정)과 그 들판, 거기에 있던 양·말풍선·목도리를 모두 지웁니다. 데모 데이터가 없어도 `204`입니다. 일반 사용자 계정과 다른 들판은 건드리지 않습니다.

## 데모 키

| 환경 | 값 |
|---|---|
| local (`./gradlew bootRun`) | `local-demo` |
| 배포 서버 | 환경변수 `HANDEULPAN_DEMO_KEY` (비워 두면 데모 API 꺼짐) |
