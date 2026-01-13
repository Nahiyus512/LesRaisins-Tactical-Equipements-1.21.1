plugins { 
    alias(libs.plugins.moddev) 
} 
 
val mod_id = "lrtactical" 
version = "1.0.0" 
group = "me.xjqsh.lrtactical" 
 
repositories { 
    mavenLocal() 
    mavenCentral() 
    maven("https://jitpack.io") { 
        content { 
            includeGroup("com.github.rtyley") 
            includeGroup("com.github.FiguraMC.luaj") 
        } 
    } 
    maven("https://maven.shedaniel.me") 
    maven("https://maven.kosmx.dev") 
    maven("https://maven.blamejared.com") 
    maven("https://maven.architectury.dev") { 
        content { 
            includeGroup("dev.architectury") 
        } 
    } 
    maven("https://maven.latvian.dev/releases") { 
        content { 
            includeGroup("dev.latvian.mods") 
            includeGroup("dev.latvian.apps") 
        } 
    } 
    exclusiveContent { 
        forRepository { 
            maven("https://api.modrinth.com/maven") { 
                name = "Modrinth" 
            } 
        } 
        filter { 
            includeGroup("maven.modrinth") 
        } 
    } 
    exclusiveContent { 
        forRepository { 
            maven("https://cursemaven.com") { 
                name = "CurseForge" 
            } 
        } 
        filter { 
            includeGroup("curse.maven") 
        } 
    } 
    flatDir { 
        dir("libs") 
    } 
} 
 
neoForge { 
    version = libs.versions.neoforge.get() 
    parchment { 
        mappingsVersion = libs.versions.parchment.get() 
        minecraftVersion = libs.versions.minecraft.get() 
    } 
    validateAccessTransformers = true 
 
    // Access Transformer 
    // accessTransformers.add(file("src/main/resources/META-INF/accesstransformer.cfg")) 
 
    runs { 
        configureEach { 
            systemProperty("forge.logging.console.level", "debug") 
             
            // Add runtime dependencies for runs 
            dependencies { 
                // additionalRuntimeClasspathConfiguration(libs.org.apache.commons.math3) 
                // additionalRuntimeClasspathConfiguration(libs.luaj.core) 
                // additionalRuntimeClasspathConfiguration(libs.luaj.jse) { 
                //    exclude(group = "org.apache.bcel", module = "bcel") 
                // } 
                // additionalRuntimeClasspathConfiguration(libs.org.apache.bcel) 
            } 
        } 
 
        create("client") { 
            client() 
            gameDirectory = file("run/client") 
            systemProperty("neoforge.enabledGameTestNamespaces", mod_id) 
        } 
        create("server") { 
            server() 
            gameDirectory = file("run/server") 
            systemProperty("neoforge.enabledGameTestNamespaces", mod_id) 
        } 
        create("data") { 
            data() 
            programArguments.addAll( 
                "--mod", mod_id, 
                "--all", 
                "--output", file("src/generated/resources/").absolutePath, 
                "--existing", file("src/main/resources/").absolutePath 
            ) 
        } 
    } 
 
    mods { 
        create(mod_id) { 
            sourceSet(sourceSets["main"]) 
        } 
    } 
} 
 
sourceSets["main"].resources.srcDir("src/generated/resources") 
 
dependencies { 
    // TACZ (Placeholder or local) 
    // implementation(libs.tacz) 
    // Unofficial TaCZ 1.21.1 Port 
    implementation("curse.maven:tacz-1-21-1-1353462:7374584") 
 
    // implementation(libs.org.apache.commons.math3) 
    // jarJar(libs.org.apache.commons.math3) 
    compileOnly(libs.org.apache.commons.math3) 
     
    // implementation(libs.luaj.core) 
    // jarJar(libs.luaj.core) 
    compileOnly(libs.luaj.core) 
     
    // implementation(libs.luaj.jse) 
    // jarJar(libs.luaj.jse) 
    compileOnly(libs.luaj.jse) 
     
    // implementation(libs.org.apache.bcel) 
    // jarJar(libs.org.apache.bcel) 
    compileOnly(libs.org.apache.bcel) 
 
    compileOnly(libs.cloth.config) 
    compileOnly(libs.player.animation.lib) 
     
    // implementation(libs.maven.modrinth.sodium) 
    // implementation(libs.maven.modrinth.iris) 
     
    // compileOnly(libs.maven.modrinth.carry.on) 
    // compileOnly(libs.maven.modrinth.shoulder.surfing.reloaded) 
     
    compileOnly(libs.jei.common.api) 
    compileOnly(libs.jei.neoforge.api) 
    runtimeOnly(libs.jei.neoforge) 
     
    // compileOnly(libs.curse.maven.framework) 
    // compileOnly(libs.curse.maven.controllable) 
     
    // implementation(libs.dev.latvian.mods.kubejs.neoforge) 
    // compileOnly(libs.dev.latvian.mods.rhino) 
} 
 
java { 
    toolchain.languageVersion = JavaLanguageVersion.of(21) 
} 
 
tasks.withType<JavaCompile> { 
    options.encoding = "UTF-8" 
    options.release.set(21) 
}