#!/bin/bash
#
# Usage: build-JXX.sh [option(s)] [goal(s)]
# Example: build-JXX.sh clean install
#
# Profile Id: none - default, install common jars to local repository.
# Profile Id: assembly - assemble (zip) explorer, samples projects.
# Profile Id: tests - package test plus default projects.
# Profile Id: all - package the above.
# Profile Id: sonatype-oss-release - upload default artifacts to central repository.
# Profile Id: disable-java8-doclint - ignore conformance with 'W3C HTML 4.01 HTML' for JavaDocs.
#
# How to build and test:
#
#   1) build-JXX.sh -DskipTests=true clean install
#   2) build-JXX.sh -DskipTests=true -Pall clean package
#   3) build-JXX.sh -DskipTests=false -Pall test
#   Notes:
#     Step #1 installs the shared libraries to your local maven repository.
#     Step #2 packages the shared, test and sample projects.
#     Step #3 unit test the shared, test and sample projects.
#

BASEDIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
source ${BASEDIR}/build-INC.sh

if [ $# -eq 0 ]; then
  ${BASEDIR}/build.sh
else
  source ${BASEDIR}/build-CFG.sh
  mvn ${JVM_SYS_PROPS} "$@"
fi

# ./build-JXX.sh -DskipTests=true clean install
# ./build-JXX.sh -DskipTests=true -Pnexus-deploy clean deploy
# ./build-JXX.sh -DskipTests=true -DdryRun=false release:clean
# ./build-JXX.sh -DskipTests=true -DdryRun=false release:prepare
# ./build-JXX.sh -DskipTests=true -DdryRun=false release:perform
