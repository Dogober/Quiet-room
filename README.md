# QuietRoom

QuietRoom состоит из Android-клиента на Kotlin/Jetpack Compose и API на
FastAPI/PostgreSQL.

## Backend

Из папки `backend`:

```powershell
py -3.14 -m venv .venv
.\.venv\Scripts\Activate.ps1
pip install -r requirements-dev.txt
Copy-Item .env.example .env
pytest
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

В `.env` нужно задать собственные `DATABASE_URL` и случайный `SECRET_KEY`
длиной не менее 32 символов. Локальный `.env` не попадает в git.

Проверка API:

```text
http://localhost:8000/docs
http://localhost:8000/health
```

Альтернативный запуск backend и PostgreSQL:

```powershell
docker compose up --build
```

## Android

По умолчанию debug-сборка обращается к `192.168.0.100:8000`. Адрес можно
переопределить в пользовательском `gradle.properties`:

```properties
QUIET_ROOM_API_URL=http://192.168.0.100:8000/
QUIET_ROOM_WS_URL=ws://192.168.0.100:8000/ws
```

Для Android Emulator вместо IP компьютера обычно используется `10.0.2.2`.
Физический телефон и компьютер должны находиться в одной сети.

Проверки:

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat test
.\gradlew.bat assembleDebug
```

Production-конфигурация должна использовать только `https://` и `wss://`.

