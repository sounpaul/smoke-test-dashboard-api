export SPRING_PROFILES_ACTIVE=local
./gradlew clean build -x test -Dorg.gradle.java.home=$JAVA_HOME
"$JAVA_HOME\bin\java" -Dspring.config.additional-location=optional:file:./src/main/resources/ -jar build/libs/smoke-test-dashboard-api.jar