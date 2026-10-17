#!/bin/bash
# refresh-plugins.sh   Replaces the plugin JARs in the data folder by the ones that were built now (marginalia/plugins/*/target/<name>-<version>.jar).
#   The step snapshots hold the JARs of the day the step was made; plugin pictures must show the plugins of this commit.
#   The application must be stopped (restore.sh <step> --no-start). The .valid reports and logs of the old JARs are deleted,
#   the application verifies the new ones when it starts. shoot-all.sh calls it after every restore.
. "$(dirname "$0")/env.sh"
ext="$SHOT_HOME/.marginalia/extensions"
version=$(sed -n 's:.*<version>\(.*\)</version>.*:\1:p' "$REPO_ROOT/pom.xml" | head -1)
for old in "$ext"/*.jar; do
  [ -f "$old" ] || continue
  base=$(basename "$old" .jar); name="${base%-*}"
  new="$REPO_ROOT/marginalia/plugins/$name/target/$name-$version.jar"
  [ -f "$new" ] || { echo "no built JAR for $name ($new), kept $base"; continue; }
  rm -f "$ext/$base.jar" "$ext/$base.valid" "$ext/$base.jar.log" "$ext/$base.STOPPED"
  cp "$new" "$ext/$name-$version.jar"
done
echo "plugins refreshed to $version"
