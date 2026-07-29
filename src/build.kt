import kotlin.text.get

fun Project.configureCoreBuild() {
    registerTask("common_task"){
        println("common registered")
    }
}

fun Project.configureAppBuild(projectCore: Project) {
    val task = registerTask("first_task"){
        println("task registered")
    }
    task.dependsOn(projectCore.tasks["common_task"]!!)
}