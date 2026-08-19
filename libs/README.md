# Local jars (optional)

Build uses **Modrinth Maven** for Touhou Little Maid by default (`build.gradle` → `maven.modrinth:touhou-little-maid`).

This folder is only an **offline fallback**. Current file:

- `touhoulittlemaid-1.5.3.jar`

To use it offline, temporarily switch the dependency in `build.gradle` to the `flatDir` / `blank:` style noted in comments there.

**Do not** treat root-directory copies of the same jar as the real dependency — those were clutter; a spare may live under `docs/归档/…` as backup. Prefer keeping the usable offline copy **here in `libs/`**.

Do not commit huge jar files if the team relies on Maven only.
