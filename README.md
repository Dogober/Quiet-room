# QuietRoom

QuietRoom consists of an Android client built with Kotlin/Jetpack Compose and a FastAPI/PostgreSQL backend.

## Backend

From the `backend` directory:

```powershell
py -3.14 -m venv .venv
.\.venv\Scripts\Activate.ps1
pip install -r requirements-dev.txt
Copy-Item .env.example .env
pytest
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

In the `.env` file, configure your own `DATABASE_URL` and a random `SECRET_KEY` that is at least 32 characters long. The local `.env` file is excluded from version control and is not committed to Git.

API health checks:

```text
http://localhost:8000/docs
http://localhost:8000/health
```

Alternative way to start the backend and PostgreSQL:

```powershell
docker compose up --build
```

## Android

By default, the debug build connects to `192.168.0.100:8000`. You can override the address in your user-specific `gradle.properties` file:

```properties
QUIET_ROOM_API_URL=http://192.168.0.100:8000/
QUIET_ROOM_WS_URL=ws://192.168.0.100:8000/ws
```

When using the Android Emulator, you should typically use `10.0.2.2` instead of your computer's IP address.

For testing on a physical device, both the Android device and the computer must be connected to the same local network.

Verification commands:

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat test
.\gradlew.bat assembleDebug
```

Production configurations must use only secure protocols:

```text
https://
wss://
```
