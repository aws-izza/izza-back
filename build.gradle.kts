plugins {
    java
    id("org.springframework.boot") version "3.5.4"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.asciidoctor.jvm.convert") version "3.3.2"
    id("org.sonarqube") version "5.0.0.4638"
}

group = "com"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

val springCloudAwsVersion = "3.4.0"


configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

extra["snippetsDir"] = file("build/generated-snippets")

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    compileOnly("org.projectlombok:lombok")

    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    runtimeOnly("org.postgresql:postgresql")


    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.9")

    // AWS
    implementation(platform("io.awspring.cloud:spring-cloud-aws-dependencies:${springCloudAwsVersion}"))
    implementation("io.awspring.cloud:spring-cloud-aws-starter-secrets-manager")

    // Explicit AWS SDK dependencies for direct API calls
    implementation("software.amazon.awssdk:secretsmanager")
    implementation("software.amazon.awssdk:sts")

    // Utils
    implementation("org.locationtech.jts:jts-core:1.20.0")
    implementation("net.logstash.logback:logstash-logback-encoder:8.1")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    annotationProcessor("org.projectlombok:lombok")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.springframework.restdocs:spring-restdocs-mockmvc")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.test {
    outputs.dir(project.extra["snippetsDir"]!!)
}

tasks.asciidoctor {
    inputs.dir(project.extra["snippetsDir"]!!)
    dependsOn(tasks.test)
}

sonarqube {
    properties {
        property("sonar.projectKey", "izza-back")
        property("sonar.projectName", "izza-back")
        property("sonar.host.url", "http://sonarqube-service.sonarqube.svc.cluster.local:9000")
        property("sonar.sourceEncoding", "UTF-8")
        property("sonar.token", System.getenv("SONAR_TOKEN"))
        property("sonar.java.binaries", "build/classes/java/main")
        property("sonar.sources", "src/main")
    }
}