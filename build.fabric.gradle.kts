plugins {
    id("dev.kikugie.loom-back-compat")
    id("me.modmuss50.mod-publish-plugin")
    id("maven-publish")
}

val minecraft = stonecutter.current.version
val mcVersion = prop("deps.minecraft")
val loader = "fabric"

fun prop(name: String) = project.property(name) as String

version = prop("mod.version")
group = prop("mod.group")
base.archivesName = "${prop("mod.id")}-$loader-$minecraft"

// The access widener differs per Minecraft version, so it lives in a version overlay
val versionOverlay = rootProject.file("src/main/overlays/$minecraft")
sourceSets.main {
    resources.srcDir(rootProject.file("src/main/overlays/$loader"))
    resources.srcDir(versionOverlay)
}

repositories {
    mavenCentral()
    exclusiveContent {
        forRepository { maven("https://maven.parchmentmc.org/") { name = "ParchmentMC" } }
        filter { includeGroup("org.parchmentmc.data") }
    }
    exclusiveContent {
        forRepository { maven("https://api.modrinth.com/maven") { name = "Modrinth" } }
        filter { includeGroup("maven.modrinth") }
    }
    maven("https://maven.terraformersmc.com/") { name = "TerraformersMC" }
    maven("https://maven.quiltmc.org/repository/release/") { name = "Quilt Maven" }
}

dependencies {
    minecraft("com.mojang:minecraft:$mcVersion")
    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-$minecraft:${prop("deps.parchment")}@zip")
    })

    modImplementation("net.fabricmc:fabric-loader:${prop("deps.fabric_loader")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${prop("deps.fabric_api")}")

    // EMI
    modImplementation("dev.emi:emi-fabric:${prop("deps.emi")}")

    // Mod Menu
    modImplementation("maven.modrinth:modmenu:${prop("deps.modmenu")}")

    // YACL
    modImplementation("maven.modrinth:yacl:${prop("deps.yacl")}-fabric")
    implementation("org.quiltmc.parsers:json:0.3.0")
    implementation("org.quiltmc.parsers:gson:0.3.0")
    include("org.quiltmc.parsers:json:0.3.0")
    include("org.quiltmc.parsers:gson:0.3.0")
}

loom {
    accessWidenerPath = versionOverlay.resolve("${prop("mod.id")}.accesswidener")

    runs.configureEach {
        ideConfigGenerated(true)
        runDir("run/$name")
    }
}

val javaVer = prop("deps.java_version").toInt()
java {
    toolchain.languageVersion = JavaLanguageVersion.of(javaVer)
    withSourcesJar()
}

tasks {
    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.release = javaVer
    }

    processResources {
        val props = mapOf(
            "version" to project.version,
            "group" to project.group,
            "minecraft_version" to prop("mod.mc_dep_fabric"),
            "fabric_version" to prop("deps.fabric_api"),
            "fabric_loader_version" to prop("deps.fabric_loader"),
            "yacl_version" to prop("deps.yacl"),
            "mod_name" to prop("mod.name"),
            "mod_author" to prop("mod.author"),
            "mod_id" to prop("mod.id"),
            "license" to prop("mod.license"),
            "description" to prop("mod.description"),
            "credits" to prop("mod.credits"),
            "java_version" to javaVer,
        )
        inputs.properties(props)

        filesMatching(listOf("pack.mcmeta", "fabric.mod.json", "*.mixins.json")) {
            expand(props)
        }

        dependsOn("stonecutterGenerate")
    }

    jar {
        from(rootProject.file("LICENSE")) {
            rename { "${it}_${prop("mod.name")}" }
        }

        manifest.attributes(
            "Specification-Title" to prop("mod.name"),
            "Specification-Vendor" to prop("mod.author"),
            "Specification-Version" to archiveVersion,
            "Implementation-Title" to loader,
            "Implementation-Version" to archiveVersion,
            "Implementation-Vendor" to prop("mod.author"),
            "Built-On-Minecraft" to mcVersion,
        )
    }

    register<Copy>("buildAndCollect") {
        group = "build"
        from(loomx.modJar.map { it.archiveFile })
        into(rootProject.layout.buildDirectory.dir("libs/${project.version}"))
        dependsOn("build")
    }
}

publishing {
    publications {
        register<MavenPublication>("mavenJava") {
            artifactId = base.archivesName.get()
            from(components["java"])
        }
    }
    repositories {
        System.getenv("local_maven_url")?.let { maven(it) }
    }
}

publishMods {
    file = loomx.modJar.flatMap { it.archiveFile }
    changelog = provider { rootProject.file("CHANGELOG-LATEST.md").readText() }
    type = STABLE
    version = "${project.version}-$minecraft-$loader"
    displayName = "${prop("mod.name")} Fabric $minecraft - ${project.version}"
    modLoaders.add(loader)

    curseforge {
        accessToken = providers.environmentVariable("CURSEFORGE_TOKEN")
        projectId = prop("publish.curseforge")
        minecraftVersions.add(mcVersion)
        client = true
        server = false

        requires("emi")
        requires("yacl")
    }

    modrinth {
        accessToken = providers.environmentVariable("MODRINTH_TOKEN")
        projectId = prop("publish.modrinth")
        minecraftVersions.add(mcVersion)

        requires("emi")
        requires("yacl")
    }
}
