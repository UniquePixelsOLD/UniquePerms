plugins {
  `java-library`
  id("io.papermc.paperweight.userdev") version "1.5.11"
  id("xyz.jpenilla.run-paper") version "2.2.2" // Adds runServer and runMojangMappedServer tasks for testing
  id("com.github.johnrengelman.shadow") version ("8.1.1")
}

group = "net.uniquepixels"
version = "1.0.0"
description = "Permission plugin for UniquePixels"

java {
  toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

repositories {
  maven("https://repo.uniquepixels.net/repository/minecraft") {
    credentials {
      username = "projectwizard"
      password = System.getenv("UP_NEXUS_PASSWORD")
    }
  }
  mavenCentral()
}

dependencies {
  paperweight.paperDevBundle("1.20.4-R0.1-SNAPSHOT")

  compileOnly("net.uniquepixels:core:latest")
  compileOnly("net.uniquepixels:core-api:latest")

  implementation("org.mongodb:mongodb-driver-sync:4.10.1")

  implementation("dev.s7a:base64-itemstack:1.0.0")
}

tasks {
  // Configure reobfJar to run when invoking the build task
  assemble {
    dependsOn(reobfJar)
  }
  shadowJar {
    dependencies {
      include(dependency("org.mongodb:mongodb-driver-sync:4.10.1"))
      include(dependency("org.mongodb:mongodb-driver-core:4.10.1"))
      include(dependency("org.mongodb:bson:4.10.1"))
      exclude(dependency("net.uniquepixels:core:latest"))
      exclude(dependency("net.uniquepixels:core-api:latest"))
    }
  }

  compileJava {
    options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything

    // Set the release flag. This configures what version bytecode the compiler will emit, as well as what JDK APIs are usable.
    // See https://openjdk.java.net/jeps/247 for more information.
    options.release.set(17)
  }
  javadoc {
    options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything
  }
  processResources {
    filteringCharset = Charsets.UTF_8.name() // We want UTF-8 for everything
    val props = mapOf(
      "name" to project.name,
      "version" to project.version,
      "description" to project.description,
      "apiVersion" to "1.20"
    )
    inputs.properties(props)
    filesMatching("plugin.yml") {
      expand(props)
    }
  }

  reobfJar {
    // This is an example of how you might change the output location for reobfJar. It's recommended not to do this
    // for a variety of reasons, however it's asked frequently enough that an example of how to do it is included here.
    outputJar.set(layout.buildDirectory.file("dist/UniquePerms-${project.version}.jar"))
  }
}
