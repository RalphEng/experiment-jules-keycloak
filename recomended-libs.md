Für den Prototypen sollten die folgenden etablierten und modernen Libraries verwendet werden, um die Entwicklung zu beschleunigen und bewährte Muster zu nutzen.

### Frontend (React SPA)

* **`@react-keycloak/web`**: Die wichtigste Bibliothek für die Integration. Sie stellt React Hooks (z.B. `useKeycloak`) und Komponenten bereit, die den Authentifizierungs-Flow und den Zugriff auf Token-Informationen extrem vereinfachen.
* **`axios`**: Ein beliebter und robuster HTTP-Client zur Kommunikation mit dem Backend. Er lässt sich leicht mit einem "Interceptor" konfigurieren, der automatisch das JWT-Bearer-Token an jede ausgehende API-Anfrage anhängt.
* **`react-router-dom`**: Die Standard-Bibliothek für das Routing in React. Sie wird benötigt, um die verschiedenen Seiten der Anwendung zu verwalten (z.B. `/`, `/profile`, `/admin`) und den Zugriff auf bestimmte Routen zu schützen.

---

### Backend (Spring Boot)

* **`spring-boot-starter-oauth2-resource-server`**: Dies ist die zentrale Abhängigkeit für Spring Security. Sie konfiguriert die Anwendung als Resource Server, der in der Lage ist, eingehende JWTs von einem externen Authorization Server wie Keycloak zu validieren.
* **`spring-boot-starter-web`**: Die Standard-Abhängigkeit für die Erstellung von REST-APIs mit Spring Boot.
* **`org.keycloak:keycloak-admin-client`**: Die offizielle Java-Bibliothek von Keycloak, um mit der Keycloak Admin REST API zu interagieren. Diese wird benötigt, um die administrativen Funktionen wie das Erstellen und Verwalten von Benutzern aus dem Backend heraus umzusetzen.

---

### Testing (Backend-Integrationstests)

* **`org.testcontainers:junit-jupiter`**: Die Kernbibliothek für **Testcontainers**, die eine nahtlose Integration mit JUnit 5 ermöglicht, um Docker-Container als Teil des Test-Lebenszyklus zu verwalten.
* **`org.testcontainers:keycloak`**: Das spezialisierte Testcontainers-Modul für Keycloak. Es vereinfacht das Starten eines vorkonfigurierten Keycloak-Containers für die Integrationstests erheblich.
