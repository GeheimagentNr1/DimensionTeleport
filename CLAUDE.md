# CLAUDE.md - Dimension Teleport

Dimension Teleport fügt den Befehl `/tpd` hinzu: Teleport wie Vanilla-`/tp`, aber zusätzlich in andere Dimensionen (Spieler und beliebige Entities). Mod-ID `dimensionteleport`, Package `de.geheimagentnr1.dimensionteleport`. Läuft rein serverseitig: kein `@Mod( dist = .. )`, aber `displayTest=IGNORE_SERVER_VERSION` (gradle.properties), Clients brauchen den Mod nicht.

Diese Datei ist auf allen `develop_*`-Branches identisch; Quelle: `../Docs/mod-claude/DimensionTeleport.md` (Änderungen dort, dann `Docs/tools/sync_mod_claude_md.sh`). Versionsstand: [`../Docs/VERSIONEN.md`](../Docs/VERSIONEN.md).

## Versionen

- Forge: `develop_1.14.4` .. `develop_1.21`, `develop_1.21.1_forge` (letzter Forge-Stand 1.21.1).
- NeoForge: vier Jars - `develop_1.21.1` (nur 1.21.1), `develop_1.21.2` (1.21.2 - 1.21.10, Bytecode identisch), `develop_1.21.11`, `develop_26.1` (ein Jar 26.1 - 26.3). Details: [`../Docs/migrations/1.21.1-to-1.21.2.md`](../Docs/migrations/1.21.1-to-1.21.2.md) Abschnitt 4e.
- Ranges, Tags und Java-Versionen pro Branch: [`../Docs/VERSIONEN.md`](../Docs/VERSIONEN.md).

## Abhängigkeiten

- Keine Mod-Abhängigkeiten. Forge 1.20 - 1.21.1-Forge: MinecraftForgeAPI per `jarJar` gebündelt (`AbstractMod`, `ModCommandsRegisterFactory`), siehe [`../Docs/forge/README.md`](../Docs/forge/README.md).

## Architektur

- `DimensionTeleport` - Mod-Klasse, registriert den Befehl in `RegisterCommandsEvent` (Forge 1.20+: über `AbstractMod` und `ModCommandsRegisterFactory`; Forge bis 1.19.4: `handlers/ForgeEventHandler`).
- `elements/commands/dimension_teleport/DimensionTeleportCommand` - Brigadier-Baum und Teleport-Logik; feuert vorher `EventHooks.onEntityTeleportCommand(..)` (andere Mods können das Ziel ändern oder abbrechen) und prüft `Level.isInSpawnableBounds`.
- `TargetListener` - Callback, das bei den Positions-Varianten pro Ziel-Entity die Ziel-Dimension liefert (eigene oder angegebene Dimension); bei `<destination>` gilt die Dimension der Ziel-Entity.
- Teleport-Implementierung je Version:
  - bis 1.21.1: eigene Logik nach dem alten Vanilla-`/tp` (`ServerPlayer.teleportTo(ServerLevel, ..)`, für andere Entities neu erzeugen und in der Ziel-Dimension einfügen);
  - ab 1.21.2: wie Vanilla-`/tp` über `entity.teleportTo( level, x, y, z, Set.of(), yaw, pitch, true )` für Spieler und Entities, gleiche und andere Dimension;
  - ab 26.1 bekommt `onEntityTeleportCommand(..)` zusätzlich die Ziel-Dimension.

## Funktionen, Befehle, Konfiguration

- `/tpd <targets> <location> [<dimension>]` und `/tpd <targets> <destination>` - Rechtestufe 2 (`hasPermission( 2 )` bis 1.21.10, ab 1.21.11 `Commands.LEVEL_GAMEMASTERS`).
- Keine Config, keine gespeicherten Daten.

## Fallstricke

- **1.21.2**: `ServerPlayer.teleportTo(..)`-Überladungen und `RelativeMovement` entfernt, `TeleportTransition` gibt es in 1.21.1 nicht - kein Cross-fix zu 1.21.1. `getCommandSenderWorld()` (1.21.6 entfernt) → `level()`, deshalb ein Jar 1.21.2 - 1.21.10.
- `TicketType.POST_TELEPORT` gibt es nicht mehr - der frühere `addRegionTicket`-Aufruf ist entfallen, `teleportTo()` lädt die Chunks selbst.
- **1.21.11**: `hasPermission(int)` entfernt ([`../Docs/migrations/1.21.10-to-1.21.11.md`](../Docs/migrations/1.21.10-to-1.21.11.md)). Alle Brüche: [`../Docs/migrations/BREAKING_CHANGES.md`](../Docs/migrations/BREAKING_CHANGES.md).
- GameTest-Smoke-Test nur auf `develop_1.21.1`; ab 1.21.2 entfernt.

## Testen

- `/tpd @s ~ ~ ~ minecraft:the_nether`, zurück per `/tpd @s <x> <y> <z> minecraft:overworld`.
- Eine Entity (z. B. Kuh) in eine andere Dimension, mehrere Ziele (`@e[type=cow]`), auf eine andere Entity (`/tpd @s <Spieler>`).
- Ungültige Position (außerhalb der Welt) gibt die Vanilla-Fehlermeldung; die Rotation bleibt erhalten, gleitende Spieler (Elytra) behalten ihren Schwung.
- Allgemeine Regeln: [`../Docs/testing/README.md`](../Docs/testing/README.md).

## Links

- Wiki: https://github.com/GeheimagentNr1/DimensionTeleport/wiki
- CurseForge: https://www.curseforge.com/minecraft/mc-mods/dimension-teleport (Projekt-ID 388219)
- Modrinth: https://modrinth.com/mod/49hXsr4B (Projekt-ID `49hXsr4B`)
- [`../Docs/migrations/1.21.1-to-1.21.2.md`](../Docs/migrations/1.21.1-to-1.21.2.md) (4e), [`../Docs/migrations/1.21.11-to-26.1.md`](../Docs/migrations/1.21.11-to-26.1.md), [`../Docs/releasing/release-process.md`](../Docs/releasing/release-process.md)
