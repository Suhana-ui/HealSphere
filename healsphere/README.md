# HealSphere – Virtual Reality Therapy Session

A college Java mini-project: a calm, guided-breathing website with four environments, a session timer, an
animated breathing circle, optional nature sounds, an optional 3D/WebXR view, and a progress dashboard
backed by a Java Spring Boot server and an H2 database.

> **Wellness disclaimer:** HealSphere offers general relaxation activities. It is not medical care and not a
> substitute for professional mental health support.

## Honest status of this project (read first)

This code was written without being able to download Maven dependencies, so **the Java code and the automated tests
have NOT been compiled or run by me**. The JavaScript files were syntax-checked with Node.js, nothing more.
Please run `mvn test` yourself (steps below) and tell me about any error message, and I will fix it.

Also not included:
- `mvnw` / `mvnw.cmd` (the Maven wrapper needs a download to generate). Use IntelliJ's built-in Maven, or create the wrapper once with `mvn -N wrapper:wrapper`.
- Photos and sound files. Put your own legally usable files in `src/main/resources/static/images/` and `.../static/audio/` (names below). Without them the site shows CSS gradient scenes and shows a friendly "audio not available" message.
- 3D/VR has not been tested on a real headset.

## Features
- Home page, environment cards, How It Works, benefits, footer disclaimer
- 4 environments (forest, beach, mountain, room); invalid/missing `?env=` falls back to forest on the server
- Session: 1/3/5 minute timer, breathing circle ("Breathe In/Out"), Start / Pause / Resume / Reset / End, progress bar
- Optional ambience with Play / Mute / Stop; stops on end, complete, reset, and leaving the page
- Completion screen, optional mood, saved only when you press **Save session**; retry is safe (idempotency key)
- Progress page from real database records, with loading, error, and empty states
- REST API: `GET /api/sessions`, `GET /api/sessions/{id}`, `POST /api/sessions`, `GET /api/progress`, `GET /api/environments`
- Optional 3D scene (Three.js from a CDN) with drag-to-look and an "Enter VR" button when WebXR is available

## Technology
Java 17+, Spring Boot 3.3.5, Maven, Spring MVC + Thymeleaf, Spring Data JPA, H2, Bean Validation, JUnit 5 / MockMvc,
HTML/CSS/vanilla JavaScript, Fetch API, Three.js (optional).

## Folder structure
```
healsphere/
  pom.xml
  src/main/java/com/healsphere/
    HealsphereApplication.java      starts the app
    controller/  PageController (HTML pages), ApiController (JSON)
    service/     EnvironmentService, SessionService (rules + stats)
    repository/  TherapySessionRepository (database access)
    model/       TherapySession (database table)
    dto/         request/response shapes
    config/      SecurityHeadersConfig
    exception/   error classes + GlobalExceptionHandler
  src/main/resources/
    templates/   index.html, session.html, progress.html, fragments/layout.html
    static/css/  style.css, session.css, progress.css
    static/js/   main.js, session.js, progress.js, scene.js
    static/images/  (add: hero.jpg, forest.jpg, beach.jpg, mountain.jpg, room.jpg)
    static/audio/   (add: forest.mp3, beach.mp3, mountain.mp3, room.mp3)
    application.properties, application-prod.properties
  src/test/java/com/healsphere/HealsphereApplicationTests.java
```

## Setup on Windows
1. Install **JDK 17 or newer** (e.g. Temurin 17/21). Check: `java -version`.
2. Install **IntelliJ IDEA Community** (easiest; it bundles Maven) or VS Code with the "Extension Pack for Java".
3. Open the `healsphere` folder (IntelliJ: *File > Open*, pick `pom.xml`, "Open as Project"). Wait for dependencies to download (internet needed).
4. Run: open `HealsphereApplication.java` and click the green Run arrow.

### Command line
If you have Maven installed (`mvn -v` works):
```
mvn -N wrapper:wrapper        (one time; creates mvnw and mvnw.cmd)
mvnw.cmd spring-boot:run
mvnw.cmd test
```
Without the wrapper use `mvn spring-boot:run` and `mvn test`.

Open **http://localhost:8080/** in your browser.

## Pages and database
- Home: `/`  Session: `/session?env=forest`  Progress: `/progress`
- H2 console (local only): **http://localhost:8080/h2-console**
  JDBC URL `jdbc:h2:file:./data/healsphere`, user `sa`, empty password. Try `SELECT * FROM therapy_session;`
- For deployment run with `--spring.profiles.active=prod`, which turns the console off.
- Records are local development records and are **not separated per user** (no login in this version).

## Troubleshooting
- *Port 8080 already in use*: add `server.port=8081` in `application.properties`.
- *"release version 17 not supported"*: your JDK is older than 17; install JDK 17+ and set it in IntelliJ (*File > Project Structure > SDK*).
- *Dependencies fail to download*: check internet/proxy.
- *Database file locked*: stop the other running copy of the app, or delete the `data` folder to start fresh.
- *3D view says it could not load*: it needs internet to fetch Three.js. The rest of the app works without it.
- *No sound*: add the audio files, then press "Play sound" (browsers block autoplay, so sound only starts after a click).

## How it works (simple version)
- **Java & Spring Boot**: `HealsphereApplication` starts an embedded web server. Spring finds your controllers, services and repositories automatically.
- **Controllers**: `PageController` answers page URLs by returning a template name; `ApiController` answers `/api/...` with JSON.
- **Thymeleaf** fills the HTML templates on the server (e.g. the selected environment name) before sending them.
- **HTML/CSS/JS**: HTML is structure, CSS is looks, JavaScript adds behaviour (timer, buttons).
- **REST + fetch**: JavaScript calls `fetch('/api/sessions')` to send/receive JSON; the controller hands work to the service.
- **JPA + H2**: `TherapySession` is a Java class that becomes a table; the repository saves and reads rows without hand-written SQL.
- **Timer & breathing**: one `requestAnimationFrame` loop measures active time (paused time doesn't count); remaining time = total - active time. The circle size follows a smooth cosine curve over an 8-second cycle (4s in, 4s out). The loop is cancelled before every restart, so timers never stack.
- **Progress**: the service counts rows where `completed = true` and sums their elapsed seconds.
- **Duplicate protection**: each session gets a random key; the database has a unique constraint on it, so retrying never creates two rows.
- **Cheating protection**: the server checks environment, duration (60/180/300), mood, start time, and that "completed" matches the full duration and real elapsed time.

## Manual browser tests
1. Home: every nav link/button works; resize to phone width and use the menu.
2. Pick each environment; the session page title and colours match; try `/session?env=xyz` (falls back to forest).
3. Press Start, Pause (timer and circle freeze), Resume (continues), Reset, End session (no record saved).
4. Choose 1 minute, let it finish, pick a mood, Save; open Progress and confirm the row and totals.
5. Stop the server, press Save on a completed session (error message), restart server, press Save again (one record only).
6. Play sound, Mute, Stop; navigate away and confirm sound stops. Rename audio files to test the fallback message.
7. Open the 3D view, drag around; use "Enter VR" only on a WebXR device over HTTPS or localhost.

## Demo script for evaluators
Home > choose Mountain Retreat > 1 minute > Start > Pause/Resume > finish > Save > Progress > show H2 console > show `mvn test`.

## VR requirements
WebXR browser (Chrome/Edge/Meta Quest Browser), a compatible headset, `https://` or `http://localhost`, and permission when prompted. Without WebXR the 3D view still works by mouse/touch.
