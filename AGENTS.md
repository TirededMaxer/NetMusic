# Development workflow

- Use this local checkout as the source of truth for source edits and Git operations. Run compilation, tests and release packaging on GitHub Actions; do not build locally or download local JDK/dependency caches.
- Preserve and maintain the existing GitHub Actions workflows for all supported targets.
- Target the released Minecraft versions listed in versions.json (26.1.2, 26.2 and 26.3), Fabric only, with Java 25. Verify every target on GitHub Actions before publishing. Do not add online search or account login without a new user request.
- Keep the 1024-block mono playback rules, one world music player, shared radio stream, redstone behavior and Chinese/English language coverage.
- Keep the checkout limited to source, required build inputs/configuration, and Git metadata. Do not retain `.tooling/`, `.gradle/`, `build/`, runtime directories or downloaded release artifacts locally.
