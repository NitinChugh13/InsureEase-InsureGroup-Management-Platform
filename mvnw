#!/bin/sh
set -e

if [ -z "$JAVA_HOME" ] && [ -d "/Library/Java/JavaVirtualMachines/jdk-25.jdk/Contents/Home" ]; then
  export JAVA_HOME="/Library/Java/JavaVirtualMachines/jdk-25.jdk/Contents/Home"
  export PATH="$JAVA_HOME/bin:$PATH"
fi

SCRIPT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
MAVEN_VERSION="3.9.9"
MAVEN_HOME_DIR="$SCRIPT_DIR/.mvn/dist"

if [ ! -x "$MAVEN_HOME_DIR/apache-maven-$MAVEN_VERSION/bin/mvn" ]; then
  mkdir -p "$MAVEN_HOME_DIR"
  echo "Downloading Apache Maven $MAVEN_VERSION..."
  curl -fsSL "https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/$MAVEN_VERSION/apache-maven-$MAVEN_VERSION-bin.tar.gz" \
    | tar -xz -C "$MAVEN_HOME_DIR"
fi

exec "$MAVEN_HOME_DIR/apache-maven-$MAVEN_VERSION/bin/mvn" "$@"
