class Repository(val name: String, val url: String)

class RepositoryHandler{
    val list = mutableListOf<Repository>()

    fun mavenCentral(){
        val url = "https://repo.maven.apache.org/maven2/"
        println("  [Repository] Maven Central が登録されました ($url)")
        list.add(Repository("MavenCentral", url))
    }
}