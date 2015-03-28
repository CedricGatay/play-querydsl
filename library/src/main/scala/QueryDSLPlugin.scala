package codetroopers

import sbt.Keys._
import sbt._
import sbt.plugins.JvmPlugin

/**
 * @author cgatay
 */
object QueryDSLPlugin extends AutoPlugin {
  

  val QueryDSL = config("querydsl").hide
  
  object autoImport {
    val queryDSLVersion = SettingKey[String]("querydsl-version", "QueryDSL version.")
    val queryDSLPackage = SettingKey[String]("querydsl-package", "QueryDSL package to scan.")
  }

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
        try {
          val outputDirectory: File = generatedSourcesDirectory / "querydsl"
          outputDirectory.mkdirs()
          streams.log("QueryDSLPlugin").debug("Going to process the following files for annotation scanning : " 
                                              + in.map(_.getPath).mkString(","))
          compilers.javac(in.toSeq,
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
  
  import autoImport._

  override def projectSettings: Seq[Def.Setting[_]] =  Seq[Def.Setting[_]](
    queryDSLVersion := "3.6.2",
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

  override def projectConfigurations: Seq[Configuration] = Seq(QueryDSL)

  override def requires = JvmPlugin
}
