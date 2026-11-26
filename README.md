
GitHub 다운로드부터 설치·실행까지의 전체 절차입니다. 처음 시작하는 학생도 순서대로 따라 할 수 있게 정리했습니다.

> **API 키 등 민감한 설정 값은 이 문서에 적지 않습니다.** 필요한 설정 파일은 프로젝트 담당자에게 따로 받으세요. 받은 파일이나 비밀번호는 GitHub에 올리지 마세요.

## 구성

| 폴더 | 내용 |
|---|---|
| `db/` | `soul-schema-20261105.sql`(테이블 구조만), `soul-20261105.sql`(데이터 포함) |
| `backend/soul-backend` | Spring Boot 3.2.5 / Java 17 / MariaDB 백엔드 (포트 8080) |
| `wangye/SOUL-Web` | React + Vite 웹 |
| `app/` | `SOUL-优化1.1.apk` 및 `soul-android` 소스 |

## 먼저 정하세요: 어떤 방식으로 쓸 건가요?

| 방식 | 누가 | 해야 할 일 |
|---|---|---|
| **A. 서버 주소를 받아서 사용 (가장 쉬움)** | 대부분의 학생 | 4·5장만 진행. 서버는 담당자가 한 PC에서 실행하고 주소를 알려 줍니다. |
| **B. 내 PC에 서버까지 전부 설치** | 서버 담당자, 혼자 테스트하는 학생 | 1~5장을 모두 진행 |

> 서버 주소 예시: `http://192.168.0.10:8080` (담당자 PC의 IP). 같은 Wi-Fi/네트워크여야 접속됩니다.

---

## 1장. 준비물 설치 (방식 B만)

설치 후에는 **새 PowerShell 창**을 열어야 적용됩니다.

1. **Git**: https://git-scm.com/download/win
2. **JDK 17**: https://adoptium.net 의 "Temurin 17 (LTS)". 설치 중 **"Add to PATH"** 옵션을 켭니다.
3. **MariaDB**: https://mariadb.org/download. **root 비밀번호를 정하고 꼭 메모**하세요. 포트는 3306 그대로 둡니다.
4. **Node.js (LTS)**: https://nodejs.org

확인:

```powershell
git --version
java -version
node -v
```

각각 버전이 나오면 성공입니다. `java -version`은 `17`로 시작해야 합니다.

## 2장. 코드 내려받기 (방식 B만)

```powershell
cd $HOME\Desktop
git clone https://github.com/xiangyu204/soulFinally.git
cd soulFinally
```

Git이 어렵다면 GitHub 페이지에서 **Code → Download ZIP** 후 압축을 풀어도 됩니다.

## 3장. 서버(백엔드) 실행 (방식 B만)

### 3-1. 데이터베이스 만들기

1. **HeidiSQL** 또는 **MariaDB Command Prompt**를 실행하고 root로 로그인합니다.
2. 입력합니다.
   ```sql
   CREATE DATABASE soul DEFAULT CHARACTER SET utf8mb4;
   ```
3. 데이터를 넣습니다. (PowerShell은 `<` 기호가 안 되므로 **명령 프롬프트(cmd)**에서 실행)
   ```bat
   cd %USERPROFILE%\Desktop\soulFinally
   mysql -u root -p soul < db\soul-20261105.sql
   ```

> `spring.jpa.hibernate.ddl-auto=update` 설정이라, 백엔드를 처음 실행할 때 테이블이 자동 생성되기도 합니다.

### 3-2. 설정 파일 준비

담당자에게 받은 **`application.properties`**를 `backend/soul-backend/soul-backend.jar`와 **같은 폴더**에 넣습니다. jar와 같은 폴더의 설정 파일은 jar 내부 설정보다 우선 적용됩니다.

이 파일에서 본인 환경에 맞게 바꿀 값:

| 항목 | 설명 |
|---|---|
| DB 접속 정보 | 기본은 로컬 MariaDB의 `soul` 데이터베이스. 사용자 이름/비밀번호를 본인 것으로 변경 |
| 업로드 폴더 | 실제로 존재하는 폴더 경로로 변경 |
| 서버 포트 | 기본 8080 |

> 외부 서비스(AI·음성/영상 통화) 설정은 담당자가 파일에 미리 넣어 줍니다. 직접 값을 찾거나 이 저장소에 올리지 마세요.

### 3-3. 서버 켜기

```powershell
cd $HOME\Desktop\soulFinally\backend\soul-backend
java -jar soul-backend.jar --spring.datasource.password=<DB비밀번호>
```

- `<...>`는 **꺾쇠까지 지우고** 실제 값으로 바꿉니다.
- `Started ...Application` 같은 문구가 나올 때까지 기다립니다.
- **이 창은 닫지 마세요.** 닫으면 서버가 꺼집니다.

소스에서 직접 빌드하려면 (Maven 필요):

```powershell
cd backend\soul-backend
mvn clean package -DskipTests
java -jar target\soul-backend-0.0.1-SNAPSHOT.jar
```

### 3-4. 켜졌는지 확인

브라우저에서 http://localhost:8080/actuator/health 를 엽니다. `"status":"UP"`이 보이면 성공입니다.

## 4장. 웹 사용하기

### 4-1. 서버 주소 맞추기 (한 번만)

