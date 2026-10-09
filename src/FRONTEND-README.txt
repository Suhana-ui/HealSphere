HealSphere FRONTEND files (Thymeleaf templates, CSS, JavaScript, images/audio folders).

Spring Boot serves these from src/main/resources, so they must sit inside the backend project.
Extract this zip into the SAME parent folder as the backend zip and let folders merge:

  healsphere/src/main/resources/templates/   <- HTML pages
  healsphere/src/main/resources/static/      <- css, js, images, audio

The JavaScript calls these backend endpoints: /api/sessions, /api/progress, /api/environments.
Add your own images (hero.jpg, forest.jpg, beach.jpg, mountain.jpg, room.jpg) to static/images
and audio (forest.mp3, beach.mp3, mountain.mp3, room.mp3) to static/audio.
