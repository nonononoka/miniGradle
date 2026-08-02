class PluginProvider(val pluginId: String)

class KotlinPlugins{
    val jvm = PluginProvider("org.jetbrains.kotlin.jvm")
}

class PluginsCatalog{
    val kotlin = KotlinPlugins()
}

class Libs {
    val plugins = PluginsCatalog()
}

val lib = Libs()