1. `wangye\SOUL-Web\vite.config.js`를 메모장이나 VS Code로 엽니다.
2. `proxy` 설정 안의 `target:` 값을 사용할 서버 주소로 바꿉니다.
   - 방식 B (내 PC 서버): `http://localhost:8080`
   - 방식 A (담당자 서버): 담당자가 알려 준 주소
3. 저장합니다.

### 4-2. 웹 실행

```powershell
cd $HOME\Desktop\soulFinally\wangye\SOUL-Web
npm install
npm run dev
```

- `npm install`은 처음에 몇 분 걸립니다.
- 브라우저에서 **http://localhost:5090** 으로 접속합니다. 이 창도 닫지 마세요.

### 4-3. 사용하기

1. 회원가입 후 로그인합니다.
2. 통화 기능은 브라우저가 카메라/마이크 권한을 물으면 **허용**합니다.
3. 카메라/마이크는 `localhost` 또는 HTTPS 주소에서만 동작합니다.

### (선택) 빌드된 웹을 바로 실행

`wangye/SOUL-Web/启动.bat`를 `dist-standalone` 폴더와 같은 위치에서 더블클릭하면 http://localhost:8000 이 열립니다. 이 방식은 서버 주소를 바꿀 수 없으니, 문제가 있으면 위의 4-2 방식을 사용하세요.

### (선택) 서버에 배포 (Nginx)

```bash
npm install && npm run build
```

`dist/` 내용을 Nginx의 웹 루트에 복사하고, 저장소의 `nginx.conf`를 적용합니다. 이 설정은 API 요청을 같은 서버의 백엔드로 전달합니다. 미리 빌드된 `soul-web-dist.tar.gz`도 있습니다. 외부 공개 시 HTTPS를 설정하세요.

## 5장. Android 앱 설치와 사용

앱은 **APK 설치만 하면 되고, Android Studio는 필요 없습니다.**

### 5-1. 휴대폰에 설치

1. `app/SOUL-优化1.1.apk`를 휴대폰으로 보냅니다. (USB, 메신저 "나와의 채팅", 클라우드 드라이브 등)
2. 파일을 누르면 나오는 "출처를 알 수 없는 앱 설치"를 **허용**합니다.
3. 설치 후 앱을 엽니다. 보안 경고가 나오면 "무시하고 설치"를 선택합니다.

### 5-2. 서버 주소 입력 (가장 중요)

1. **로그인 화면**의 **서버 설정**을 누릅니다.
2. 서버 주소를 입력합니다.
   - 방식 A: 담당자가 알려 준 주소
   - 방식 B에서 내 PC 서버: **내 PC의 IP** (`localhost`는 휴대폰 자신을 가리키므로 쓰면 안 됨)
3. 저장 후 로그인합니다. `http://`와 끝의 `/`는 자동으로 보정됩니다.

내 PC의 IP 확인:

```powershell
ipconfig
```

"IPv4 주소"(예: `192.168.0.10`)를 사용합니다. 휴대폰과 PC는 **같은 Wi-Fi**여야 합니다.

### 5-3. 방화벽 열기 (방식 B, 서버 PC에서 한 번)

관리자 PowerShell에서:

```powershell
New-NetFirewallRule -DisplayName "SOUL 8080" -Direction Inbound -Protocol TCP -LocalPort 8080 -Action Allow
```

### 5-4. 에뮬레이터

Android Studio 에뮬레이터에서는 서버 주소를 `http://10.0.2.2:8080`으로 입력합니다.

## 6장. 점검표

- [ ] http://localhost:8080/actuator/health 가 `UP` (방식 B)
- [ ] 웹 http://localhost:5090 에서 로그인 됨
- [ ] 앱에서 서버 설정 입력 후 로그인 됨
- [ ] 웹과 앱에서 서로 메시지/매칭이 보임

## 7장. 막혔을 때

| 증상 | 확인할 것 |
|---|---|
| `java`를 찾을 수 없음 | JDK 설치 시 PATH 옵션, PowerShell 재시작 |
| `Access denied for user` | 실행 명령의 비밀번호가 MariaDB 설치 때 정한 값과 같은지 |
| `Port 8080 already in use` | 이전에 켠 서버 창이 남아 있는지 (닫거나 Ctrl+C) |
| 웹은 열리는데 데이터가 안 나옴 | 4-1의 주소가 맞는지, 수정 후 `npm run dev`를 다시 켰는지 |
| 앱에서 "연결 실패" | 같은 Wi-Fi인지, `localhost`를 쓰지 않았는지, 5-3 방화벽 |
| AI 기능이나 통화가 안 됨 | 3-2의 설정 파일을 담당자에게 받아 jar 옆에 두었는지 |
| 통화 화면이 안 보임 | 브라우저 카메라/마이크 권한, `localhost` 접속 여부 |

## 8장. 끝낼 때

서버·웹 창에서 **Ctrl+C**를 누르면 종료됩니다. 다음에 다시 쓸 때는 3-3 → 4-2만 반복하면 됩니다.

## 보안 안내

- 비밀번호, API 키, 설정 파일은 이 저장소에 올리지 않습니다.
- 담당자는 공개 저장소의 `application.properties` 등에 실제 값이 들어 있는지 확인하세요. 이미 올라간 값은 Git 기록에 남으므로 **키를 재발급하고 비밀번호를 변경**해야 합니다.
