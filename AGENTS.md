# AGENTS.md - Dimension Teleport

## Projekt-Übersicht

**Dimension Teleport** ist ein NeoForge Minecraft Mod für Minecraft 1.21.1.
- **Mod ID**: `dimensionteleport`
- **Package**: `de.geheimagentnr1.dimensionteleport`
- **Java Version**: 21
- **NeoForge Version**: 21.1.x

Fügt einen interdimensionalen Teleport-Command hinzu.

## Abhängigkeiten

Keine Mod-Abhängigkeiten - eigenständiger Mod.

## Projektstruktur

```
src/main/java/de/geheimagentnr1/dimensionteleport/
└── DimensionTeleport.java    # Haupt-Mod-Klasse
```

## Besonderheiten

- **Minimaler Mod**: Sehr kleiner Mod mit nur einer Klasse
- **Server-fokussiert**: DisplayTest ist `IGNORE_SERVER_VERSION`

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
