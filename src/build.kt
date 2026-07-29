import kotlin.text.get

fun configureCoreBuild(settings: Settings) {
    settings.projects["core"]!!.registerTask("common_task"){
        println("common registered")
    }
}

fun configureAppBuild(settings: Settings, projectCore: Project) {
    val task = settings.projects["app"]!!.registerTask("first_task"){
        println("task registered")
    }
    task.depends.add(projectCore.tasks["common_task"]!!)
}