interface Plugin<T>{
    fun apply(target: T)
}

class JavaPlugin: Plugin<Project>{
    override fun apply(target :Project){
        val compileTask = target.registerTask("compileJava"){
            println("> [Action] ${target.name}:compileJava is compiling source code...")
        }
        val buildTask = target.registerTask("build"){
            println("    > [Action] ${target.name}:build finished!")
        }
        buildTask.dependsOn(compileTask)
    }
}

class KotlinJVMPlugin: Plugin<Project>{
    override fun apply(target :Project){
        println("  [Plugin Action] KotlinJvmPlugin: ${target.name} に Kotlin のサポートを追加します")

        val compileKotlinTask = target.registerTask("compileKotlin") {
            println("    > [Action] ${target.name}:compileKotlin is compiling Kotlin code...")
        }
        val buildTask = target.tasks["build"]
        if (buildTask != null) {
            buildTask.dependsOn(compileKotlinTask)
        }
    }
}

class ApplicationPlugin: Plugin<Project>{
    override fun apply(target :Project){
        println("  [Plugin Action] ApplicationPlugin: ${target.name} に CLIアプリケーションのサポートを追加します")

        target.registerTask("run"){
            println("    > [Action] ${target.name}:run is executing the application...")
        }
    }
}

class BuildCachePlugin: Plugin<Settings>{
    override fun apply(target :Settings) {
        target.isBuildCacheEnabled = true
        target.buildCacheDirectory = "/tmp/minigradle-cache"
        println("  [Settings] BuildCachePlugin: ビルドキャッシュを有効化しました (Dir: ${target.buildCacheDirectory})")
    }
}

object PluginRegistry{
    private val projectPlugins = mapOf<String, Plugin<Project>>(
        "java" to JavaPlugin(),
        "org.jetbrains.kotlin.jvm" to KotlinJVMPlugin(),
        "application" to ApplicationPlugin(),
    )

    private val settingsPlugins = mapOf<String, Plugin<Settings>>(
        "build-cache" to BuildCachePlugin()
    )

    fun getProjectPlugin(id: String): Plugin<Project> {
        return projectPlugins[id] ?: throw IllegalArgumentException("Plugin '$id' not found")
    }

    fun getSettingsPlugin(id: String): Plugin<Settings> {
        return settingsPlugins[id] ?: throw IllegalArgumentException("Plugin '$id' not found")
    }
}

class PluginContainer(private val target: Project){
    fun id(pluginId: String){
        println("  [Plugin] Applying plugin: $pluginId to ${target.name}")
        val plugin = PluginRegistry.getProjectPlugin(pluginId)
        plugin.apply(target)
    }

    fun alias(provider: PluginProvider){
        // providerからIDを取り出して結局はid()を呼ぶ
        id(provider.pluginId)
    }

    val application: Unit
        get(){
            id("application")
        }
}

class SettingsPluginContainer(private val target: Settings){
    fun id(pluginId: String){
        val plugin = PluginRegistry.getSettingsPlugin(pluginId)
        plugin.apply(target)
    }
}