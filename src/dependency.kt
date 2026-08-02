class Dependency(val notation: String)

class DependencyHandler{
    // スコープ名と紐づく依存関係のリスト
    val configurations = mutableMapOf<String, MutableList<Dependency>>()

    private fun addDependency(configurationName: String, notation: String){
        val list = configurations.getOrPut(configurationName) { mutableListOf() }
        list.add(Dependency(notation))
        println("  [Dependency] Added '$notation' to '$configurationName'")
    }

    fun implementation(notation: String){
        addDependency("implementation", notation)
    }

    fun testImplementation(notation: String) {
        addDependency("testImplementation", notation)
    }

    fun testRuntimeOnly(notation: String) {
        addDependency("testRuntimeOnly", notation)
    }
}