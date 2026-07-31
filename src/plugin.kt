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

object PluginRegistry{
    private val projectPlugins = mapOf<String, Plugin<Project>>(
        "java" to JavaPlugin()
    )

    fun getProjectPlugin(id: String): Plugin<Project> {
        return projectPlugins[id] ?: throw IllegalArgumentException("Plugin '$id' not found")
    }
}

class PluginContainer(private val target: Project){
    fun id(pluginId: String){
        val plugin = PluginRegistry.getProjectPlugin(pluginId)
        plugin.apply(target)
    }
}