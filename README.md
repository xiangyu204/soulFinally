
GitHub 다운로드부터 설치·실행까지의 전체 절차입니다.

## 0. 저장소 구성

| 폴더 | 내용 | 설치 방식 |
|---|---|---|
| `db/` | `soul-schema-20261105.sql`(테이블 구조만), `soul-20261105.sql`(데이터 포함) | MariaDB에 import |
| `backend/soul-backend` | Spring Boot 3.2.5 / Java 17 / MariaDB, 포트 8080 | `soul-backend.jar` 실행 또는 Maven 빌드 |
| `wangye/SOUL-Web` | React 19 + Vite 웹 | `dist-standalone` 실행 또는 소스 빌드 |
| `app/` | `SOUL-优化1.1.apk` 및 `soul-android` 소스 | APK 설치 |

## 1. 사전 준비

- Git
- JDK 17
- MariaDB 10.x (기본 포트 3306, DB 이름 `soul`)
- Maven (백엔드를 소스에서 빌드할 때만 필요)
- Node.js 20+ (웹을 소스에서 빌드할 때만 필요). `启动.bat`만 실행한다면 Python 3.8+ 또는 Node.js 중 하나면 충분
- 외부 서비스 키: DeepSeek(AI), Agora(음성/영상 통화)

## 2. 코드 다운로드

```bash
git clone https://github.com/xiangyu204/soulFinally.git
cd soulFinally
```

Git이 없으면 GitHub 페이지에서 **Code → Download ZIP**으로 받아 압축을 풉니다.

## 3. 데이터베이스 설정

1. MariaDB에 접속해 데이터베이스를 생성합니다.
   ```sql
   CREATE DATABASE soul DEFAULT CHARACTER SET utf8mb4;
   ```
2. SQL을 import합니다.
   - 빈 DB로 시작: `db/soul-schema-20261105.sql`
   - 기존 데이터 복원: `db/soul-20261105.sql`
   ```bash
   mysql -u root -p soul < db/soul-20261105.sql
   ```
3. 백엔드에서 사용할 DB 계정과 비밀번호를 준비합니다.

> 참고: `spring.jpa.hibernate.ddl-auto=update`가 설정되어 있어, 백엔드를 처음 실행할 때 테이블이 자동 생성되기도 합니다.

## 4. 백엔드 설치 (포트 8080)

설정 파일: `backend/soul-backend/src/main/resources/application.properties`
환경에 맞게 아래 항목을 수정하세요. **비밀번호와 키는 Git에 올리지 마세요.**

| 설정 항목 | 설명 |
|---|---|
| `spring.datasource.url` | 기본값 `jdbc:mariadb://localhost:3306/soul?...` |
| `spring.datasource.username` / `password` | 3단계에서 만든 DB 계정 |
| `agora.app.id` / `agora.app.certificate` | Agora 콘솔에서 발급 |
| `deepseek.api.key` | DeepSeek API 키. 기본값이 비어 있어 반드시 추가해야 AI 기능이 동작합니다. 환경변수 `DEEPSEEK_API_KEY`로도 지정 가능 |
| `deepseek.api.url` / `deepseek.model` | 선택. 기본값 `https://api.deepseek.com/chat/completions` / `deepseek-chat` |
| `app.upload.dir` | 업로드 파일 저장 경로. 실제 존재하는 경로로 변경 |
| `server.port` | 기본값 8080 |

**방법 A: 빌드된 jar 실행** (`backend/soul-backend/soul-backend.jar`)

```bash
java -jar soul-backend.jar --spring.datasource.password=<DB비밀번호> --deepseek.api.key=<DeepSeek키>
```

**방법 B: 소스에서 빌드**

```bash
cd backend/soul-backend
mvn clean package -DskipTests
java -jar target/soul-backend-0.0.1-SNAPSHOT.jar
```

확인: http://localhost:8080/actuator/health 가 `UP`이면 정상입니다.

## 5. 웹 설치

**방법 A: 바로 실행 (Windows)**

1. `wangye/SOUL-Web/启动.bat`를 더블클릭합니다. (`dist-standalone` 폴더와 같은 위치에 있어야 함)
2. http://localhost:8000 으로 접속합니다. `index.html`을 직접 열면 안 됩니다.
3. 카메라/마이크 권한을 허용합니다.
4. 이 방식에는 `/api` 프록시가 없으므로 빌드된 파일의 API 주소를 확인해야 합니다. 불확실하면 방법 B 또는 C를 사용하세요.

**방법 B: 개발 서버 (소스)**

1. `wangye/SOUL-Web/vite.config.js`의 프록시 대상을 수정합니다. 기본값 `http://112.126.72.20:8080`은 원작자의 서버이므로, 로컬 배포 시 `http://localhost:8080`으로 바꿉니다.
2. 실행합니다.
   ```bash
   cd wangye/SOUL-Web
   npm install
   npm run dev
   ```
3. http://localhost:5090 으로 접속합니다.

**방법 C: Nginx 서버 배포**

```bash
npm install && npm run build
```

`dist/` 내용을 Nginx의 `/usr/share/nginx/html`에 복사하고 저장소의 `nginx.conf`를 적용합니다. `/api/`를 `127.0.0.1:8080`으로 전달하므로 같은 서버에 백엔드가 있어야 합니다. `soul-web-dist.tar.gz`는 미리 빌드된 배포본입니다.

> 주의: 카메라/마이크(Agora)는 `localhost` 또는 HTTPS에서만 동작합니다. 외부 공개 시 HTTPS를 설정하세요.

## 6. Android 앱

- **APK 설치**: `app/SOUL-优化1.1.apk`를 휴대폰으로 옮겨 설치합니다. "출처를 알 수 없는 앱 설치"를 허용해야 합니다.
- **서버 주소**: 앱을 다시 빌드할 필요 없이, 로그인 화면의 **서버 설정**에서 백엔드 주소를 입력하면 됩니다. (예: `http://192.168.0.10:8080`) `http://`와 끝의 `/`는 자동으로 보정됩니다. 에뮬레이터에서는 `http://10.0.2.2:8080`을 사용하세요.
- 휴대폰에서 PC 백엔드에 접속하려면 같은 네트워크여야 하며, `localhost` 대신 PC의 IP를 사용하고 Windows 방화벽에서 8080 포트를 허용해야 합니다.

## 7. 실행 순서와 자주 발생하는 문제

실행 순서: MariaDB → 백엔드(`/actuator/health` 확인) → 웹 → 모바일 앱

| 문제 | 원인 / 해결 |
|---|---|
| `Access denied` | DB 계정 또는 비밀번호가 `application.properties`와 다름 |
| 8080 포트 사용 중 | `server.port`를 변경하고 웹 프록시/nginx 설정도 함께 변경 |
| 웹에서 API 오류 | `vite.config.js` 프록시가 원작자 서버를 가리키는지 확인 |
| AI 기능(번역/퀴즈 등) 실패 | `deepseek.api.key`가 비어 있는지 확인 |
| 통화 화면이 안 나옴 | Agora 키가 비어 있거나, HTTPS가 아니거나, 권한이 거부됨 |

## 8. 보안 안내

`application.properties`에는 DB 비밀번호와 키가 포함됩니다. 공개 저장소에 실제 값이 올라가 있는지 확인하고, 있다면 키를 재발급하고 비밀번호를 변경하세요.
