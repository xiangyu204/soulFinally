GitHub 링크만 가지고 있어도 따라 할 수 있도록 순서대로 정리했습니다. **번호 순서대로, 건너뛰지 말고** 진행하세요.

## 먼저 정하세요: 어떤 방식으로 쓸 건가요?

| 방식 | 누가 | 해야 할 일 |
|---|---|---|
| **A. 서버 주소를 받아서 사용 (가장 쉬움)** | 대부분의 학생 | 3·4장만 하면 됩니다. 백엔드 서버는 담당자(교수/조장)가 한 대의 PC에서 실행하고 주소를 알려 줍니다. |
| **B. 내 PC에 서버까지 전부 설치** | 서버 담당자, 혼자 테스트하는 학생 | 1~5장을 모두 진행합니다. |

> 서버 주소 예시: `http://192.168.0.10:8080` (담당자 PC의 IP). 같은 Wi-Fi/네트워크에 있어야 접속됩니다.

---

## 1장. 준비물 설치 (방식 B만)

아래 순서로 설치합니다. 설치 후에는 **새 PowerShell 창**을 열어야 적용됩니다.

1. **Git**: https://git-scm.com/download/win 에서 설치 (계속 Next)
2. **JDK 17**: https://adoptium.net 에서 "Temurin 17 (LTS)" 설치. 설치 중 **"Add to PATH"** 옵션을 반드시 켭니다.
3. **MariaDB**: https://mariadb.org/download 에서 설치. 설치 중 **root 비밀번호를 정하고 꼭 메모**하세요. 포트는 3306 그대로 둡니다.
4. **Node.js (LTS)**: https://nodejs.org 에서 설치 (웹을 소스로 실행할 때 필요)

설치 확인 (PowerShell에 한 줄씩 입력):

```powershell
git --version
java -version
node -v
```

각각 버전이 출력되면 성공입니다. `java -version`은 `17`로 시작해야 합니다. "명령을 찾을 수 없음"이 나오면 PowerShell을 껐다 켜거나 설치 때 PATH 옵션을 놓친 것입니다.

---

## 2장. 코드 내려받기 (방식 B만)

1. 바탕화면에서 PowerShell을 엽니다.
2. 입력합니다.
   ```powershell
   cd $HOME\Desktop
   git clone https://github.com/xiangyu204/soulFinally.git
   cd soulFinally
   ```
3. 바탕화면에 `soulFinally` 폴더가 생기면 성공입니다.

> Git이 어렵다면 GitHub 저장소 페이지에서 **Code → Download ZIP** → 압축 해제도 됩니다.

---

## 3장. 서버(백엔드) 실행 (방식 B만)

### 3-1. 데이터베이스 만들기

1. 시작 메뉴에서 **"HeidiSQL"** 또는 **"MariaDB Command Prompt"**를 실행합니다.
2. root 비밀번호로 로그인한 뒤 입력합니다.
   ```sql
   CREATE DATABASE soul DEFAULT CHARACTER SET utf8mb4;
   ```
3. PowerShell로 돌아와 데이터를 넣습니다. (`mysql` 명령이 없으면 MariaDB 설치 폴더의 `bin`에서 실행)
   ```powershell
   cd $HOME\Desktop\soulFinally
   mysql -u root -p soul < db\soul-20261105.sql
   ```
   > PowerShell은 `<` 방향 기호를 지원하지 않습니다. 오류가 나면 **명령 프롬프트(cmd)**에서 같은 명령을 실행하세요.

### 3-2. 서버 켜기

```powershell
cd $HOME\Desktop\soulFinally\backend\soul-backend
java -jar soul-backend.jar --spring.datasource.password=<root비밀번호> --deepseek.api.key=<DeepSeek키>
```

- `<...>`는 **꺾쇠까지 지우고** 실제 값으로 바꿉니다.
- 처음에는 `Started ...Application` 같은 문구가 나올 때까지 기다립니다.
- DB 사용자 이름이 root가 아니라면 `--spring.datasource.username=사용자명`도 추가합니다.
- **이 창은 닫지 마세요.** 닫으면 서버가 꺼집니다.

### 3-3. 켜졌는지 확인

브라우저에서 http://localhost:8080/actuator/health 를 엽니다. `"status":"UP"`이 보이면 성공입니다.

---

## 4장. 웹 사용하기

### 4-1. 서버 주소 맞추기 (한 번만)

1. `soulFinally\wangye\SOUL-Web\vite.config.js`를 메모장이나 VS Code로 엽니다.
2. 아래 줄을 찾습니다.
   ```js
   target: 'http://112.126.72.20:8080',
   ```
