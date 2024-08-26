plugins {
    id("java")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {




    // https://mvnrepository.com/artifact/com.google.guava/guava
    implementation("com.google.guava:guava:11.0.2")

    // https://mvnrepository.com/artifact/org.jsoup/jsoup
    implementation("org.jsoup:jsoup:1.7.2")

    // https://mvnrepository.com/artifact/commons-io/commons-io
    implementation("commons-io:commons-io:2.6")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}