# 공통 규칙

> 기준: 개발 명세(MVP) 4장. 용어와 경로는 BE-A 코드(`pasture`, `CENTER` / `WALK` / `EDGE`, `/api/sheep/...`)를 따릅니다.

## 인증

- 모든 요청에 헤더 `X-Device-Token: {UUID}`를 보냅니다.
- 회원만 이용할 수 있습니다. 토큰은 `POST /api/auth/signup`(가입) 또는 `/api/auth/login`(로그인)으로 받아 브라우저에 저장합니다. → [auth.md](auth.md)
- 토큰이 곧 사용자입니다. JWT는 쓰지 않습니다.
- 기기를 바꿔도 로그인하면 같은 양·들판으로 돌아옵니다.
- 헤더가 없거나 등록되지 않은 토큰이면 `401 INVALID_DEVICE_TOKEN`.

## 응답 형식

- 성공: 본문은 엔드포인트별 JSON 그대로입니다. 별도 래퍼(`data` 등)로 감싸지 않습니다. 본문이 없으면 `204`.
- 시각은 서버 시간(Asia/Seoul)을 기준으로 계산하고, 응답에는 마지막 쓰다듬은 시각을 넣지 않습니다. (사생활 보호)

## 에러 형식

```json
{ "code": "PASTURE_FULL", "message": "이 들판은 가득 찼어요." }
```

- `code`: 프론트가 분기에 쓰는 고정 문자열 (대문자 스네이크)
- `message`: 사람이 읽는 문장. 프론트는 그대로 보여주지 않아도 됩니다.
- 검증 오류도 같은 형식을 씁니다.

## 에러 코드

| HTTP | code | 언제 | 담당 |
|---|---|---|---|
| 401 | `INVALID_DEVICE_TOKEN` | 토큰이 없거나 모르는 값 | 공통 |
| 400 | `VALIDATION_FAILED` | 요청 형식이 잘못됨 (필수값 누락, 길이 초과 등) | 공통 |
| 500 | `INTERNAL_ERROR` | 예상하지 못한 서버 오류. 내부 메시지는 노출하지 않음 | 공통 |
| 404 | `NOT_FOUND` | 없는 주소 | 공통 |
| 405 | `METHOD_NOT_ALLOWED` | 그 주소가 지원하지 않는 HTTP 메서드 | 공통 |
| 404 | `PASTURE_NOT_FOUND` | 없는 들판 | BE-A |
| 404 | `SHEEP_NOT_FOUND` | 없는 양 | 공통 |
| 403 | `NOT_PASTURE_MEMBER` | 그 들판의 멤버가 아님 | 공통 |
| 403 | `NOT_MY_SHEEP` | 내 양이 아닌데 내 양만 가능한 API를 호출 | 공통 |
| 409 | `PASTURE_FULL` | 들판이 6마리로 가득 참 | BE-A |
| 409 | `ALREADY_JOINED` | 이미 이 들판에 합류함 | BE-A |
| 400 | `BUBBLE_TEXT_INVALID` | 말풍선이 비었거나 20자 초과 | BE-B |
| 404 | `BUBBLE_NOT_FOUND` | 지울 말풍선이 없음 | BE-B |
| 409 | `ALREADY_SENT_TODAY` | 같은 친구에게 오늘 이미 목도리를 보냄 | BE-B |
| 400 | `SELF` | 자기 자신에게 목도리를 보내려 함 | BE-B |
| 404 | `SCARF_NOT_FOUND` | 내가 받은 목도리가 아님 | BE-B |
| 403 | `INVALID_DEMO_KEY` | 데모 API의 `X-Demo-Key`가 틀림 | BE-A |
| 409 | `DUPLICATE_EMAIL` | 이미 가입된 이메일로 가입 | BE-B |
| 409 | `DUPLICATE_NICKNAME` | 이미 쓰는 닉네임으로 가입 | BE-B |
| 401 | `INVALID_CREDENTIALS` | 로그인 이메일이 없거나 비밀번호가 틀림 | BE-B |

`PASTURE_FULL`, `ALREADY_JOINED`, `ALREADY_SENT_TODAY`, `SELF`는 명세에 있는 값이고, 나머지는 BE-B가 제안한 이름입니다.

## CORS

- 브라우저(프론트)에서 호출할 수 있는 출처는 서버 환경변수 `CORS_ALLOWED_ORIGINS`로 정합니다. 쉼표로 여러 개를 적고 `https://*.vercel.app` 같은 와일드카드도 됩니다. 기본값은 `http://localhost:5173`입니다.
- 허용 메서드는 GET, POST, PUT, DELETE이고, 허용 헤더는 `Content-Type`, `Accept`, `X-Device-Token`, `X-Demo-Key`입니다.
- 인증이 쿠키가 아니라 헤더라서 `credentials`는 쓰지 않습니다. 프론트는 `fetch`에 `credentials: 'include'`를 넣지 마세요.
