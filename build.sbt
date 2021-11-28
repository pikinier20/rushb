name := "rushb"

version := "0.1"

ThisBuild / scalaVersion := "2.13.6"

ThisBuild / resolvers ++= Seq(
  Resolver.mavenLocal
)

lazy val common = project
  .in(file("common"))
  .settings(
    libraryDependencies ++= Seq(
      "com.lihaoyi" %% "upickle" % "1.4.2"
    )
  )

lazy val downloader = project
  .in(file("downloader"))
  .settings(
    libraryDependencies ++= Seq(
      "commons-io" % "commons-io" % "2.11.0",
      "com.amazonaws" % "aws-lambda-java-core" % "1.2.1",
      "com.amazonaws" % "aws-lambda-java-events" % "3.11.0",
      "com.amazonaws" % "aws-java-sdk-s3" % "1.12.119",
      "com.amazonaws" % "aws-java-sdk-sqs" % "1.12.119",
      "org.apache.logging.log4j" % "log4j-api" % "2.14.1",
      "org.apache.logging.log4j" % "log4j-core" % "2.14.1"
    )
  )
  .dependsOn(common)

lazy val crawler = project
  .in(file("crawler"))
  .settings(
    libraryDependencies ++= Seq(
      "io.monix" %% "monix" % "3.3.0",
      "org.jsoup" % "jsoup" % "1.13.1"
    )
  )
  .dependsOn(common)

lazy val parser = project
  .in(file("parser"))
  .settings(
    libraryDependencies ++= Seq(
      "io.monix" %% "monix" % "3.3.0",
      "org.pikinier20" %% "csdemoparser" % "0.1",
      "commons-io" % "commons-io" % "2.11.0",
      "com.amazonaws" % "aws-lambda-java-core" % "1.2.1",
      "com.amazonaws" % "aws-lambda-java-events" % "3.11.0"
    )
  )
