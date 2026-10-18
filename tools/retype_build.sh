#!/bin/sh
# Builds the manual (docs/) with the version of the project and the commit it was built from in the label next to
# the title and in the footer. Run from anywhere; passes extra arguments to `retype build`.
set -e
cd "$(dirname "$0")/.."
version=$(sed -n '/<artifactId>marginalia-parent<\/artifactId>/,/<\/version>/s/.*<version>\(.*\)<\/version>.*/\1/p' pom.xml | head -1)
[ -n "$version" ] || version=$(mvn -q -N help:evaluate -Dexpression=project.version -DforceStdout)
commit=$(git rev-parse --short HEAD)
retype build --override "{\"branding\":{\"label\":\"Manual $version\"},\"footer\":{\"copyright\":\"&copy; Copyright 2026. MIT License. Version $version ($commit).\"}}" "$@"
