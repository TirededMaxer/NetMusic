# Development workflow

- Use this local checkout as the source of truth. Edit, reproduce failures, run relevant tests and build locally before committing and pushing to GitHub.
- Preserve the existing GitHub Actions workflows. They remain supplemental checks and release packaging.
- Target Minecraft 26.1.2 Fabric with Java 25. Do not add online search or account login without a new user request.
- Keep the 1024-block mono playback rules, one world music player, shared radio stream, redstone behavior and Chinese/English language coverage.
- `.tooling/` contains local Java and Gradle caches and must not be committed.
