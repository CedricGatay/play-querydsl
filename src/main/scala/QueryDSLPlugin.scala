
import sbt._
import sbt.Keys._

/**
 * @author cgatay
 */
object QueryDSLPlugin extends Plugin {
  val QueryDSL = config("querydsl").hide

  val queryDSLVersion = SettingKey[String]("querydsl-version", "QueryDSL version.")

  private def compileModels(
                             classpath: Classpath,
                             compilers: Compiler.Compilers,
                             javaSourceDirectory: File,
                             classesDirectory: File,
                             streams: TaskStreams
                             ) = {


    try {
      classesDirectory.mkdirs()
      compilers.javac((javaSourceDirectory ** "*.java").get,
        classpath.map(_.data),
        classesDirectory,
        Seq("-proc:only", "-processor", "com.mysema.query.apt.jpa.JPAAnnotationProcessor", "-s", classesDirectory.getAbsolutePath))(streams.log)
    }
    catch {
      case c: sbt.compiler.CompileFailed => streams.log.info("Compilation failed to complete, it might be because of cross dependencies")
    }
  }

  val QueryDSLTemplates = (state: State,
                           dependencyClassPath: Classpath,
                           pluginClassPath: Classpath,
                           javaSourceDirectory: File,
                           classesDirectory: File,
                           generatedDir: File,
                           compilers: Compiler.Compilers,
                           streams: TaskStreams) => {
    compileModels(dependencyClassPath ++ pluginClassPath, compilers, javaSourceDirectory, generatedDir, streams)
    (generatedDir ** "Q*.java").get.map(_.getAbsoluteFile)
  }

  lazy val queryDSLSettings = Seq[Project.Setting[_]](
    queryDSLVersion := "3.3.1",
    libraryDependencies <++= (queryDSLVersion in QueryDSL)(version =>
      Seq(
        //add querydsl-apt to dependencies in QueryDSL
        "com.mysema.querydsl" % "querydsl-apt" % version % QueryDSL.name,
        //add querydsl-jpa to dependencies of the project
        "com.mysema.querydsl" % "querydsl-jpa" % version
      )
    ),

    managedClasspath in QueryDSL <<=(classpathTypes, update) map {
      (ct, report) =>
        Classpaths.managedJars(QueryDSL, ct, report)
    },

    sourceGenerators in Compile <+= (
      state,
      dependencyClasspath in Compile,
      managedClasspath in QueryDSL,
      sourceDirectory in Compile,
      classDirectory in Compile,
      sourceManaged in Compile,
      compilers in Compile, streams
      ) map QueryDSLTemplates
  )
}
