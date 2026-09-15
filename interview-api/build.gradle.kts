plugins {
    java
    id("org.springframework.boot") version "3.5.16"
}

group = "org.example"
version = "0.1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform("org.springframework.boot:spring-boot-dependencies:3.5.16"))
    implementation("org.springframework.boot:spring-boot-starter-web")

    testImplementation(platform("org.springframework.boot:spring-boot-dependencies:3.5.16"))
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
