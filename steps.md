# **Umsetzungsplan & Taskliste: Prototyp "Keycloak-Integrierte Webanwendung"**

### Phase 0: Projekt-Setup & Konfiguration als Code

*Ziel: Eine reproduzierbare Grundlage für alle Komponenten schaffen.*

  * **Task 0.1: Projektstruktur anlegen**
      * Erstelle ein Git-Repository mit einer Monorepo-Struktur:
        ```
        /keycloak-prototype
        ├── backend/      // Spring Boot App
        ├── frontend/     // React SPA
        ├── keycloak/     // Keycloak Konfiguration
        └── docker-compose.yml
        ```
  * **Task 0.2: Backend initialisieren**
      * Erstelle im `backend/` Verzeichnis eine neue Spring Boot Anwendung mit den Maven/Gradle-Abhängigkeiten: `web`, `security`, `oauth2-resource-server`, `test`, `org.testcontainers:keycloak`.
  * **Task 0.3: Frontend initialisieren**
      * Erstelle im `frontend/` Verzeichnis eine neue React-Anwendung (z.B. mit Vite). Füge die Abhängigkeiten `@react-keycloak/web` und `axios` hinzu.
  * **Task 0.4: Keycloak-Konfiguration als Code erstellen**
      * Erstelle im `keycloak/` Verzeichnis eine Datei namens `appx-realm.json`.
      * Diese JSON-Datei ist ein **Realm-Export**, der die gesamte Konfiguration enthält:
          * **Realm:** `appx-realm`
          * **Clients:**
              * `appx-frontend` (public, Standard Flow, PKCE, Valid Redirect URI: `http://localhost:3000/*`)
              * `appx-backend` (confidential, Service Account enabled, mit Client Secret)
          * **Rollen:** `APPX-Admin`, `APPX-Nutzer`
          * **Benutzer:**
              * `adminuser` (Passwort: `admin`, Rolle: `APPX-Admin`)
              * `normaluser` (Passwort: `user`, Rolle: `APPX-Nutzer`)
      * **Anmerkung:** Diese Datei wird später von Docker und Testcontainers genutzt, um Keycloak automatisch zu konfigurieren.

-----

### Phase 1: Backend - API-Sicherheit implementieren und testen

*Ziel: Ein gesichertes Backend, dessen Sicherheitskonzept isoliert validiert ist.*

  * **Task 1.1: Spring Security konfigurieren**
      * Konfiguriere in `application.yml` den OAuth2 Resource Server. Der `issuer-uri` Wert muss über eine Umgebungsvariable (`SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI`) gesetzt werden.
  * **Task 1.2: Test-Endpunkte erstellen**
      * Erstelle einen `ApiController` mit zwei Methoden:
          * `GET /api/public/data`: Gibt einen einfachen String zurück und ist nicht gesichert.
          * `GET /api/secure/data`: Gibt einen gesicherten String zurück und ist für alle authentifizierten Benutzer zugänglich.
  * **Task 1.3: Rollenbasierte Endpunkte erstellen**
      * Erstelle einen `AdminController` mit einer Methode:
          * `GET /api/admin/data`: Gibt einen Admin-String zurück und ist mittels `@PreAuthorize("hasRole('APPX-Admin')")` nur für Admins zugänglich.
  * **✅ Task 1.4: TEST (Integrationstest mit Testcontainers)**
      * Erstelle eine Testklasse `SecurityIntegrationTest.java`.
      * **Setup:** Starte im Test-Setup (`@BeforeAll`) einen **Keycloak-Testcontainer**. Konfiguriere den Container so, dass er die `keycloak/appx-realm.json` beim Start importiert. Übergib die dynamische `issuer-uri` des Containers an die Spring-Anwendung.
      * **Testfall 1 (Admin-Zugriff):** Hole ein Admin-Token vom Testcontainer, sende eine Anfrage an `/api/admin/data` und erwarte HTTP `200 OK`.
      * **Testfall 2 (Nutzer-Zugriff verweigert):** Hole ein Nutzer-Token, sende eine Anfrage an `/api/admin/data` und erwarte HTTP `403 Forbidden`.
      * **Testfall 3 (Allgemeiner Zugriff):** Hole ein Nutzer-Token, sende eine Anfrage an `/api/secure/data` und erwarte HTTP `200 OK`.
      * **Testfall 4 (Anonymer Zugriff):** Sende eine Anfrage ohne Token an `/api/secure/data` und erwarte HTTP `401 Unauthorized`.

-----

### Phase 2: Backend - Admin-Funktionalität implementieren und testen

*Ziel: Das Backend kann Benutzer in Keycloak über die Admin API verwalten.*

  * **Task 2.1: Keycloak Admin Client integrieren**
      * Füge die `keycloak-admin-client` Bibliothek zu den Abhängigkeiten des Backends hinzu.
      * Konfiguriere den Admin-Client als Spring Bean. Die Konfiguration (Server-URL, Realm, Client-ID, Client-Secret) muss über Umgebungsvariablen erfolgen.
  * **Task 2.2: Service-Schicht implementieren**
      * Erstelle einen `UserManagementService`, der die Logik für die Benutzerverwaltung kapselt (Nutzer auflisten, erstellen, Rollen zuweisen).
  * **Task 2.3: Admin-Controller erweitern**
      * Erweitere den `AdminController` um Endpunkte für die Benutzerverwaltung (z.B. `GET /api/admin/users`, `POST /api/admin/users`). Sichere alle Methoden mit `@PreAuthorize("hasRole('APPX-Admin')")`.
  * **✅ Task 2.4: TEST (Integrationstest mit Testcontainers)**
      * Erstelle eine Testklasse `UserManagementIntegrationTest.java` (kann denselben Testcontainer wie in 1.4 nutzen).
      * **Testfall (Nutzer erstellen):** Hole ein Admin-Token. Sende eine `POST`-Anfrage an `/api/admin/users`, um einen neuen Benutzer zu erstellen. Rufe danach mit dem Admin-Client direkt im Test die Benutzerliste von Keycloak ab und verifiziere, dass der neue Benutzer dort existiert.

