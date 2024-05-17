./gradlew clean build -x test -Dorg.gradle.java.home=$JAVA_HOME
"$JAVA_HOME\bin\java" -jar -Dspring.profiles.active=$1 build/libs/smoke-test-dashboard-api.jar