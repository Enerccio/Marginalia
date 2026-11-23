#!/usr/bin/env bash
# The release version is the tag: on a build of a v* tag (GITHUB_REF=refs/tags/v1.2.3) every POM of the build (the parent, the
# application, the libraries and the plugins: the modules of the root pom.xml) gets the version of the tag, 1.2.3, before anything is
# built, so the WAR, the plugin JARs, the desktop archives and the image are named and labelled by the tag. Any other build keeps the
# version of pom.xml. Run by the workflows (desktop.yml, server.yml) in every job that builds; locally: GITHUB_REF=refs/tags/v1.2.3
# tools/version_from_tag.sh (then `git checkout -- '*pom.xml'` to undo).
set -euo pipefail

ref="${GITHUB_REF:-}"
if [[ "$ref" != refs/tags/v* ]]; then
  echo "Not a release tag (${ref:-no ref}): the version stays the one of pom.xml"
  exit 0
fi
version="${ref#refs/tags/v}"
if [[ ! "$version" =~ ^[0-9]+\.[0-9]+\.[0-9]+([-.][0-9A-Za-z.-]+)?$ ]]; then
  echo "::error::The tag v$version is not a version (v<major>.<minor>.<patch>, optionally -<suffix>)"
  exit 1
fi

cd "$(dirname "$0")/.."
pom_version=$(mvn -q -N help:evaluate -Dexpression=project.version -DforceStdout)
if [ "$pom_version" = "$version" ]; then
  echo "The version of pom.xml is the tag's: $version"
  exit 0
fi
echo "::warning::pom.xml says $pom_version, the tag v$version: the build takes $version. Set <version>$version</version> in the root pom.xml (mvn versions:set) so that the next builds of master say it too."
mvn -q versions:set -DnewVersion="$version" -DgenerateBackupPoms=false -DprocessAllModules=true
echo "Version set to $version"
