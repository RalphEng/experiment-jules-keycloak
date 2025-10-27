# Lessons Learned

## Docker Compose Healthcheck ohne curl

**Problem:**
Der Keycloak-Container wurde als "unhealthy" markiert, obwohl er erfolgreich gestartet ist. Die Fehlermeldung war:
```
dependency failed to start: container experiment-jules-keycloak-keycloak-1 is unhealthy
```

**Ursache:**
Der ursprüngliche Healthcheck verwendete `curl`:
```yaml
healthcheck:
  test: ["CMD-SHELL", "curl -f http://localhost:8080/health/ready || exit 1"]
```

Das Problem: `curl` ist nicht im offiziellen Keycloak-Image `quay.io/keycloak/keycloak:22.0.5` installiert.

**Lösung (Best Practice):**
Die einfachste und zuverlässigste Lösung ist, den Healthcheck komplett zu entfernen und die `depends_on` Bedingung zu vereinfachen:

```yaml
# Keycloak Service - OHNE Healthcheck
services:
  keycloak:
    image: quay.io/keycloak/keycloak:22.0.5
    # ... weitere Config ...
    # KEIN healthcheck mehr!

  backend:
    depends_on:
      - keycloak  # Einfache Abhängigkeit ohne 'condition: service_healthy'
```

**Warum diese Lösung?**
- Keycloak startet zuverlässig und ist nach ca. 10-15 Sekunden bereit
- Das Backend kann mit Retry-Logik umgehen, falls Keycloak noch nicht ganz bereit ist
- Keine Abhängigkeit von Tools wie `curl`, `wget` oder `nc` im Container
- Einfacher und wartbarer Code

**Alternative Lösungen (getestet, aber nicht empfohlen):**
1. Bash TCP sockets verwenden - funktioniert nicht zuverlässig, komplex, plattformabhängig
2. `curl` im Container installieren - erfordert Custom Dockerfile
3. Längere `start_period` und mehr `retries` - maskiert nur das Problem

**Weitere Anpassungen:**
- `version: "3"` aus docker-compose.yml entfernt (veraltet seit Docker Compose v1.27.0)

**Quellen:**
- Docker Compose Healthcheck Documentation: https://docs.docker.com/compose/compose-file/compose-file-v3/#healthcheck
- Bash TCP Socket Technique: Standard bash feature, verfügbar in den meisten Container-Images
- Keycloak Health Endpoints: https://www.keycloak.org/server/health
- Docker Compose version field deprecation: https://docs.docker.com/compose/compose-file/04-version-and-name/
- https://gist.github.com/sarath-soman/5d9aec06953bbd0990c648605d4dba07
- 
**Datum:** 2025-10-16

---

## WSL2 Port Forwarding Problem

**Problem:**
Docker-Container laufen in WSL2, sind aber von Windows aus unter `http://localhost:8080` nicht erreichbar.
- Innerhalb WSL2: `curl http://localhost:8080` funktioniert ✓
- Von Windows Browser: `http://localhost:8080` - Timeout/Connection refused ✗

**Ursache:**
WSL2 verwendet ein separates virtuelles Netzwerk (z.B. `172.20.122.193`). Ports werden nicht automatisch an Windows weitergeleitet.

**Lösung 1 (Temporär):**
WSL2 IP-Adresse verwenden:
```bash
# In WSL2 ausführen:
ip addr show eth0 | grep "inet "
# Beispiel Ausgabe: inet 172.20.122.193/20

# Von Windows aus dann aufrufen:
http://172.20.122.193:8080
```

**Nachteil:** IP-Adresse ändert sich bei jedem WSL2-Neustart!

**Lösung 2 (Permanent - Empfohlen):**
Port Forwarding automatisch einrichten. In Windows PowerShell (als Administrator):

```powershell
# Port 8080 (Keycloak)
netsh interface portproxy add v4tov4 listenport=8080 listenaddress=0.0.0.0 connectport=8080 connectaddress=172.20.122.193

# Port 8081 (Backend)
netsh interface portproxy add v4tov4 listenport=8081 listenaddress=0.0.0.0 connectport=8081 connectaddress=172.20.122.193

# Port 3000 (Frontend)
netsh interface portproxy add v4tov4 listenport=3000 listenaddress=0.0.0.0 connectport=3000 connectaddress=172.20.122.193

# Prüfen:
netsh interface portproxy show all
```

**Lösung 3 (Automatisches Script):**
`.wslconfig` in `C:\Users\<username>\.wslconfig`:
```ini
[wsl2]
localhostForwarding=true
```

Dann WSL2 neu starten:
```powershell
wsl --shutdown
```

**Lösung 4 (Docker Desktop verwenden):**
Wenn Docker Desktop for Windows installiert ist, werden Ports automatisch weitergeleitet.

**Prüfung:**
```bash
# In WSL2:
curl http://localhost:8080

# Von Windows PowerShell:
curl http://localhost:8080
# oder im Browser: http://localhost:8080
```

**Quellen:**
- WSL2 Networking: https://docs.microsoft.com/en-us/windows/wsl/networking
- WSL2 Port Forwarding: https://github.com/microsoft/WSL/issues/4150
- netsh portproxy: https://learn.microsoft.com/en-us/windows-server/networking/technologies/netsh/netsh-interface-portproxy

**Datum:** 2025-10-16
