# Continuous Integration with Google Cloud Build
We're currently in the situation that migrating LSML to Java 9+ is difficult (#714), we rely on JavaFX which is bundled with Oracle Java 8 but not with OpenJDK 8. We could use OpenJFX with OpenJDK but OpenJFX requires Java 11 or later which by the above, is hard. For this reason we're stuck with Oracle JDK 8 for building for now.

To integrate with GCB we need a builder image that has Oracle Java 8 so that we can successfully build. To do this we need to create our own builder image based on https://github.com/oracle/docker-images/tree/main/OracleJava.

## Building the Builder

Install Docker
Figure out steps to build container locally
Push container to eu.gcr.io
???
Profit

1. Install Google Cloud SDK and initialize it
1. `gcloud config set project $PROJECT_ID` (See: `gcloud projects list`).
1. Download `jdk-8u301-linux-x64.tar.gz` from Oracle and place in `lsml/gcb-ci-builder`.
1. `cd lsml/gcb-ci-builder`.
1. Find next version tag from https://console.cloud.google.com
1. Trigger building of the container on on Google Cloud Build:
```
gcloud builds submit --tag europe-west3-docker.pkg.dev/li-soft/lsml-ci/oracle-jdk8-builder:vX
```
1. Add the live tag to the newly created container.
