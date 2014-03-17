
import sbt._
import sbt.Keys._

/**
 * @author cgatay
 */
object QueryDSLPlugin extends Plugin {
  val QueryDSL = config("querydsl").hide

  val queryDSLVersion = SettingKey[String]("querydsl-version", "QueryDSL version.")
  val queryDSLPackage = SettingKey[String]("querydsl-package", "QueryDSL package to scan.")

  private def compileModels(
                             classpath: Classpath,
                             compilers: Compiler.Compilers,
                             javaSourceDirectory: File,
                             generatedSourcesDirectory: File,
                             packageToScan : String,
                             streams: TaskStreams
                             ) = {

    val cached = FileFunction.cached(streams.cacheDirectory / "querydsl", FilesInfo.lastModified, FilesInfo.exists) {
      (in: Set[File]) => {
        //we don't use input as we need full classpath to scan for annotations
        try {
          val outputDirectory: File = generatedSourcesDirectory / "querydsl"
          outputDirectory.mkdirs()
          compilers.javac((javaSourceDirectory ** "*.java").get.toSeq,
            classpath.map(_.data),
            outputDirectory,
            Seq("-proc:only", "-processor", "com.mysema.query.apt.jpa.JPAAnnotationProcessor", "-s", outputDirectory.getAbsolutePath))(streams.log)
        } catch {
          case c: sbt.compiler.CompileFailed => streams.log.info("Compilation failed to complete, it might be because of cross dependencies")
        }
        (generatedSourcesDirectory ** "Q*.java").get.toSet
      }
    }
    cached((javaSourceDirectory / packageToScan ** "*.java").get.toSet)
  }

  val QueryDSLTemplates = (state: State,
                           dependencyClassPath: Classpath,
                           pluginClassPath: Classpath,
                           javaSourceDirectory: File,
                           classesDirectory: File,
                           generatedDir: File,
                           compilers: Compiler.Compilers,
                           packageToScan : String,
                           streams: TaskStreams) => {
    compileModels(dependencyClassPath ++ pluginClassPath, compilers, javaSourceDirectory, generatedDir, packageToScan, streams)
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
    queryDSLPackage := "models",
    managedClasspath in QueryDSL <<= (classpathTypes, update) map {
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
      compilers in Compile,
      queryDSLPackage,
      streams
      ) map QueryDSLTemplates
  )
}
