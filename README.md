# 🌿 HealSphere – Virtual Reality Therapy Session

### Team: TechCore

HealSphere is a browser-based virtual reality therapy and mental wellness platform designed to provide users with guided relaxation and immersive calming experiences.

The project combines **Java, Spring Boot, HTML, CSS, JavaScript, Three.js and MySQL** to create an interactive wellness platform.

> ⚠️ **Disclaimer:** HealSphere is an educational prototype developed for a college project. It is not a medical diagnosis or treatment system and should not replace professional medical or mental-health care.

---

## ✨ Features

- 🧠 **AI Wellness Companion** – Provides conversational wellness support.
- 📝 **Daily Check-in** – Allows users to record their daily emotional state.
- 🧘 **Guided Meditation** – Provides structured relaxation activities.
- 🌿 **VR Calm Environments** – Explore calming virtual environments.
- 🌲 **Virtual Forest** – Relaxing nature-based environment.
- 🌊 **Peaceful Beach** – Ocean-inspired calming environment.
- 🌌 **Cosmic Night** – Immersive night-sky environment.
- 🧘‍♀️ **Zen Garden** – Minimal and peaceful meditation environment.
- 🫁 **Guided Breathing** – Uses a 4–4–6 breathing cycle.
- ⏱️ **Session Timer** – Tracks therapy session duration.
- 📊 **Progress Tracking** – Displays wellness/session information.
- 👨‍⚕️ **Therapist Support** – Provides an interface for professional-care concepts.

---

## 🎯 Project Objectives

The main objectives of HealSphere are:

1. To create an immersive browser-based relaxation experience.
2. To provide guided breathing and meditation activities.
3. To demonstrate Java and Spring Boot backend development.
4. To integrate a 3D environment using Three.js.
5. To store user and therapy-session information using MySQL.
6. To create a simple and user-friendly wellness interface.
7. To provide a foundation for future VR and wellness technologies.

---

## 🛠️ Technologies Used

### Backend

- Java
- Spring Boot
- Spring MVC
- Spring Data JPA
- Hibernate

### Frontend

- HTML5
- CSS3
- JavaScript
- Thymeleaf

### 3D / VR

- Three.js

### Database

- MySQL

### Development Tools

- IntelliJ IDEA
- Maven
- GitHub

---

## 🏗️ System Architecture

```text
                    USER
                      │
                      ▼
              HTML / CSS / JS
                      │
                      ▼
                 Thymeleaf
                      │
                      ▼
               Spring Boot
                      │
          ┌───────────┼───────────┐
          ▼           ▼           ▼
     Controller     Service      Model
                      │
                      ▼
              Spring Data JPA
                      │
                      ▼
                   MySQL
