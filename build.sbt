lazy val root = (project in file(".")).
  settings(
    commonSettings,
    compilerOptions,
    consoleSettings,
    testSettings,
    publishSettings
  )

lazy val commonSettings = Seq(
  organization := "org.systemfw",
  name := "eidos",
  scalaVersion := "2.11.8",
  crossScalaVersions := Seq("2.11.8", "2.12.1")
)

lazy val compilerOptions =
  scalacOptions ++= Seq(
    "-unchecked",
    "-deprecation",
    "-encoding", "utf8"
  )

lazy val consoleSettings =
  initialCommands := s"import eidos._"

lazy val testSettings = {
  val specs2 = Seq(
    "specs2-core",
    "specs2-matcher-extra",
    "specs2-scalacheck"
  ).map("org.specs2" %% _ % "3.8.8")

  val scalacheck = "org.scalacheck" %% "scalacheck" % "1.13.4"

  Seq(
    libraryDependencies ++= (specs2 :+ scalacheck).map(_ % "test"),
    testFrameworks := Seq(TestFrameworks.Specs2)
  )
}

import ReleaseTransformations._

lazy val publishSettings = {
  val username = "SystemFw"
  val repo = "eidos"

  Seq(
    homepage := Some(url(s"https://github.com/$username/$repo")),
    licenses += "MIT" -> url("http://opensource.org/licenses/MIT"),
    scmInfo := Some(
      ScmInfo(
        url(s"https://github.com/$username/$repo"),
        s"git@github.com:$username/$repo.git")
    ),
    publishMavenStyle := true,
    publishArtifact in Test := false,
    publishTo := Some(
      if (isSnapshot.value)
        Opts.resolver.sonatypeSnapshots
      else Opts.resolver.sonatypeStaging),
    pomExtra := (
      <developers>
        <developer>
         <id>{username}</id>
         <name>Fabio Labella</name>
         <url>http://github.com/{username}</url>
        </developer>
      </developers>
    ),
    releaseCrossBuild := true,
    releasePublishArtifactsAction := PgpKeys.publishSigned.value,
    releaseProcess := Seq[ReleaseStep](
    checkSnapshotDependencies,
    inquireVersions,
    runClean,
    releaseStepCommand("package"),
    setReleaseVersion,
    commitReleaseVersion,
    tagRelease,
    releaseStepCommand("publishSigned"),
    setNextVersion,
    commitNextVersion,
    releaseStepCommand("sonatypeReleaseAll"), 
    pushChanges)
  )
}
