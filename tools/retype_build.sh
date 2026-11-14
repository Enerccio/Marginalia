#!/bin/sh
# Builds the manual (docs/, ignored by git; the Manual workflow publishes its own build) with the version of the project and the commit it was built from in the label next to
# the title and in the footer. Run from anywhere; passes extra arguments to `retype build`.
#
# The English site is docs/ and every language that has translated pages (tools/manual_i18n.py languages) is a site of its own,
# docs/<lang>, built from the English manual with the translated pages over it; each has the language links in the top bar. The in-app
# help links to these sites (ManualPage.url), so a language that has translations must have one.
set -e
cd "$(dirname "$0")/.."
version=$(sed -n '/<artifactId>marginalia-parent<\/artifactId>/,/<\/version>/s/.*<version>\(.*\)<\/version>.*/\1/p' pom.xml | head -1)
[ -n "$version" ] || version=$(mvn -q -N help:evaluate -Dexpression=project.version -DforceStdout)
commit=$(git rev-parse --short HEAD)
branding="{\"branding\":{\"label\":\"Manual $version\"},\"footer\":{\"copyright\":\"&copy; Copyright 2026. MIT License. Version $version ($commit).\"}}"

# the label and the footer, then what tools/manual_i18n.py says for the language (the links; the input, the output and the url of a language)
build() {
    lang=$1
    shift
    override=$(python3 tools/manual_i18n.py override --lang "$lang")
    merged=$(python3 -c 'import json, sys; a = json.loads(sys.argv[1]); a.update(json.loads(sys.argv[2])); print(json.dumps(a, ensure_ascii=False))' "$branding" "$override")
    retype build --override "$merged" "$@"
}

# the English site first: it empties docs/
build en "$@"
for lang in $(python3 tools/manual_i18n.py languages); do
    python3 tools/manual_i18n.py assemble --lang "$lang" --out "build/i18n/$lang/manual"
    build "$lang" "$@"
done
