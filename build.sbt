
organization := "com.adagility"
name := "docker-it-scala"
version := "0.2.2"
scalaVersion := "2.11.7"
crossScalaVersions := Seq("2.11.7", "2.10.5")
scalariformSettings
fork in Test := true
val gitHeadCommitSha = settingKey[String]("current git commit SHA")
gitHeadCommitSha in ThisBuild := Process("git rev-parse --short HEAD").lines.head

lazy val root = project.in(file(".")).aggregate(core, scalatest, specs2)

lazy val core =
  project.settings(
    resolvers += "softprops-maven" at "http://dl.bintray.com/content/softprops/maven",
    libraryDependencies ++=
      Seq(
        "com.github.docker-java" % "docker-java" % "1.4.0",
        "me.lessis" %% "odelay-core" % "0.1.0",
        "me.lessis" %% "undelay" % "0.1.0",
        "org.specs2" %% "specs2-core" % "3.6.4"))

lazy val scalatest =
  project
    .settings(
    libraryDependencies ++=
      Seq(
        "org.scalatest" %% "scalatest" % "2.2.4",
        "ch.qos.logback" % "logback-classic" % "1.1.2" % "test"))
    .dependsOn(core)

lazy val specs2 =
  project
    .settings(
    libraryDependencies ++= Seq(
      "org.specs2" %% "specs2-core" % "3.6.4",
      "ch.qos.logback" % "logback-classic" % "1.1.2" % "test"))
    .dependsOn(core)
