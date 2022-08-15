# Continuous Integration with Google Cloud Build
We're currently in the situation that migrating LSML to Java 9+ is difficult (#714), we rely on JavaFX which is bundled with Oracle Java 8 but not with OpenJDK 8. We could use OpenJFX with OpenJDK but OpenJFX requires Java 11 or later which by the above, is hard. For this reason we're stuck with Oracle JDK 8 for building for now.

To integrate with GCB we need a builder image that has Oracle Java 8 so that we can successfully build. To do this we need to create our own builder image based on https://github.com/oracle/docker-images/tree/main/OracleJava.

## Building the Builder

Install Docker