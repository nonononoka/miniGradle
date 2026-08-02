class ProjectDescriptor(var name: String)

class Task(val name: String, val project: Project, val action: () -> Unit) {
    val depends = mutableSetOf<Task>()

    fun dependsOn(vararg tasks: Task) {
        depends.addAll(tasks)
    }

    fun execute(executedTasks: MutableList<Task>) {
        if (executedTasks.contains(this)) return

        for (task in depends) {
            task.execute(executedTasks)
        }
        action.invoke()
        executedTasks.add(this)
    }
}

class Project(val name: String, val settings: Settings) {
    val tasks = mutableMapOf<String, Task>()

    fun plugins(block: PluginContainer.()-> Unit){
        val container = PluginContainer(this)
        container.block()
    }

    fun registerTask(name: String, action: () -> Unit): Task {
        val task = tasks.getOrPut(name) { Task(name, this, action) }
        return task
    }

    fun project(path: String): Project {
        val cleanPath = path.removePrefix(":")
        return settings.projects[cleanPath] ?: throw IllegalArgumentException("Project '\$path' not found")
    }
}

class Settings {
    val rootProject = ProjectDescriptor("default-root-project")
    val projects = mutableMapOf<String, Project>()
    var isBuildCacheEnabled = false
    var buildCacheDirectory: String? = null

    fun plugins(block: SettingsPluginContainer.()->Unit){
        val container = SettingsPluginContainer(this)
        container.block()
    }

    fun include(vararg projectNames: String) {
        for (projectName in projectNames) {
            val projectName = projectName.removePrefix(":")
            projects[projectName] = Project(projectName, this)
        }
    }

    fun project(name: String, block: Project.() -> Unit) {
        val p = projects[name] ?: throw IllegalStateException("Project $name not found")
        block(p)
    }
}

class GradleRunner {
    private lateinit var settings: Settings

    // 1. settings.gradle.ktsに相当するスクリプトを省略する
    fun initialize(settingsScript: Settings.() -> Unit) {
        println("=== [Phase 1] Initialization ===")
        settings = Settings()
        settings.settingsScript()
    }

    // 2. build.gradle.ktsに相当するスクリプトを評価する
    fun configure(buildScript: Map<String, Project.() -> Unit>) {
        println("\n=== [Phase 2] Configuration ===")
        for ((projectName, project) in settings.projects) {
            val script = buildScript[projectName]
            if (script == null) {
                println("  (No build script found for :$projectName)")
            } else {
                project.script()
            }
        }
    }

    // 3. execution
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

fun main() {
    val settingKts: Settings.() -> Unit = {
        rootProject.name = "my-kotlin-project"
        plugins{
            id("build-cache")
        }
        include(":core", ":app")
    }

    val coreBuildKts: Project.() -> Unit = {
        // これでJava pluginのtaskをprojectに登録
        plugins{
            id("java")
            alias(lib.plugins.kotlin.jvm)
            application
        }

        val coreTask = registerTask("core_task") {
            println("    > [Action] core:core_task is running!")
        }

        coreTask.dependsOn(tasks["build"]!!)
    }

    val appBuildKts: Project.() -> Unit = {
        val task = registerTask("first_task") {
            println("    > [Action] app:first_task is running!")
        }
        task.dependsOn(project(":core").tasks["core_task"]!!)
    }

    val runner = GradleRunner()
    runner.initialize {
        settingKts()
        println("  Root Project Name: ${this.rootProject.name}")
        println("  Included projects: ${this.projects.keys.joinToString()}")
    }

    val scripts = mapOf(
        "core" to coreBuildKts,
        "app" to appBuildKts
    )
    runner.configure(scripts)

    runner.execute("app", "first_task")
}