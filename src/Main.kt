
//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.

class Task(val name: String, val project: Project, val action: () -> Unit){
    val depends = mutableSetOf<Task>()

    fun execute(executedTasks: MutableList<Task>) {
        if (executedTasks.contains(this)) return

        for (task in depends){
            task.execute(executedTasks)
        }
        action.invoke()
        executedTasks.add(this)
    }
}

class Project(val name: String){
    val tasks = mutableMapOf<String,Task>()

    fun registerTask(name: String, action: ()->Unit): Task{
        val task = tasks.getOrPut(name, ) { Task(name, this, action)}
        return task
    }
}

class Settings{
    val projects = mutableMapOf<String,Project>()

    fun include(vararg projectNames: String){
        for (projectName in projectNames){
            val projectName = projectName.removePrefix(":")
            projects[projectName] = Project(projectName)
        }
    }
}

fun main() {
    // settings.ktsを読んで，projetsを登録
    val settings = Settings()
    configureSettings(settings)

    // 設定
    configureCoreBuild(settings)
    configureAppBuild(settings, settings.projects["core"]!!)

    // 実行
    val executedTasks = mutableListOf<Task>()
    settings.projects["app"]!!.tasks["first_task"]?.execute(executedTasks)
}