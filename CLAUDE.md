# CLAUDE.md - Dimension Teleport

## Projekt-Übersicht

**Dimension Teleport** ist ein NeoForge Minecraft Mod.
- **Mod ID**: `dimensionteleport`
- **Package**: `de.geheimagentnr1.dimensionteleport`
- **Java Version**: 21

Fügt den Befehl `/tpd <targets> (<location> [<dimension>] | <destination>)` hinzu (Permission-Level 2): Teleport wie `/tp`, aber auch in andere Dimensionen.

| Branch | MC | Range | NeoForge (kompiliert gegen) | Grund für den Schnitt |
|---|---|---|---|---|
| `develop_1.21.1` | 1.21.1 | `[1.21.1,1.21.2)` | 21.1.x | |
| `develop_1.21.2` | 1.21.2 - 1.21.10 | `[1.21.2,1.21.11)` | `21.2.1-beta` | `ServerPlayer.teleportTo(..)`-Überladungen und `RelativeMovement` entfernt: Teleport wie Vanilla-`/tp` seit 1.21.2 über `entity.teleportTo( level, x, y, z, Set.of(), yaw, pitch, true )` (Spieler und andere Entities, gleiche und andere Dimension); `getCommandSenderWorld()` (in 1.21.6 entfernt) → `level()`. Bytecode identisch für 1.21.2 - 1.21.10 |
| `develop_1.21.11` | 1.21.11 | `[1.21.11,1.21.12)` | `21.11.45` | Aufbauend auf `develop_1.21.2`: `source.hasPermission( 2 )` (in 1.21.11 entfernt) → `Commands.hasPermission( Commands.LEVEL_GAMEMASTERS )` |


## Abhängigkeiten

Keine Mod-Abhängigkeiten - eigenständiger Mod.

## Projektstruktur

```
src/main/java/de/geheimagentnr1/dimensionteleport/
├── DimensionTeleport.java                                     # Haupt-Mod-Klasse
└── elements/
    └── commands/
    │   └── dimension_teleport/
    │       ├── DimensionTeleportCommand.java                  # /tpd Command-Implementierung
    │       └── TargetListener.java                            # Callback Interface für Ziel-Dimension
```

## Besonderheiten

- **Server-fokussiert**: DisplayTest ist `IGNORE_SERVER_VERSION`
- **Teleport-Einschränkung**: Cross-Dimension-Teleport (`player.teleportTo(ServerLevel,...)`) entfernt in MC 1.21.2 → Range begrenzt auf `[1.21.1,1.21.2)`

## Code-Stil

- **Annotations**: `@NotNull` aus `org.jetbrains.annotations`
- **Lombok**: Projekt nutzt Lombok
- **Formatierung**: Leerzeichen nach `(` und vor `)` bei Methodenaufrufen

## Build & Test

```bash
./gradlew build
./gradlew runClient
./gradlew runServer
```

## Deployment

- **CurseForge**: `./gradlew curseforge`
- **Modrinth**: `./gradlew modrinth`

## Testing

### Java-Versionen

Verschiedene Java-Versionen sind unter `C:\Program Files\Eclipse Adoptium` installiert. Für einen Gradle-Build muss die passende Java-Version gewählt werden:

```powershell
# Java 21 für MC 1.20.5+ (NeoForge)
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.12.8-hotspot"
./gradlew build
```

### Unit Tests (JUnit 5)

Für reine Logik-Tests ohne Minecraft-Abhängigkeiten:

```bash
./gradlew test
```

Tests liegen unter `src/test/java/`. Ergebnisse: `build/reports/tests/test/index.html`

### NeoForge GameTest Framework

Ab `develop_1.21.2` gibt es keine GameTests mehr (Annotations-Framework ab 1.21.5 entfernt, trivialer Smoke-Test samt Run-Config und CI-Job gelöscht).

### Ingame-Test

`/tpd @s ~ ~ ~ minecraft:the_nether`, zurück per `/tpd @s <x> <y> <z> minecraft:overworld`, eine Entity (z. B. Kuh) in eine andere Dimension, mehrere Ziele (`@e[type=cow]`), auf eine andere Entity (`/tpd @s <Spieler>`), ungültige Position (außerhalb der Welt) gibt die Vanilla-Fehlermeldung. Die Rotation bleibt erhalten, gleitende Spieler (Elytra) behalten ihren Schwung.

### CI/CD (GitHub Actions)

Der Workflow `.github/workflows/build-and-test.yml` führt automatisch aus:
1. **Build**: Kompiliert den Mod
2. **Unit Tests**: Führt JUnit Tests aus

### Was kann automatisiert getestet werden?

| Aspekt | Automatisiert? | Methode |
|--------|----------------|---------|
| Utility-Klassen | ✅ | JUnit |
| Config-Parsing | ✅ | JUnit |
| Commands | ✅ | GameTest |
| Block/Item-Verhalten | ✅ | GameTest |
| Multi-MC-Version | ⚠️ Pro Branch | CI Matrix |

## Referenzen

- [NeoForge Migration Primer](https://docs.neoforged.net/primer/docs/) — Dokumentiert API-Aenderungen zwischen Minecraft/NeoForge-Versionen; nuetzlich fuer die Pruefung von Breaking Changes beim Upgrade auf neue Versionen

---

## Wissensdatenbank

Versionsübergreifende Migrations- und Entwicklungs-Erkenntnisse (Breaking Changes, Fixes, Testumgebungs-Patterns) werden zentral in [`../Docs/`](../Docs/) gepflegt. Bei neuen relevanten Erkenntnissen dort ergänzen, nicht nur hier.
