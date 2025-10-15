# Architekturbeschreibung: Prototyp "Keycloak-Integrierte Webanwendung"

* **Dokument-Version:** 1.0
* **Datum:** 15. Oktober 2025
* **Autor:** Gemini Architekt

### 1. Einleitung

Dieses Dokument beschreibt die Softwarearchitektur des Prototyps. Es erläutert die Hauptkomponenten, deren Verantwortlichkeiten und vor allem deren Zusammenspiel. Die Architektur ist darauf ausgelegt, die in der *Requirements Specification v1.0* definierten Anforderungen zu erfüllen, mit einem klaren Fokus auf eine sichere und entkoppelte Authentifizierung und Autorisierung.

### 2. Architekturelle Ziele & Leitprinzipien

* **Entkopplung der Identität:** Die Benutzerverwaltung (Authentifizierung, Speicherung von Credentials, Rollenmanagement) ist vollständig an Keycloak als zentralen Identity Provider (IdP) delegiert. Die Anwendung selbst speichert keine Passwörter.
* **Standardkonformität:** Die Kommunikation zwischen den Komponenten basiert ausschließlich auf den offenen Standards **OAuth 2.0** und **OpenID Connect (OIDC) 1.0**. Dies gewährleistet Interoperabilität und Sicherheit.
* **Klare Verantwortlichkeiten:** Jede Komponente hat eine definierte Aufgabe. Das Frontend ist für die Darstellung zuständig, das Backend für die Geschäftslogik und die API, und Keycloak für die Identität.
* **Sicherheit als Priorität:** Die Architektur verwendet moderne Sicherheitspraktiken wie den **Authorization Code Flow mit PKCE** für das Frontend und eine tokenbasierte Autorisierung im Backend.

### 3. Systemkomponenten



1.  **Frontend (React SPA):** Ein **Public Client** im Sinne von OAuth 2.0. Das Frontend läuft vollständig im Browser des Benutzers.
    * **Verantwortlichkeiten:** Rendern der Benutzeroberfläche, Anstoßen des Login-/Logout-Prozesses, sicheres Speichern der Tokens im Browser und Anhängen des Access Tokens an alle API-Aufrufe.
2.  **Backend (Spring Boot API):** Ein **Resource Server** im Sinne von OAuth 2.0. Das Backend stellt die gesicherte REST-API bereit. Für administrative Aufgaben agiert es zusätzlich als **Confidential Client**.
    * **Verantwortlichkeiten:** Schutz der Endpunkte, Validierung von JWTs, Durchsetzung von rollenbasierten Zugriffsregeln (`@PreAuthorize`), Bereitstellung von Geschäftslogik und Interaktion mit der Keycloak Admin API für die Benutzerverwaltung.
3.  **Identity & Access Management (Keycloak):** Der zentrale **Authorization Server** und Identitätsspeicher.
    * **Verantwortlichkeiten:** Hosten der Login-Seite, Authentifizieren von Benutzern, Ausstellen von Tokens (Access & Refresh Tokens), Verwalten von Benutzern, Rollen und Clients, Bereitstellung von Endpunkten für Token-Validierung und -Verwaltung.

---

### 4. Zentrale Interaktionsflüsse

Dies ist der Kern der Architektur und beschreibt, wie die Komponenten zusammenarbeiten.

#### 4.1 Benutzer-Authentifizierung (Login)

Der Login-Prozess folgt dem **OAuth 2.0 Authorization Code Flow mit PKCE**. Dies ist der sicherste Standard für SPAs, da er "Authorization Code Interception"-Angriffe verhindert.

**Ablauf:**


1.  **Initiierung:** Der Benutzer klickt auf "Login". Die `keycloak-js` Bibliothek im SPA generiert ein kryptografisches Geheimnis (`code_verifier`).
2.  **Redirect zu Keycloak:** Die Bibliothek leitet den Browser zur `/auth` URL von Keycloak weiter. Im Redirect werden Parameter wie `client_id`, `response_type=code` und eine Transformation des Geheimnisses (`code_challenge`) mitgesendet.
3.  **Authentifizierung:** Der Benutzer authentifiziert sich direkt bei Keycloak (Benutzername/Passwort). Keycloak hat an dieser Stelle keinen Kontakt zum Backend der Anwendung.
4.  **Authorization Code:** Nach erfolgreichem Login leitet Keycloak den Browser zurück zum SPA (zur `redirect_uri`). Im URL-Parameter befindet sich ein einmalig gültiger `authorization_code`.
5.  **Token-Austausch:** Das SPA sendet diesen `code` zusammen mit dem ursprünglichen Geheimnis (`code_verifier`) direkt an den `/token` Endpunkt von Keycloak. Dieser Schritt erfolgt im Hintergrund (Browser-JavaScript zu Keycloak-Server).
6.  **Token-Empfang:** Keycloak validiert den `code` und den `code_verifier`. Stimmen sie überein, stellt Keycloak ein **Access Token (JWT)** und ein **Refresh Token** aus und sendet sie an das SPA zurück.
7.  **Sitzung:** Das SPA speichert die Tokens und gilt nun als authentifiziert.

