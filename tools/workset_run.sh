#!/bin/sh
# Runs the dataset harness (WorksetBuilder) with the plugins' compiled classes on the classpath (no OSGi, see marginalia/src/test/.../workset/README.md).
# Usage: tools/workset_run.sh [mvn -D options...]     e.g.  tools/workset_run.sh -Dworkset.parts=5 -Dworkset.author=claude -Dworkset.lengthFactor=0.9
# The plugins must be built first (mvn install -DskipTests in marginalia/ and libs/translator-common, then mvn package in each plugin).
set -e
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
M="$ROOT/marginalia"
CP="$M/plugins/conlang-translator/target/classes,$M/plugins/reallang-translator/target/classes,$M/plugins/knowledge-tracker/target/classes,$M/plugins/arc-outline/target/classes,$M/plugins/audiobook/target/classes,$M/libs/http-hooks/target/classes,$M/libs/translator-common/target/classes"
cd "$M"
exec mvn test -Dbuild.dir="${BUILD_DIR:-/tmp/marginalia-build}" -Dtest=WorksetBuilder -Dworkset.build=true \
    -Dworkset.steps="${WORKSET_STEPS:-generate,annotate,redraft,knowledge,translate,blockend}" -Dmaven.test.additionalClasspath="$CP" "$@"