3. 주소를 바꿉니다.
   - 방식 B (내 PC 서버): `http://localhost:8080`
   - 방식 A (담당자 서버): 담당자가 알려 준 주소 (예: `http://192.168.0.10:8080`)
4. 저장합니다.

### 4-2. 웹 실행

```powershell
cd $HOME\Desktop\soulFinally\wangye\SOUL-Web
npm install
npm run dev
```

- `npm install`은 처음에 몇 분 걸릴 수 있습니다.
- `Local: http://localhost:5090` 같은 문구가 보이면 브라우저에서 **http://localhost:5090** 으로 접속합니다.
- 이 창도 닫지 마세요.

### 4-3. 사용하기

1. 회원가입 후 로그인합니다. (이미 DB 데이터를 넣었다면 기존 계정이 있을 수 있습니다.)
2. 통화 기능을 쓸 때 브라우저가 카메라/마이크 권한을 물으면 **허용**합니다.
3. 통화 기능은 `localhost` 주소에서만 동작합니다. 다른 PC의 IP 주소(`http://192.168...`)로 열면 카메라가 막힐 수 있습니다.

---

## 5장. Android 앱 설치와 사용

앱은 **APK 파일 설치만 하면 되고, Android Studio는 필요 없습니다.**

### 5-1. 실제 휴대폰에 설치

1. `soulFinally\app\SOUL-优化1.1.apk`를 휴대폰으로 보냅니다. (USB 케이블, 카카오톡 "나와의 채팅", 구글 드라이브 등)
2. 휴대폰에서 파일을 누르면 "출처를 알 수 없는 앱 설치" 허용 화면이 나옵니다. **허용**합니다.
3. 설치를 누르고 앱을 엽니다.
4. 보안 경고(Play 프로텍트)가 나오면 "무시하고 설치"를 선택합니다. 직접 만든 앱이라 나오는 경고입니다.

### 5-2. 서버 주소 입력 (가장 중요)

1. 앱의 **로그인 화면**에서 **서버 설정**을 누릅니다.
2. 서버 주소를 입력합니다.
   - 방식 A: 담당자가 알려 준 주소 (예: `http://192.168.0.10:8080`)
   - 방식 B에서 내 PC 서버를 쓸 때: **내 PC의 IP** (아래 참고). 휴대폰에서 `localhost`는 휴대폰 자신을 가리킵니다.
3. 저장 후 로그인합니다.

내 PC의 IP 확인:

```powershell
ipconfig
```

"IPv4 주소" 값(예: `192.168.0.10`)을 사용합니다. 휴대폰과 PC는 **같은 Wi-Fi**여야 합니다.

### 5-3. 방화벽 열기 (방식 B, 서버 PC에서 한 번)

관리자 PowerShell에서:

```powershell
New-NetFirewallRule -DisplayName "SOUL 8080" -Direction Inbound -Protocol TCP -LocalPort 8080 -Action Allow
```

### 5-4. 에뮬레이터를 쓰는 경우

Android Studio 에뮬레이터에서는 서버 주소를 `http://10.0.2.2:8080`으로 입력합니다. (PC의 localhost를 뜻하는 특수 주소)

---

## 6장. 끝났는지 점검표

- [ ] http://localhost:8080/actuator/health 가 `UP` (방식 B)
- [ ] 웹 http://localhost:5090 에서 로그인 됨
- [ ] 앱에서 서버 설정 입력 후 로그인 됨
- [ ] 웹과 앱에서 서로 메시지/매칭이 보임

## 7장. 막혔을 때

| 증상 | 확인할 것 |
|---|---|
| `java`를 찾을 수 없음 | JDK 설치 시 PATH 옵션, PowerShell 재시작 |
| `Access denied for user` | 3-2의 비밀번호가 MariaDB 설치 때 정한 값과 같은지 |
| `Port 8080 already in use` | 이전에 켠 서버 창이 남아 있지 않은지 (닫거나 Ctrl+C) |
| 웹은 열리는데 데이터가 안 나옴 | 4-1의 주소가 맞는지, 수정 후 `npm run dev`를 다시 켰는지 |
| 앱에서 "연결 실패" | 같은 Wi-Fi인지, 주소에 `localhost`를 쓰지 않았는지, 5-3 방화벽 |
| AI 번역/퀴즈가 안 됨 | 서버 실행 때 `--deepseek.api.key`를 넣었는지 |
| 통화 화면이 안 보임 | 브라우저 카메라/마이크 권한, `localhost`로 접속했는지 |

## 8장. 끝낼 때

서버·웹 창에서 **Ctrl+C**를 누르면 종료됩니다. 다음에 다시 쓸 때는 3-2 → 4-2만 반복하면 됩니다.