---

#### 4.2 Gesicherte API-Kommunikation

Jeder Zugriff auf geschützte Backend-Ressourcen erfolgt mittels **Bearer Token Authentication**.

**Ablauf:**
1.  **API-Aufruf:** Das SPA möchte eine geschützte Ressource abrufen (z.B. `GET /api/user/data`) und fügt den `Authorization`-Header mit dem Access Token hinzu: `Authorization: Bearer <ey...JWT...>`.
2.  **Validierung im Backend:** Das Spring Boot Backend empfängt die Anfrage. Das **Spring Security OAuth2 Resource Server** Modul führt automatisch folgende Schritte durch:
    a.  **Signaturprüfung:** Es prüft die kryptografische Signatur des JWT. Den dafür benötigten öffentlichen Schlüssel holt es sich einmalig von der JWKS-URL, die im OIDC-Discovery-Endpunkt von Keycloak veröffentlicht wird.
    b.  **Claim-Validierung:** Es prüft die Standard-Claims des Tokens, insbesondere das Ablaufdatum (`exp`) und den Aussteller (`iss`), um sicherzustellen, dass das Token von der vertrauenswürdigen Keycloak-Instanz stammt und noch gültig ist.
3.  **Autorisierung:** Wenn das Token gültig ist, extrahiert Spring Security die darin enthaltenen Rollen (z.B. aus dem `realm_access.roles` Claim). Anhand dieser Rollen werden die `@PreAuthorize("hasRole('APPX-Admin')")` Annotationen an den Controller-Methoden ausgewertet.
4.  **Zugriff:** Nur wenn die Validierung und Autorisierung erfolgreich sind, wird die Anfrage an die Controller-Methode weitergeleitet. Andernfalls antwortet Spring Security direkt mit `401 Unauthorized` oder `403 Forbidden`.

---

#### 4.3 Administrative Benutzerverwaltung

Für die Admin-Funktionen nutzt das Backend die **Keycloak Admin API**. Hier agiert das Backend selbst als Client gegenüber Keycloak.

**Ablauf:**


1.  **Admin-Aktion im SPA:** Ein im SPA angemeldeter Admin (mit `APPX-Admin`-Rolle) führt eine Aktion aus, z.B. das Erstellen eines neuen Benutzers.
2.  **Request an das Backend:** Das SPA sendet eine Anfrage an das eigene Backend, z.B. `POST /api/admin/users`. Diese Anfrage ist mit dem **JWT des Admins** gesichert.
3.  **Backend validiert den Admin:** Das Backend validiert das Admin-JWT wie in 4.2 beschrieben und prüft, ob die `APPX-Admin`-Rolle vorhanden ist.
4.  **Backend authentifiziert sich selbst:** Um mit der Keycloak Admin API zu sprechen, benötigt das Backend seine eigene Berechtigung. Es verwendet den **Client Credentials Flow**, um sich mit seiner eigenen `client-id` und seinem `client-secret` bei Keycloak zu authentifizieren. Keycloak stellt daraufhin ein **Service-Account-Token** für das Backend aus.
5.  **Aufruf der Admin API:** Mit diesem Service-Account-Token ruft das Backend nun den entsprechenden Endpunkt der Keycloak Admin API auf (z.B. `POST /admin/realms/.../users`).
6.  **Antwort:** Das Backend verarbeitet die Antwort von Keycloak und leitet das Ergebnis an das SPA weiter.

Dieser zweistufige Prozess ist entscheidend: Das Backend validiert zuerst die Berechtigung des *Endbenutzers* und handelt dann mit seiner eigenen, privilegierten *Service-Identität* gegenüber Keycloak.

---

### 5. Keycloak Konfiguration (Zusammenfassung)

* **Realm:** `appx-realm`
* **Client 1 (Frontend):**
    * **Client ID:** `appx-frontend`
    * **Access Type:** `public`
    * **Standard Flow Enabled:** `true`
    * **Valid Redirect URIs:** URL des React-Frontends
* **Client 2 (Backend):**
    * **Client ID:** `appx-backend`
    * **Access Type:** `confidential` (mit Secret)
    * **Service Accounts Enabled:** `true`
    * **Berechtigungen:** Dem Service-Account müssen die notwendigen `realm-management` Rollen zugewiesen werden (z.B. `manage-users`, `view-users`).
* **Rollen:**
    * **Realm Roles:** `APPX-Admin`, `APPX-Nutzer`
