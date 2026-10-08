---
label: Server (Docker)
order: 90
---

# Server (Docker)

Run Marginalia as a server when several people use it, or when you want to open it from other devices. Every user
gets their own books, lorebooks, inference providers and protocols.

## Requirements

- [Docker](https://docs.docker.com/get-docker/) with Docker Compose
- About 512 MB of memory for Marginalia
- For access over the internet: a domain and a reverse proxy with HTTPS (see [below](#https-and-reverse-proxy))

## Install

```sh
git clone https://github.com/Enerccio/Marginalia.git
cd Marginalia
docker compose up -d
```

The first `docker compose up` builds the image from source, which takes a few minutes. Marginalia then listens on port
**8080**. Open `http://<server>:8080` and create the administrator account (see [First start](first-start.md)).

## Configuration

Everything is set in `docker-compose.yml`:

```yaml
services:
  marginalia:
    build:
      context: .
      dockerfile: Dockerfile
    container_name: marginalia-app
    restart: unless-stopped
    ports:
      - "8080:8080"
    volumes:
      - ./data:/var/marginalia/.marginalia
    environment:
      - JAVA_TOOL_OPTIONS=-Duser.home=/var/marginalia -Xms256m -Xmx512m -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/var/marginalia/.marginalia/heapdump.hprof
```

| Setting | What it does |
|---|---|
| `ports` | `"8080:8080"` publishes Marginalia on port 8080 of the host. Use `"127.0.0.1:8080:8080"` when only a reverse proxy on the same host should reach it. |
| `volumes` | `./data` holds the database, backups and extensions. Keep it, and include it in your server backups. |
| `-Xmx512m` | Maximum memory. Raise it (for example `-Xmx1g`) for many users or very large books. |

After changing the file, apply it with `docker compose up -d`.

## HTTPS and reverse proxy

!!!danger Don't use plain HTTP over the internet
Passwords and login cookies are sent with every request. Over plain HTTP anyone on the way can read them.
Put Marginalia behind a reverse proxy with HTTPS.
!!!

The *Save login* option on the login screen also needs HTTPS: its cookies are marked secure, so browsers keep them
only on HTTPS connections.

Marginalia updates the page over a WebSocket connection, so the proxy must forward WebSocket upgrades. Examples
for `marginalia.example.com`:

+++ Caddy
Caddy gets the certificate and forwards WebSockets automatically.

```
marginalia.example.com {
    reverse_proxy localhost:8080
}
```
+++ nginx
```nginx
server {
    listen 443 ssl;
    server_name marginalia.example.com;

    ssl_certificate     /etc/letsencrypt/live/marginalia.example.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/marginalia.example.com/privkey.pem;

    client_max_body_size 100m;   # uploads of backups and extensions

    location / {
        proxy_pass http://127.0.0.1:8080;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_read_timeout 1h;   # long generations keep the connection open
    }
}
```
+++

## Models on the same machine

Inside the container, `localhost` is the container itself, not the server. To use a model server that runs on the
Docker host (llama.cpp, Ollama, LM Studio...), use the address `http://host.docker.internal:<port>/v1` in the
[inference provider](../inference-providers.md). On Linux, add this to the service in `docker-compose.yml`:

```yaml
    extra_hosts:
      - "host.docker.internal:host-gateway"
```

The model server must also listen on an address the container can reach, not only on `127.0.0.1`.

## Updating

```sh
git pull
docker compose up -d --build
```

The database is upgraded automatically when the new version starts. Make a
[database backup](../administration/database-backups.md) first.

## Logs

```sh
docker compose logs -f marginalia
```
