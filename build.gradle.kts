plugins {
    java
    id("org.springframework.boot") version "3.3.2"
    id("io.spring.dependency-management") version "1.1.6"
    id("org.asciidoctor.jvm.convert") version "3.3.2"
}

group = "org.cubewhy"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

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
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0")
    implementation("commons-io:commons-io:2.16.1")
    implementation("cn.hutool:hutool-crypto:5.8.31")
    implementation("com.auth0:java-jwt:4.4.0")
    implementation("commons-io:commons-io:2.16.1")
    implementation("com.google.firebase:firebase-admin:9.3.0")
    implementation("com.alibaba.fastjson2:fastjson2:2.0.52")
    implementation("com.alibaba.fastjson2:fastjson2-extension-spring6:2.0.52")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-websocket")
//    implementation("org.springframework.kafka:spring-kafka")
    implementation("org.springframework.session:spring-session-data-redis")
    implementation("org.springframework.session:spring-session-jdbc")
    implementation("org.thymeleaf.extras:thymeleaf-extras-springsecurity6")
    implementation("org.jetbrains:annotations:24.1.0")
    compileOnly("org.projectlombok:lombok")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    runtimeOnly("com.h2database:h2")
    annotationProcessor("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
//    testImplementation("org.springframework.kafka:spring-kafka-test")
    testImplementation("org.springframework.restdocs:spring-restdocs-mockmvc")
    testImplementation("org.springframework.security:spring-security-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.bootJar {
    archiveFileName.set("app.jar")
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
