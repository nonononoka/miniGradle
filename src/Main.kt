
//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.

class Task(val name: String, val project: Project){
    var action: (()->Unit)? = null

    fun doLast(block: ()->Unit){
        action = block
    }

    fun execute(){
        action?.invoke()
    }
}


class Project(val name: String){
    val tasks = mutableMapOf<String,Task>()

    fun registerTask(name: String, block: Task.()->Unit){
        val task = tasks.getOrPut(name, ){Task(name, this)}
        task.block()
    }
}

fun main() {
    val project = Project("app")
    project.registerTask("first_task"){
        doLast{println("task registered")}
    }

    project.tasks["first_task"]?.execute()
}