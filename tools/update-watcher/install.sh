#!/bin/bash

# Installs the watcher of Admin -> Update (watch.sh in this folder) as systemd units of this checkout:
#   <name>.service  runs watch.sh once
#   <name>.path     starts it when the application asks for an update (the file data/update/request appears)
#   <name>.timer    starts it after the boot and every 15 minutes, to look for new commits
#
# Usage: sudo tools/update-watcher/install.sh [--name NAME] [--user USER] [--uninstall]
# --name NAME   the name of the units (default marginalia-update); give each checkout on the computer its own
# --user USER   run the watcher as this user instead of root. The user must be allowed to use Docker and to write
#               into data/update and data/extensions.
# --uninstall   stops and removes the units
#
# See manual/user/administration/updates.md.

NAME="marginalia-update"
RUN_AS=""
UNINSTALL=0
while [ $# -gt 0 ]; do
    case "$1" in
        --name) NAME="$2"; shift 2 ;;
        --user) RUN_AS="$2"; shift 2 ;;
        --uninstall) UNINSTALL=1; shift ;;
        *)
            echo "Usage: $0 [--name NAME] [--user USER] [--uninstall]"
            exit 1
            ;;
    esac
done

if [ -z "$NAME" ] || [[ "$NAME" =~ [^A-Za-z0-9_.-] ]]; then
    echo "The name may have only letters, digits, '.', '_' and '-'."
    exit 1
fi
if [ "$(id -u)" != "0" ]; then
    echo "Run it as root: sudo $0"
    exit 1
fi
if ! command -v systemctl >/dev/null 2>&1; then
    echo "systemd was not found. Run tools/update-watcher/watch.sh from cron instead, see the manual."
    exit 1
fi

UNITS=/etc/systemd/system

if [ "$UNINSTALL" = "1" ]; then
    systemctl disable --now "$NAME.path" "$NAME.timer" 2>/dev/null
    rm -f "$UNITS/$NAME.service" "$UNITS/$NAME.path" "$UNITS/$NAME.timer"
    systemctl daemon-reload
    echo "Removed $NAME."
    exit 0
fi

REPO="$(cd "$(dirname "$0")/../.." && pwd)" || exit 1
if [[ "$REPO" =~ [[:space:]] ]]; then
    echo "The path of the checkout ($REPO) has a space in it, which the units cannot take."
    exit 1
fi
if [ ! -x "$REPO/rebuild.sh" ] || [ ! -x "$REPO/tools/update-watcher/watch.sh" ]; then
    echo "rebuild.sh and tools/update-watcher/watch.sh must be executable (chmod +x)."
    exit 1
fi

USER_LINE=""
if [ -n "$RUN_AS" ]; then
    id "$RUN_AS" >/dev/null 2>&1 || { echo "There is no user $RUN_AS."; exit 1; }
    USER_LINE="User=$RUN_AS"
fi

cat > "$UNITS/$NAME.service" <<EOF
[Unit]
Description=Marginalia update watcher ($REPO)
After=docker.service network-online.target
Wants=network-online.target

[Service]
Type=oneshot
ExecStart=$REPO/tools/update-watcher/watch.sh
$USER_LINE
EOF

cat > "$UNITS/$NAME.path" <<EOF
[Unit]
Description=Marginalia update request ($REPO)

[Path]
PathExists=$REPO/data/update/request
Unit=$NAME.service

[Install]
WantedBy=multi-user.target
EOF

cat > "$UNITS/$NAME.timer" <<EOF
[Unit]
Description=Marginalia update check ($REPO)

[Timer]
OnBootSec=2min
OnUnitInactiveSec=15min
Unit=$NAME.service

[Install]
WantedBy=timers.target
EOF

systemctl daemon-reload
systemctl enable --now "$NAME.path" "$NAME.timer" || exit 1
# the first look at the remote, so that Admin -> Update shows the status at once
systemctl start "$NAME.service"

echo "Installed $NAME for $REPO."
echo "Status: systemctl status $NAME.service    Log: journalctl -u $NAME.service"
