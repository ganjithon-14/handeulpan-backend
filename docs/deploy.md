# 배포 (EC2 + Docker Compose)

서버 한 대에서 앱, PostgreSQL, Caddy(HTTPS)를 `docker compose`로 같이 띄웁니다.
외부에 열리는 포트는 80/443뿐이고, 앱(8080)과 DB(5432)는 Docker 내부에서만 보입니다.

## 처음 한 번

1. EC2 보안 그룹: 22(SSH), 80, 443만 허용합니다. 8080과 5432는 열지 않습니다.
2. 서버에 Docker와 Compose 플러그인을 설치하고, 도메인(DNS A 레코드)을 서버 IP에 연결합니다.
3. 레포를 받고 환경 파일을 만듭니다.

```bash
git clone https://github.com/ganjithon-14/handeulpan-backend.git
cd handeulpan-backend
git checkout dev
cp .env.example .env
nano .env   # DOMAIN, POSTGRES_PASSWORD, HANDEULPAN_DEMO_KEY 채우기
```

4. 실행합니다. 첫 빌드는 몇 분 걸립니다.

```bash
docker compose up -d --build
```

## 확인

```bash
docker compose ps                 # 세 컨테이너가 Up인지
docker compose logs -f app        # 앱 로그
curl -X POST https://{DOMAIN}/api/auth/signup -H 'Content-Type: application/json' \
  -d '{"nickname":"확인용","email":"check@example.com","password":"pass1234!"}'
```

`{"deviceToken": "..."}`가 오면 성공입니다. 확인용 계정이 DB에 남으니, 시연 전에 데이터를 정리할 때 함께 지웁니다.

## 업데이트 배포

```bash
git pull
docker compose up -d --build
```

## 주의

- `SPRING_PROFILES_ACTIVE=prod`는 compose에 이미 들어 있습니다. 빠지면 앱이 H2(메모리 DB)와 공개된 데모 키(`local-demo`)로 뜹니다.
- `.env`와 `.pem` 키는 커밋하지 않습니다.
- DB 데이터는 Docker 볼륨(`pgdata`)에 있습니다. `docker compose down -v`는 데이터를 지우니 쓰지 않습니다.
- 서버 메모리가 작으면(2GB) 빌드 중에 느려지거나 실패할 수 있습니다. swap을 2GB 만들어 두세요.
