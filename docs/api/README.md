# API 명세 (요청·응답 예시)

11/6 명세 확정 관문까지 여기에 엔드포인트별 예시를 고정합니다. 프론트는 이 JSON으로 목업을 만들어 먼저 개발합니다.

## 공통

- Base URL: `https://handeulpan.p-e.kr`
- Swagger UI: `https://handeulpan.p-e.kr/swagger-ui/index.html`, OpenAPI 문서: `https://handeulpan.p-e.kr/v3/api-docs`
- 인증 헤더: `X-Device-Token: {UUID}`
- 공통 응답·에러 형식·에러 코드: [common.md](common.md)
- BE-A 담당 상세: [pasture.md](pasture.md), [sheep.md](sheep.md), [demo.md](demo.md) (구현된 경로)
- BE-B 담당 상세: [auth.md](auth.md), [bubble.md](bubble.md), [scarf.md](scarf.md) (개발 명세 기준 경로)

## 엔드포인트 목록 (초안)

| 기능 | 메서드 | 경로 | 담당 | 상태 |
|---|---|---|---|---|
| 가입 (닉네임·이메일·비밀번호, 토큰 발급) | POST | `/api/auth/signup` | BE-B | 구현됨 |
| 로그인 (새 토큰 발급) | POST | `/api/auth/login` | BE-B | 구현됨 |
| 로그아웃 (토큰 무효화) | POST | `/api/auth/logout` | BE-B | 구현됨 |
| F1 들판 만들기 (내 양 포함) | POST | `/api/pastures` | BE-A | 구현됨 |
| F1 들판 미리보기 (링크 구경, 토큰 불필요) | GET | `/api/pastures/{pastureId}/preview` | BE-A | 구현됨 |
| F1 들판 합류 (양 만들기·동의) | POST | `/api/pastures/{pastureId}/sheep` | BE-A | 구현됨 |
| 들판 조회 (화면 전체) | GET | `/api/pastures/{pastureId}` | BE-A | 구현됨 |
| 내 들판 목록 (홈) | GET | `/api/pastures` | BE-A | 구현됨 |
| F2 쓰다듬기 | POST | `/api/sheep/{sheepId}/pat` | BE-A | 구현됨 |
| F4 말풍선 올리기 (위험 키워드 검사) | PUT | `/api/sheep/{sheepId}/bubble` | BE-B | 구현됨 |
| F4 말풍선 지우기 | DELETE | `/api/sheep/{sheepId}/bubble` | BE-B | 구현됨 |
| F7 목도리 보내기 | POST | `/api/sheep/{sheepId}/scarves` | BE-B | 구현됨 |
| F7 목도리 두르기·벗기 (받은 목록은 들판 조회의 `myScarves`) | PUT | `/api/sheep/{sheepId}/scarf` | BE-B | 구현됨 |
| D 데모 되감기 (`X-Demo-Key`) | POST | `/api/demo/sheep/{sheepId}/rewind` | BE-A | 구현됨 |

말풍선·목도리 경로는 구현된 경로입니다.

## 상세 문서

```
docs/api/
  common.md     # 인증·에러 형식·에러 코드
  auth.md       # 가입·로그인·로그아웃
  pasture.md    # F1 들판, 들판 조회
  sheep.md      # F2 쓰다듬기
  bubble.md     # F4 말풍선
  scarf.md      # F7 목도리
  demo.md       # D 데모 되감기
```
