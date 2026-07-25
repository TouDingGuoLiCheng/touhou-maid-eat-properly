# Local jars (optional)

Build uses Modrinth Maven for Touhou Little Maid by default.

If you need an offline fallback, place:

`touhoulittlemaid-1.5.3.jar`

in this folder and temporarily switch `build.gradle` back to:

```gradle
implementation fg.deobf("blank:touhoulittlemaid:1.5.3")
```

Do not commit jar files.
