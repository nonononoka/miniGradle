
//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.

class Task(val name: String, val project: Project, val action: () -> Unit){
    val depends = mutableSetOf<Task>()

    fun dependsOn(vararg tasks: Task){
        depends.addAll(tasks)
    }

    fun execute(executedTasks: MutableList<Task>) {
        if (executedTasks.contains(this)) return

        for (task in depends){
            task.execute(executedTasks)
        }
        action.invoke()
        executedTasks.add(this)
    }
}

class Project(val name: String, val settings: Settings){
    val tasks = mutableMapOf<String,Task>()

    fun registerTask(name: String, action: ()->Unit): Task{
        val task = tasks.getOrPut(name, ) { Task(name, this, action)}
        return task
    }

    fun project(path: String):Project{
        val cleanPath = path.removePrefix(":")
        return settings.projects[cleanPath]?:throw IllegalArgumentException("Project '\$path' not found")
    }
}

class Settings{
    val projects = mutableMapOf<String,Project>()

    fun include(vararg projectNames: String){
        for (projectName in projectNames){
            val projectName = projectName.removePrefix(":")
            projects[projectName] = Project(projectName, this)
        }
    }

    fun project(name: String, block:Project.()->Unit){
        val p = projects[name]?:throw IllegalStateException("Project $name not found")
        block(p)
    }
}

class GradleRunner{
    private lateinit var settings: Settings

    // ① Initialization（初期化）フェーズ
    // settings.gradle.kts に相当するスクリプトを評価する
    fun initialize(settingsScript: Settings.() -> Unit) {
        println("=== [Phase 1] Initialization ===")
        settings = Settings()
        settings.settingsScript()
        println("  Included projects: ${settings.projects.keys.joinToString()}")
    }

    // ② Configuration（構成）フェーズ
    // 各プロジェクトの build.gradle.kts に相当するスクリプトを評価する
    fun configure(buildScripts: Map<String, Project.() -> Unit>) {
        println("\n=== [Phase 2] Configuration ===")
        for ((projectName, project) in settings.projects) {
            val script = buildScripts[projectName]
            if (script != null) {
                println("  Configuring project: :$projectName")
                project.script() // ここでTaskが登録され、依存関係が結ばれる
            } else {
                println("  (No build script found for :$projectName)")
            }
        }
    }

    // ③ Execution（実行）フェーズ
    // 指定されたタスクを起点に、DAG（依存グラフ）を辿って実行する
    fun execute(projectPath: String, taskName: String) {
        println("\n=== [Phase 3] Execution ===")
        val cleanPath = projectPath.removePrefix(":")
        val project = settings.projects[cleanPath] ?: throw IllegalArgumentException("Project not found")
        val task = project.tasks[taskName] ?: throw IllegalArgumentException("Task not found")

        println("  Requested task: $projectPath:$taskName")
        val executedTasks = mutableListOf<Task>()
        task.execute(executedTasks)
    }
}


fun mySettings(block: Settings.() -> Unit): Settings{
    val s = Settings()
    s.block()
    return s
}

fun main() {
    // ---------------------------------------------------------
    // 仮想のファイルシステム（実際の .kts ファイルの中身だと思ってください）
    // ---------------------------------------------------------

    // settings.gradle.kts の中身
    val settingsKts: Settings.() -> Unit = {
        include(":core", ":app")
    }

    // core/build.gradle.kts の中身
    val coreBuildKts: Project.() -> Unit = {
        registerTask("common_task") {
            println("    > [Action] core:common_task is running!")
        }
    }

    // app/build.gradle.kts の中身
    val appBuildKts: Project.() -> Unit = {
        val firstTask = registerTask("first_task") {
            println("    > [Action] app:first_task is running!")
        }
        // 🎉 引数渡しではなく、DSLの project(":core") で参照できるようになった！
        firstTask.dependsOn(project(":core").tasks["common_task"]!!)
    }


    // ---------------------------------------------------------
    // コマンド実行（例: ./gradlew :app:first_task を叩いた時の裏側の動き）
    // ---------------------------------------------------------
    val runner = GradleRunner()

    // 1. 初期化フェーズ (Settingsの評価)
    runner.initialize(settingsKts)

    // 2. 構成フェーズ (すべてのProjectのビルドスクリプトを評価)
    val scripts = mapOf(
        "core" to coreBuildKts,
        "app" to appBuildKts
    )
    runner.configure(scripts)

    // 3. 実行フェーズ (タスクの実行)
    runner.execute(":app", "first_task")
}