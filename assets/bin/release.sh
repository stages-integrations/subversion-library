#!/bin/bash

set -e
set -o pipefail
set -u

THIS_PATH="`readlink --canonicalize-existing "${0}"`"
THIS_NAME="`basename "${THIS_PATH}"`"
THIS_DIR="`dirname "${THIS_PATH}"`"

ROOT="`readlink --canonicalize-existing "${THIS_DIR}/../.."`"

if [ $# -ne 2 ]; then
    echo "${0} <RELEASE_VERSION> <NEXT_DEVELOPMENT_VERSION>"
    echo "    RELEASE_VERSION         : major.minor.path"
    echo "    NEXT_DEVELOPMENT_VERSION: major.minor.path (-SNAPSHOT is added automatically)"
    exit 1
fi

RELEASE_VERSION="${1}"; shift
SNAPSHOT_VERSION="${1}-SNAPSHOT"; shift

echo "releasing version ${RELEASE_VERSION}"
echo "preparing version ${SNAPSHOT_VERSION}"
echo ""
echo "any key to continue, CTRL-C to abort"
read input


# make sure you have no uncommited changes and your repository is up to date
git -C "${ROOT}" reset --hard HEAD
git -C "${ROOT}" clean --force -d
git -C "${ROOT}" pull  --rebase
git -C "${ROOT}" clean --force -d

mvn --file="${ROOT}/pom.xml" clean
mvn --file="${ROOT}/pom.xml" versions:set -DnewVersion=${RELEASE_VERSION} -DgenerateBackupPoms=false

git -C "${ROOT}" commit --all --message="prepare release v${RELEASE_VERSION}"
git -C "${ROOT}" tag    --annotate --message="v${RELEASE_VERSION}" "v${RELEASE_VERSION}" HEAD

mvn --file="${ROOT}/pom.xml" deploy

mvn --file="${ROOT}/pom.xml" clean
mvn --file="${ROOT}/pom.xml" versions:set -DnewVersion=${SNAPSHOT_VERSION} -DgenerateBackupPoms=false

git -C "${ROOT}" commit --all --message="prepare for next development iteration"

git -C "${ROOT}" push
git -C "${ROOT}" push --tags
