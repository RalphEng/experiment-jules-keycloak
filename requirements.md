# Requirements Specification: Prototyp "Keycloak-Integrierte Webanwendung"

* **Dokument-Version:** 1.0
* **Datum:** 15. Oktober 2025
* **Autor:** Gemini Architekt

### 1. Einleitung

#### 1.1 Zweck des Dokuments
Dieses Dokument beschreibt die funktionalen und nicht-funktionalen Anforderungen für den Prototypen einer modernen Webanwendung. Der Schwerpunkt des Prototyps liegt auf der Implementierung einer robusten und entkoppelten Benutzer- und Zugriffsverwaltung unter Verwendung von **Keycloak** als Identity and Access Management (IAM) System.

#### 1.2 Projektumfang
Der Prototyp umfasst drei Kernkomponenten:
1.  Ein **Single-Page-Application (SPA) Frontend**, entwickelt mit React.
2.  Ein **Backend-Service**, entwickelt mit Java Spring Boot.
3.  Eine **Keycloak-Instanz** für die zentrale Identitätsverwaltung.

Ziel ist es, den gesamten Authentifizierungs- und Autorisierungs-Flow sowie grundlegende administrative Benutzerverwaltungsfunktionen abzubilden. Jegliche anwendungsspezifische Fachlogik (z.B. Projektmanagement, Ticketing etc.) ist explizit **außerhalb** des Geltungsbereichs.

#### 1.3 Definitionen
* **IAM:** Identity and Access Management. Ein System zur Verwaltung von Benutzeridentitäten und deren Zugriffsrechten.
* **JWT:** JSON Web Token. Ein kompakter, URL-sicherer Standard zur Darstellung von "Claims", die zwischen zwei Parteien übertragen werden.
* **Rolle:** Definiert eine grobgranulare Berechtigungsstufe für einen Benutzer (z.B. `APPX-Admin`).
* **Admin:** Ein Benutzer mit der Rolle `APPX-Admin`, der erweiterte Rechte zur Benutzerverwaltung hat.
* **Nutzer:** Ein Benutzer mit der Rolle `APPX-Nutzer` ohne administrative Rechte.

---

### 2. Systemüberblick

Die Anwendung folgt einer klassischen 3-Tier-Architektur. Das SPA-Frontend kommuniziert über eine REST-API mit dem Backend. Das Backend sichert seine Endpunkte und verlässt sich für die Authentifizierung und Autorisierung vollständig auf Keycloak. Die Benutzerverwaltung durch Admins erfolgt über eine dedizierte UI im Frontend, die über das Backend mit der Admin-API von Keycloak interagiert.



---

### 3. Funktionale Anforderungen (FR)

#### 3.1 Authentifizierung
* **FR-01: Login-Prozess:** Ein anonymer Benutzer muss sich authentifizieren können. Der Klick auf "Login" im SPA leitet den Benutzer zur von Keycloak bereitgestellten Login-Seite weiter. Nach erfolgreicher Anmeldung wird der Benutzer zurück zum SPA geleitet und ist eingeloggt.
* **FR-02: Logout-Prozess:** Ein eingeloggter Benutzer muss sich über einen "Logout"-Button im SPA abmelden können. Dies beendet die lokale Session im SPA und leitet idealerweise auch einen Logout bei Keycloak ein.
* **FR-03: Gesicherter API-Zugriff:** Alle API-Endpunkte des Backends, mit Ausnahme eventueller öffentlicher Endpunkte (im Prototyp nicht vorgesehen), müssen gesichert sein. Anfragen ohne gültiges JWT müssen mit dem HTTP-Status `401 Unauthorized` abgelehnt werden.

