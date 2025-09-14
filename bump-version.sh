#!/bin/bash

# Check if a new version is provided
if [ -z "$1" ]; then
  echo "Usage: $0 <new-version>"
  exit 1
fi

NEW_VERSION=$1

# Get the current version from pom.xml
OLD_VERSION=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)

echo "Bumping version from $OLD_VERSION to $NEW_VERSION"

# Update the version in pom.xml
mvn versions:set -DnewVersion=$NEW_VERSION -DprocessAllModules=true

# Update the image version in the Kubernetes deployment
sed -i.bak "s/nontster\/spring-leader:$OLD_VERSION/nontster\/spring-leader:$NEW_VERSION/g" kubernetes/deployment.yaml
sed -i.bak "s/nontster\/spring-leader:$OLD_VERSION/nontster\/spring-leader:$NEW_VERSION/g" src/test/resources/deployment.yaml

echo "Successfully updated pom.xml, kubernetes/deployment.yaml and src/test/resources/deployment.yaml"
echo "Backups of the deployment files have been created: kubernetes/deployment.yaml.bak and src/test/resources/deployment.yaml.bak"
