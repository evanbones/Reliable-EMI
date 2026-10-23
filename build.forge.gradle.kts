plugins {
    id("net.neoforged.moddev.legacyforge")
    id("me.modmuss50.mod-publish-plugin")
    id("maven-publish")
}

val minecraft = stonecutter.current.version
val loader = "forge"

fun prop(name: String) = project.property(name) as String

version = prop("mod.version")
group = prop("mod.group")
base.archivesName = "${prop("mod.id")}-$loader-$minecraft"

val overlay = rootProject.file("src/main/overlays/$loader")
sourceSets.main { resources.srcDir(overlay) }

val refmap = "${prop("mod.id")}.refmap.json"
val mixinConfigs = listOf("${prop("mod.id")}.mixins.json", "${prop("mod.id")}.forge.mixins.json")

repositories {
    mavenCentral()
    exclusiveContent {
        forRepository { maven("https://api.modrinth.com/maven") { name = "Modrinth" } }
        filter { includeGroup("maven.modrinth") }
    }
    maven("https://maven.terraformersmc.com/") { name = "TerraformersMC" }
}

mixin {
    add(sourceSets["main"], refmap)
    mixinConfigs.forEach(::config)
}

legacyForge {
    version = "$minecraft-${prop("deps.forge")}"
    accessTransformers.from(overlay.resolve("META-INF/accesstransformer.cfg").absolutePath)

    parchment {
        minecraftVersion = minecraft
        mappingsVersion = prop("deps.parchment")
    }

    runs {
        configureEach {
            ideName = "Forge ${name.replaceFirstChar(Char::uppercase)} ($minecraft)"
            gameDirectory = file("run/$name")
        }
        register("client") {
            client()
        }
        register("server") {
            server()
        }
    }

    mods {
        register(prop("mod.id")) {
            sourceSet(sourceSets["main"])
        }
    }
}

dependencies {
    annotationProcessor("org.spongepowered:mixin:0.8.5:processor")

    // MixinExtras
    compileOnly("io.github.llamalad7:mixinextras-common:${prop("deps.mixinextras")}")
    annotationProcessor("io.github.llamalad7:mixinextras-common:${prop("deps.mixinextras")}")
    implementation("io.github.llamalad7:mixinextras-forge:${prop("deps.mixinextras")}")
    "jarJar"("io.github.llamalad7:mixinextras-forge:${prop("deps.mixinextras")}")

    // EMI
    modImplementation("dev.emi:emi-forge:${prop("deps.emi")}")

    // YACL
    modImplementation("maven.modrinth:yacl:${prop("deps.yacl")}-forge")

    // Mekanism
    modCompileOnly("maven.modrinth:mekanism:${prop("deps.mekanism")}")
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
            "minecraft_version" to minecraft,
            "minecraft_version_range" to prop("mod.mc_dep_forgelike"),
            "forge_version" to prop("deps.forge"),
            "forge_loader_version_range" to prop("deps.forge_loader_version_range"),
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

        filesMatching(listOf("pack.mcmeta", "META-INF/mods.toml", "*.mixins.json")) {
            expand(props)
        }

        filesMatching(mixinConfigs) {
            filter { line -> line.replace("\"package\":", "\"refmap\": \"$refmap\",\n  \"package\":") }
        }

        exclude("assets/c/stack_groups/**")
    }

    named("createMinecraftArtifacts") {
        dependsOn("stonecutterGenerate")
    }

    jar {
        finalizedBy("reobfJar")

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
            "Built-On-Minecraft" to minecraft,
            "MixinConfigs" to mixinConfigs.joinToString(","),
        )
    }

    register<Copy>("buildAndCollect") {
        group = "build"
        from(named<Jar>("reobfJar").map { it.archiveFile })
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
    file = tasks.named<Jar>("reobfJar").flatMap { it.archiveFile }
    changelog = provider { rootProject.file("CHANGELOG-LATEST.md").readText() }
    type = STABLE
    version = "${project.version}-$minecraft-$loader"
    displayName = "${prop("mod.name")} Forge $minecraft - ${project.version}"
    modLoaders.add(loader)

    curseforge {
        accessToken = providers.environmentVariable("CURSEFORGE_TOKEN")
        projectId = prop("publish.curseforge")
        minecraftVersions.add(minecraft)
        client = true
        server = false

        requires("emi")
        requires("yacl")
    }

    modrinth {
        accessToken = providers.environmentVariable("MODRINTH_TOKEN")
        projectId = prop("publish.modrinth")
        minecraftVersions.add(minecraft)

        requires("emi")
        requires("yacl")
    }
}
