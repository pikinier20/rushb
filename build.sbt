import sbtassembly.Log4j2MergeStrategy

name := "rushb"

version := "0.1"

ThisBuild / scalaVersion := "2.13.6"

ThisBuild / resolvers ++= Seq(
  Resolver.mavenLocal,
  Resolver.sonatypeRepo("public")
)

lazy val common = project
  .in(file("common"))
  .settings(
    libraryDependencies ++= Seq(
      "com.lihaoyi" %% "upickle" % "1.4.2",
    )
  )
  .settings(commonSettings)

lazy val downloader = project
  .in(file("downloader"))
  .settings(
    assemblyJarName in assembly := "rushb-downloader.jar"
  )
  .dependsOn(common)
  .settings(commonSettings)

lazy val crawler = project
  .in(file("crawler"))
  .settings(
    assemblyJarName in assembly := "rushb-crawler.jar",
    libraryDependencies ++= Seq(
      "io.monix" %% "monix" % "3.3.0",
      "org.jsoup" % "jsoup" % "1.13.1"
    )
  )
  .dependsOn(common)
  .settings(commonSettings)

lazy val parser = project
  .in(file("parser"))
  .settings(
    assemblyJarName in assembly := "rushb-parser.jar"
  )
  .dependsOn(common)
  .settings(
    libraryDependencies ++= Seq(
      "org.pikinier20" %% "csdemoparser" % "0.1",
      "com.amazonaws" % "aws-lambda-java-core" % "1.2.1",
      "com.amazonaws" % "aws-lambda-java-events" % "3.11.0"
    )
  )
  .settings(commonSettings)


val commonSettings = Seq(
  scalacOptions ++= Seq(
    "-unchecked",
    "-deprecation",
    "-feature",
    "-Xfatal-warnings"
  ),
  assemblyMergeStrategy in assembly := {
    case x if x.endsWith("module-info.class") => MergeStrategy.discard
    case PathList(ps @ _*) if ps.last == "Log4j2Plugins.dat" =>
      Log4j2MergeStrategy.plugincache
    case x =>
      val oldStrategy = (assemblyMergeStrategy in assembly).value
      oldStrategy(x)
  },
  libraryDependencies ++= Seq(
    "commons-io" % "commons-io" % "2.11.0",
    "com.amazonaws" % "aws-lambda-java-core" % "1.2.1",
    "com.amazonaws" % "aws-lambda-java-events" % "3.11.0",
    "com.amazonaws" % "aws-java-sdk-s3" % "1.12.119",
    "com.amazonaws" % "aws-java-sdk-sqs" % "1.12.119",
    "com.amazonaws" % "aws-lambda-java-log4j2" % "1.2.0",
    "org.apache.logging.log4j" % "log4j-api" % "2.17.1",
    "org.apache.logging.log4j" % "log4j-core" % "2.17.1"
  )
)