#### 3.2 Autorisierung
* **FR-04: Rollenbasierter Zugriff:** Das Backend muss den Zugriff auf bestimmte API-Endpunkte basierend auf den im JWT enthaltenen Rollen einschränken.
    * Ein Endpunkt `GET /api/admin/data` darf nur für Benutzer mit der Rolle `APPX-Admin` zugänglich sein (Ergebnis: `403 Forbidden` für andere).
    * Ein Endpunkt `GET /api/user/data` muss für alle authentifizierten Benutzer (sowohl `APPX-Nutzer` als auch `APPX-Admin`) zugänglich sein.

#### 3.3 Benutzer-Interface (Frontend)
* **FR-05: Bedingte UI-Anzeige:** Das SPA muss UI-Elemente basierend auf dem Anmeldestatus und der Rolle des Benutzers ein- oder ausblenden.
    * Ein anonymer Benutzer sieht nur einen "Login"-Button.
    * Ein eingeloggter `APPX-Nutzer` sieht seinen Namen, einen "Logout"-Button und einen Link zur "User-Seite".
    * Ein eingeloggter `APPX-Admin` sieht zusätzlich einen Link zur "Admin-Verwaltungsseite".
* **FR-06: Anzeige von Benutzerinformationen:** Nach dem Login muss das SPA den Vornamen, Nachnamen und die E-Mail-Adresse des Benutzers aus dem erhaltenen JWT auslesen und anzeigen können.

#### 3.4 Administrative Benutzerverwaltung (Admin-Funktionen)
* **FR-07: Benutzerliste anzeigen:** Ein Admin muss eine Seite aufrufen können, auf der alle in Keycloak registrierten Benutzer in einer Tabelle angezeigt werden (Anzuzeigende Felder: Vorname, Nachname, E-Mail, Status (Aktiv/Inaktiv)).
* **FR-08: Benutzer erstellen:** Ein Admin muss über ein Formular einen neuen Benutzer in Keycloak anlegen können. Erforderliche Felder: Vorname, Nachname, E-Mail, initiales Passwort.
* **FR-09: Benutzer-Rollen zuweisen:** Ein Admin muss in der Benutzerliste oder einer Detailansicht die Rollen eines Benutzers (`APPX-Admin`, `APPX-Nutzer`) ändern können.
* **FR-10: Benutzer aktivieren/deaktivieren:** Ein Admin muss den Status eines Benutzers zwischen "aktiviert" und "deaktiviert" umschalten können.

---

### 4. Nicht-Funktionale Anforderungen (NFR)

* **NFR-01: Technologie-Stack:**
    * **Frontend:** React mit `keycloak-js` (bzw. `@react-keycloak/web`).
    * **Backend:** Java Spring Boot mit Spring Security (OAuth2 Resource Server).
    * **IAM:** Keycloak.
* **NFR-02: Entwicklungsumgebung:** Die gesamte Anwendung (Frontend, Backend, Keycloak) muss über eine einzige `docker-compose.yml` Datei für die lokale Entwicklung startbar sein.
* **NFR-03: Konfigurationsmanagement:** Sensible Konfigurationsdaten (z.B. `client-secret`) dürfen nicht im Code hardcodiert sein. Sie müssen über Umgebungsvariablen bereitgestellt werden.
* **NFR-04: Testbarkeit:** Das Backend muss Integrationstests enthalten, die die API-Sicherheit gegen eine mit **Testcontainers** gestartete Keycloak-Instanz prüfen.

---

### 5. Außerhalb des Geltungsbereichs (Out of Scope)

Folgende Funktionen werden im Prototyp bewusst **nicht** umgesetzt:
* Benutzer-Selbstregistrierung.
* "Passwort vergessen"-Funktionalität (wird von Keycloak bereitgestellt, aber nicht aktiv in die App integriert).
* Bearbeitung des eigenen Benutzerprofils durch den Benutzer.
* Die serverseitige Synchronisation der Benutzerdatenbank ("Schattenkopie").
* Jegliche fachliche Domänenlogik.

---

Als Nächstes würde ich basierend auf diesen Anforderungen die **Architekturbeschreibung** und die **detaillierte Taskliste** für den KI-Agenten erstellen.
