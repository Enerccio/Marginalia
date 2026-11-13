---
label: Serveur (Docker)
order: 90
---

# Serveur (Docker)

Lancez Marginalia en tant que serveur lorsque plusieurs personnes l'utilisent, ou lorsque vous souhaitez y accéder depuis d'autres appareils. Chaque utilisateur dispose de ses propres livres, lorebooks, fournisseurs d'inférence et protocoles.

<a id="requirements"></a>

## Prérequis

- [Docker](https://docs.docker.com/get-docker/) avec Docker Compose
- Environ 1 Go de mémoire pour Marginalia
- Pour un accès via Internet : un domaine et un reverse proxy avec HTTPS (voir [ci-dessous](#https-and-reverse-proxy))

<a id="install"></a>

## Installation

```sh
git clone https://github.com/Enerccio/Marginalia.git
cd Marginalia
docker compose up -d
```

Le premier `docker compose up` construit l'image à partir des sources, ce qui prend quelques minutes. Marginalia écoute ensuite sur le port
**8080**. Ouvrez `http://<serveur>:8080` et créez le compte administrateur (voir [Premier démarrage](first-start.md)).

<a id="prebuilt-image"></a>

### Image précompilée

Au lieu de construire l'image à partir des sources, vous pouvez utiliser l'image publiée avec chaque version (`linux/amd64` et `linux/arm64`).
Dans `docker-compose.yml`, remplacez le bloc `build:` par `image:` :

```yaml
services:
  marginalia:
    image: ghcr.io/enerccio/marginalia:latest
```

`latest` correspond à la version la plus récente, `edge` à la version de développement, et `1.1.0` (ou `1.1`) fixe une version précise. Le reste du
fichier reste identique.

## Configuration

Tout se règle dans `docker-compose.yml` :

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
      - JAVA_TOOL_OPTIONS=-Duser.home=/var/marginalia -Xms256m -Xmx1g -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/var/marginalia/.marginalia/heapdump.hprof
```

| Paramètre | Rôle |
|---|---|
| `ports` | `"8080:8080"` publie Marginalia sur le port 8080 de l'hôte. Utilisez `"127.0.0.1:8080:8080"` si seul un reverse proxy sur le même hôte doit y accéder. |
| `volumes` | `./data` contient la base de données, les sauvegardes et les extensions. Conservez-le et incluez-le dans vos sauvegardes du serveur. Il est créé au premier démarrage s'il n'existe pas ; au démarrage, le conteneur en attribue la propriété à l'utilisateur sous lequel Marginalia s'exécute dans le conteneur (`jetty`), de sorte que les fichiers qu'il contient appartiennent à l'identifiant de cet utilisateur sur l'hôte. |
| `-Xmx1g` | Mémoire maximale. Augmentez-la (par exemple `-Xmx2g`) pour de nombreux utilisateurs ou de très gros livres. |

Après avoir modifié le fichier, appliquez-le avec `docker compose up -d`.

<a id="environment-variables"></a>

### Variables d'environnement

Les réglages propres à Marginalia sont des variables d'environnement du service, à côté de `JAVA_TOOL_OPTIONS` :

```yaml
    environment:
      - JAVA_TOOL_OPTIONS=-Duser.home=/var/marginalia -Xms256m -Xmx1g
      - MARGINALIA_DEFAULT_LANGUAGE=ja
      - MARGINALIA_PERSISTENT_LOGIN_TTL=604800
      - MARGINALIA_TRUSTED_PROXIES=172.16.0.0/12
```

| Variable | Valeur par défaut | Rôle |
|---|---|---|
| `MARGINALIA_DATA` | `~/.marginalia` (`/var/marginalia/.marginalia` dans l'image) | Dossier de la base de données, des sauvegardes, des extensions et de la clé qui chiffre les clés d'API. Ne le modifiez qu'avec un volume correspondant (`./data:/votre/dossier`) ; le conteneur fixe le propriétaire de ce dossier au démarrage. |
| `MARGINALIA_DEFAULT_LANGUAGE` | `en` | Langue de la page de connexion et des utilisateurs qui n'en ont pas choisi une (`en` ou `ja` ; la langue d'une extension ne peut pas être utilisée ici). |
| `MARGINALIA_ALLOW_PERSISTENT_LOGIN` | `true` | `false` masque **Rester connecté**. |
| `MARGINALIA_PERSISTENT_LOGIN_TTL` | `2592000` | Durée de validité d'une connexion mémorisée, en secondes (30 jours). |
| `MARGINALIA_TRUSTED_PROXIES` | vide | Reverse proxies dont l'en-tête `X-Forwarded-For` est pris en compte, voir [ci-dessous](#https-and-reverse-proxy). Remplace `-DtrustedProxies`. |

Les mêmes réglages peuvent être donnés sous forme d'options Java, `-Dmarginalia.default-language=ja`, qui sont prioritaires sur les
variables. Une valeur incorrecte (texte à la place d'un nombre de secondes, langue inconnue) empêche Marginalia de
démarrer ; la raison figure dans le journal.

<a id="https-and-reverse-proxy"></a>

## HTTPS et reverse proxy

!!!danger N'utilisez pas HTTP simple sur Internet
Les mots de passe et les cookies de connexion sont envoyés à chaque requête. En HTTP simple, quiconque se trouve sur le trajet peut les lire.
Placez Marginalia derrière un reverse proxy avec HTTPS.
!!!

L'option **Rester connecté** de l'écran de connexion nécessite également HTTPS : ses cookies sont marqués comme sécurisés, de sorte que les navigateurs ne les conservent que sur les connexions HTTPS.

Marginalia met à jour la page via une connexion WebSocket ; le proxy doit donc transmettre les demandes de mise à niveau WebSocket. Exemples
pour `marginalia.example.com` :

+++ Caddy
Caddy obtient le certificat et transmet automatiquement les WebSockets.

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

    client_max_body_size 100m;   # envois de sauvegardes et d'extensions

    location / {
        proxy_pass http://127.0.0.1:8080;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_read_timeout 1h;   # les longues générations gardent la connexion ouverte
    }
}
```
+++

Les connexions échouées sont limitées par utilisateur et par adresse client (après 30 échecs depuis une même adresse, les connexions depuis cette adresse sont refusées pendant une durée croissante, jusqu'à 15 minutes). Marginalia ne fait confiance à l'en-tête `X-Forwarded-For` que d'un proxy que vous désignez : sans cela, tous les utilisateurs semblent provenir de l'adresse du proxy, et 30 échecs de la part de n'importe qui bloquent tout le monde pendant un moment. Conservez l'en-tête dans la configuration du proxy (comme ci-dessus) et définissez `MARGINALIA_TRUSTED_PROXIES=<adresse ou plage du proxy>`
dans la section `environment` de `docker-compose.yml`. Un proxy situé sur l'hôte Docker atteint le conteneur depuis le réseau Docker, normalement `172.16.0.0/12` ; plusieurs entrées sont séparées par des virgules. La limitation est conservée en mémoire : redémarrer Marginalia la réinitialise.

<a id="models-on-the-same-machine"></a>

## Modèles sur la même machine

À l'intérieur du conteneur, `localhost` désigne le conteneur lui-même, et non le serveur. Pour utiliser un serveur de modèles qui tourne sur l'hôte Docker (llama.cpp, Ollama, LM Studio...), utilisez l'adresse `http://host.docker.internal:<port>/v1` dans le
[fournisseur d'inférence](../inference-providers.md). Sous Linux, ajoutez ceci au service dans `docker-compose.yml` :

```yaml
    extra_hosts:
      - "host.docker.internal:host-gateway"
```

Le serveur de modèles doit aussi écouter sur une adresse que le conteneur peut atteindre, et pas seulement sur `127.0.0.1`.

<a id="updating"></a>

## Mise à jour

```sh
git pull
docker compose up -d --build
```

Avec l'[image précompilée](#prebuilt-image), utilisez plutôt `docker compose pull` puis `docker compose up -d`.

La base de données est mise à niveau automatiquement au démarrage de la nouvelle version. Marginalia enregistre une
[copie de la base de données](../administration/database-backups.md#copy-before-an-upgrade) avant de modifier sa structure,
mais effectuez d'abord votre propre [sauvegarde de la base de données](../administration/database-backups.md).

<a id="automatic-updates"></a>

### Mises à jour automatiques

`rebuild.sh` (Linux, macOS) et `rebuild.bat` (Windows), à la racine du dépôt, effectuent la mise à jour à votre place ; lancez-les à la main ou depuis une tâche cron ou une tâche planifiée. Ils récupèrent le dépôt distant de la branche sur laquelle se trouve la copie locale (quelle qu'elle soit) et ne font rien s'il n'y a rien de nouveau (`--force` reconstruit et redémarre quand même, par exemple après une modification de `docker-compose.yml`). Sinon, ils exécutent `git pull`, construisent les [extensions fournies](#plugins) et l'image **pendant que Marginalia continue de tourner**, puis arrêtent le conteneur, copient les JAR des extensions dans `data/extensions` (en remplaçant ceux déjà installés) et démarrent la nouvelle version. Si une étape échoue, le script s'arrête en affichant un message ; si c'est le pull ou une construction, l'ancienne version tourne toujours, et la prochaine exécution reconstruit à nouveau.

`--stop-first` arrête le conteneur avant la construction, pour un serveur qui n'a pas assez de mémoire pour l'application et la construction en même temps ; Marginalia est alors indisponible pendant toute la construction.

Sous Linux, les scripts n'ont même pas besoin d'être lancés à la main : avec le surveillant de mises à jour installé, un administrateur met à jour le serveur d'un clic sur un bouton, voir [Mises à jour](../administration/updates.md).

<a id="plugins"></a>

## Extensions

L'image Docker ne contient pas les [extensions](../extensions/index.md), qui doivent être construites à partir des mêmes sources que l'application. Vous n'avez besoin ni de Java ni de Maven pour cela : `marginalia/plugins/docker-compose.yml` les construit toutes dans un conteneur, qui construit d'abord l'application puis chaque extension, et copie les JAR dans
`marginalia/plugins/build` :

```sh
docker compose -f marginalia/plugins/docker-compose.yml build
docker compose -f marginalia/plugins/docker-compose.yml run --rm plugins
```

La première construction prend plusieurs minutes (elle télécharge les dépendances de l'application). Copiez ensuite les JAR dans
`data/extensions` et redémarrez Marginalia, ou chargez-les dans *Admin → Extensions* (voir
[Extensions](../administration/extensions.md)). `rebuild.sh` / `rebuild.bat` effectuent la copie à votre place : sur un serveur qui les utilise, les extensions sont donc toujours mises à jour en même temps que l'application.

<a id="health"></a>

## Santé

L'image dispose d'une vérification d'état Docker : `docker ps` affiche `(health: starting)` pendant le démarrage de Marginalia (jusqu'à deux minutes), puis `(healthy)`. Elle passe à `unhealthy` lorsque `http://localhost:8080/health` cesse de répondre avec `200`. Docker ne redémarre pas de lui-même un conteneur en état `unhealthy` ; utilisez cet état pour votre supervision ou un outil de redémarrage automatique.

`/health` ne demande aucune connexion, un outil de supervision ou un répartiteur de charge peut donc l'utiliser aussi (via le proxy :
`https://marginalia.example.com/health`). Il répond `200` avec `{"status":"UP"}` lorsque Marginalia tourne et que sa base de données répond, et `503` avec `{"status":"DOWN",...}` pendant le démarrage ou en cas de problème. Il n'affiche rien d'autre (ni version, ni détails) ; la raison figure dans le journal.

<a id="logs"></a>

## Journaux

```sh
docker compose logs -f marginalia
```
