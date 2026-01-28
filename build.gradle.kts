plugins {
    `maven-publish`
    id("hytale-mod") version "0.+"
    id("io.github.goooler.shadow") version "8.1.8"
}

group = "de.offi"
version = "1.1.1"
val javaVersion = 25

repositories {
    mavenCentral()
    maven("https://www.cursemaven.com")
}

dependencies {
    compileOnly(libs.jetbrains.annotations)
    compileOnly(libs.jspecify)

    // External - provided at runtime
    compileOnly("net.luckperms:api:5.5")

    // Bundle kyori with jar (only text-minimessage)
    compileOnly("net.kyori:adventure-api:4.26.1")
    implementation("net.kyori:adventure-text-minimessage:4.26.1")
    compileOnly("net.kyori:adventure-text-serializer-legacy:4.26.1")
    implementation("org.yaml:snakeyaml:2.2")
}
hytale {
    // uncomment if you want to add the Assets.zip file to your external libraries;
    // ⚠️ CAUTION, this file is very big and might make your IDE unresponsive for some time!
    //
    // addAssetsDependency = true
    // uncomment if you want to develop your mod against the pre-release version of the game.
    //
    // updateChannel = "pre-release"
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(javaVersion)
    }
    withSourcesJar()
}
tasks.shadowJar {
    archiveClassifier.set("")

    // Include only kyori and snakeyaml
    dependencies {
        include(dependency("net.kyori:adventure-api"))
        include(dependency("net.kyori:adventure-text-minimessage"))
        include(dependency("net.kyori:adventure-text-serializer-legacy"))
        include(dependency("org.yaml:snakeyaml"))
    }

    // Exclude everything we don't need
    exclude("com/hypixel/**")
    exclude("com/google/**")
    exclude("com/nimbusds/**")
    exclude("com/github/**")
    exclude("google/protobuf/**")
    exclude("io/netty/**")
    exclude("io/sentry/**")
    exclude("it/unimi/**")
    exclude("org/bouncycastle/**")
    exclude("org/bson/**")
    exclude("org/checkerframework/**")
    exclude("org/fusesource/**")
    exclude("org/jline/**")
    exclude("ch/**")
    exclude("javax/**")
    exclude("joptsimple/**")
    exclude("META-INF/native/**")
    exclude("META-INF/maven/**")
    exclude("**/**.so")
    exclude("**/**.dll")
    exclude("**/**.dylib")
    exclude("**/**.jnilib")

    manifest {
        attributes["Specification-Title"] = rootProject.name
        attributes["Specification-Version"] = version
        attributes["Implementation-Title"] = project.name
        attributes["Implementation-Version"] =
            providers.environmentVariable("COMMIT_SHA_SHORT")
                .map { "${version}-${it}" }
                .getOrElse(version.toString())
    }

    destinationDirectory.set(File(System.getProperty("user.home"), "Documents/code-repo/Hytale/hytale_server/Server/mods"))
}

// shadowJar statt jar als Standard
tasks.build {
    dependsOn(tasks.shadowJar)
}
tasks.named<ProcessResources>("processResources") {
    var replaceProperties = mapOf(
        "plugin_group" to findProperty("plugin_group"),
        "plugin_maven_group" to project.group,
        "plugin_name" to project.name,
        "plugin_version" to project.version,
        "server_version" to findProperty("server_version"),
        "plugin_description" to findProperty("plugin_description"),
        "plugin_website" to findProperty("plugin_website"),
        "plugin_main_entrypoint" to findProperty("plugin_main_entrypoint"),
        "plugin_author" to findProperty("plugin_author")
    )
    filesMatching("manifest.json") {
        expand(replaceProperties)
    }
    inputs.properties(replaceProperties)
}
publishing {
    repositories {
        // This is where you put repositories that you want to publish to.
        // Do NOT put repositories for your dependencies here.
    }

    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}

// IDEA no longer automatically downloads sources/javadoc jars for dependencies, so we need to explicitly enable the behavior.
idea {
    module {
        isDownloadSources = true
        isDownloadJavadoc = true
    }
}
val syncAssets = tasks.register<Copy>("syncAssets") {
    group = "hytale"
    description = "Automatically syncs assets from Build back to Source after server stops."
    from(layout.buildDirectory.dir("resources/main"))
    into("src/main/resources")
    exclude("manifest.json")
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    doLast {
        println("✅ Assets successfully synced from Game to Source Code!")
    }
}

afterEvaluate {
    val targetTask = tasks.findByName("runServer") ?: tasks.findByName("server")
    if (targetTask != null) {
        targetTask.finalizedBy(syncAssets)
        logger.lifecycle("✅ specific task '${targetTask.name}' hooked for auto-sync.")
    } else {
        logger.warn("⚠️ Could not find 'runServer' or 'server' task to hook auto-sync into.")
    }
}