-----

### Phase 3: Frontend - Authentifizierung implementieren

*Ziel: Ein Benutzer kann sich über das Frontend bei Keycloak anmelden und abmelden.*

  * **Task 3.1: Keycloak Provider konfigurieren**
      * Konfiguriere den `ReactKeycloakProvider` in der `App.js` oder `index.js`. Die Konfigurationswerte (URL, Realm, ClientId) müssen aus einer Umgebungsvariablen-Datei (`.env`) geladen werden.
  * **Task 3.2: Login/Logout-Logik erstellen**
      * Implementiere eine Komponente, die den `useKeycloak`-Hook verwendet.
      * Zeige bedingt einen "Login"-Button an (wenn `!keycloak.authenticated`) oder den Benutzernamen und einen "Logout"-Button (wenn `keycloak.authenticated`). Die Button-Handler rufen `keycloak.login()` und `keycloak.logout()` auf.
  * **Task 3.3: API-Client erstellen**
      * Erstelle eine `axios`-Instanz, die als Interceptor bei jeder Anfrage automatisch das `Authorization: Bearer ${keycloak.token}` Header hinzufügt.
  * **✅ Task 3.4: TEST (Manueller UI-Test)**
      * Starte Backend, Frontend und Keycloak (z.B. manuell oder über eine vorläufige Docker-Compose-Datei).
      * Öffne das SPA im Browser. Klicke auf Login, melde dich als `normaluser` an. Verifiziere, dass der Name angezeigt wird. Prüfe im Browser-Entwicklertool, dass der Token vorhanden ist. Logge dich wieder aus.

-----

### Phase 4: Frontend - UI-Seiten implementieren

*Ziel: Die anwendungslogischen Seiten sind funktionsfähig und rufen die gesicherten Backend-APIs auf.*

  * **Task 4.1: Routing einrichten**
      * Implementiere ein grundlegendes Routing (z.B. mit `react-router-dom`) für eine öffentliche Startseite, eine gesicherte `/profile` Seite und eine nur für Admins zugängliche `/admin` Seite.
  * **Task 4.2: Protected Routes erstellen**
      * Erstelle eine Komponente für geschützte Routen, die prüft, ob der Benutzer authentifiziert ist.
  * **Task 4.3: Admin-Route erstellen**
      * Erstelle eine Komponente für Admin-Routen, die zusätzlich prüft, ob `keycloak.hasRealmRole('APPX-Admin')` `true` ist.
  * **Task 4.4: Admin-Seite implementieren**
      * Baue die UI für die Benutzerverwaltung. Implementiere den Aufruf der Backend-Endpunkte (`GET /api/admin/users` etc.) über den in 3.3 erstellten API-Client.
  * **✅ Task 4.5: TEST (Manueller E2E-Funktionstest)**
      * Logge dich als `normaluser` ein: Verifiziere, dass du die `/profile` Seite, aber nicht die `/admin` Seite aufrufen kannst.
      * Logge dich als `adminuser` ein: Verifiziere, dass du die `/admin` Seite aufrufen kannst. Erstelle über die UI einen neuen Benutzer und verifiziere, dass er in der Liste erscheint.

-----

### Phase 5: Finale Containerisierung

*Ziel: Die gesamte Anwendung ist mit einem einzigen Befehl startbar.*

  * **Task 5.1: Dockerfiles erstellen**
      * Erstelle ein `Dockerfile` im `backend/` Verzeichnis für die Spring Boot Anwendung.
      * Erstelle ein `Dockerfile` im `frontend/` Verzeichnis (Multi-Stage-Build), das die React-App baut und die statischen Dateien in einen Nginx-Container kopiert.
  * **Task 5.2: Docker Compose erstellen**
      * Finalisiere die `docker-compose.yml` im Hauptverzeichnis:
          * **Service `keycloak`:** Nutzt das offizielle Keycloak-Image. Mappt das `keycloak/appx-realm.json` in das Import-Verzeichnis des Containers. Setzt Admin-Passwort per Umgebungsvariable.
          * **Service `backend`:** Baut das Backend-Dockerfile. Übergibt alle notwendigen Umgebungsvariablen (Keycloak-URL, Secret etc.). Hängt von `keycloak` ab.
          * **Service `frontend`:** Baut das Frontend-Dockerfile und exposed den Port (z.B. 80).
  * **✅ Task 5.3: TEST (Finaler Systemtest)**
      * Führe `docker-compose up --build` in einem sauberen Zustand aus.
      * Warte, bis alle Container gestartet sind.
      * Führe den manuellen E2E-Test aus Task 4.5 erneut durch, um das Zusammenspiel aller containerisierten Komponenten zu validieren.
