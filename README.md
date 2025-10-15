# Prototyp: Moderne Authentifizierung mit Keycloak, Spring Boot & React

## ⚠️ Wichtiger Hinweis: Status des Projekts

Dieses Projekt ist ein **technischer Prototyp**. Sein primäres Ziel ist es, die Architektur und das Zusammenspiel von einem Single-Page-Application (SPA) Frontend, einem Java-Backend und einem externen Identity-Provider (Keycloak) zu demonstrieren und zu validieren.

Der Code ist für Demonstrationszwecke geschrieben und ist **nicht für den produktiven Einsatz** vorgesehen. Es fehlen Aspekte wie umfassendes Error-Handling, Produktions-Hardening und eine vollständige Testabdeckung.

## Zweck des Prototyps

Der Hauptzweck dieses Projekts ist die Erprobung und Demonstration folgender Konzepte:

  * **Entkoppelte Benutzerverwaltung:** Wie eine Anwendung die vollständige Authentifizierung und Benutzerverwaltung an Keycloak delegieren kann.
  * **Standardbasierte Sicherheit:** Die praktische Anwendung von **OAuth 2.0** und **OpenID Connect (OIDC)** in einem modernen Technologiestack.
  * **Rollenbasierte Zugriffskontrolle (RBAC):** Wie das Backend API-Endpunkte basierend auf Rollen schützt, die zentral in Keycloak verwaltet werden.
  * **Administrative Integration:** Wie das Backend als privilegierter Client mit der Keycloak Admin API interagieren kann, um Benutzer zu verwalten, während die Admin-UI Teil der Anwendungs-SPA bleibt.
  * **Konfiguration als Code:** Wie eine Keycloak-Instanz vollständig über Konfigurationsdateien (Realm-Export) für eine reproduzierbare Entwicklungsumgebung aufgesetzt werden kann.

## Demonstrierte Features

  * Vollständiger Login- und Logout-Flow über Keycloak.
  * Gesicherte Backend-API (Resource Server).
  * Rollenbasierter Zugriff auf verschiedene API-Endpunkte (`APPX-Nutzer` vs. `APPX-Admin`).
  * Eine Admin-Oberfläche zum:
      * Anzeigen aller Benutzer.
      * Erstellen neuer Benutzer.
      * Zuweisen von Rollen.
      * Aktivieren/Deaktivieren von Benutzern.

## Architektur im Überblick

Die Anwendung besteht aus drei containerisierten Diensten:

1.  **`frontend`**: Eine React SPA, die für die UI-Darstellung und den OIDC-Login-Flow verantwortlich ist.
2.  **`backend`**: Eine Spring Boot API, die als gesicherter Resource Server agiert und die Admin-Logik kapselt.
3.  **`keycloak`**: Der zentrale Identity and Access Management Server.

## Voraussetzungen

  * [Docker](https://www.docker.com/get-started)
  * [Docker Compose](https://docs.docker.com/compose/install/)

## Ausführen des Prototyps

Das gesamte System kann mit einem einzigen Befehl gestartet werden. Führe diesen Befehl im Hauptverzeichnis des Projekts aus:

```bash
docker-compose up --build
```

Nachdem alle Container gestartet sind, sind die Dienste unter folgenden Adressen erreichbar:

  * **Frontend-Anwendung:** [http://localhost:3000](https://www.google.com/search?q=http://localhost:3000)
  * **Keycloak Admin Console:** [http://localhost:8080](https://www.google.com/search?q=http://localhost:8080)
      * **Admin Login:** `admin` / `admin` (dies ist für die Keycloak-Konsole selbst)

## Test-Benutzer

Die Keycloak-Instanz wird automatisch mit einem Realm (`appx-realm`) und den folgenden Test-Benutzern für die Anwendung konfiguriert:

| Benutzername | Passwort | Rollen | Beschreibung |
| :--- | :--- | :--- | :--- |
| `adminuser` | `admin` | `APPX-Admin` | Kann die Admin-Verwaltungsseite sehen und Benutzer verwalten. |
| `normaluser`| `user` | `APPX-Nutzer` | Hat nur Zugriff auf die Standard-Benutzeransichten. |

## Projektstruktur

```
/
├── backend/      # Spring Boot Backend Service
├── frontend/     # React Single-Page-Application
├── keycloak/     # Keycloak "Konfiguration als Code"
│   └── appx-realm.json
└── docker-compose.yml # Definiert und startet alle Dienste
```

## Keycloak: Konfiguration als Code

Es muss **keine manuelle Konfiguration** in der Keycloak-GUI vorgenommen werden. Alle notwendigen Einstellungen (Realm, Clients, Rollen, Benutzer) sind in der Datei `keycloak/appx-realm.json` definiert. Diese Datei wird beim ersten Start des Keycloak-Containers automatisch importiert, um eine konsistente und reproduzierbare Umgebung zu gewährleisten.
