lazy val root = (project in file(".")).
  settings(
    commonSettings,
    compilerOptions,
    consoleSettings,
    tests
  )

lazy val commonSettings = Seq(
  organization := "org.systemfw",
  name := "eidos",
  scalaVersion := "2.11.8"
)

lazy val compilerOptions =
  scalacOptions ++= Seq(
    "-unchecked",
    "-deprecation",
    "-encoding", "utf8"
  )

lazy val consoleSettings =
  initialCommands := s"import eidos._"

lazy val tests = {
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
