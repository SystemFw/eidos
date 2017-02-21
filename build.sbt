lazy val root = (project in file(".")).
  settings(
    commonSettings,
    consoleSettings,
    compilerOptions,
    typeSystemEnhancements,
    dependencies,
    tests
  )

lazy val commonSettings = Seq(
  organization := "org.systemfw",
  name := "eidos",
  scalaVersion := "2.11.8"
)

val consoleSettings =
  initialCommands := s"import eidos._"

lazy val compilerOptions = {
  val options = Seq(
    "-unchecked",
    "-deprecation",
    "-target:jvm-1.8",
    "-feature",
    "-language:implicitConversions",
    "-language:higherKinds"
  )

  scalacOptions ++= options
}


lazy val typeSystemEnhancements = Seq(
  resolvers += Resolver.sonatypeRepo("releases"),
  addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.3"),
  addCompilerPlugin("com.milessabin" % "si2712fix-plugin" % "1.2.0" cross CrossVersion.full)
)


def dep(org: String)(version: String)(modules: String*) =
    Seq(modules:_*) map { name =>
      org %% name % version
    }


lazy val dependencies = {
  val scalaz = dep("org.scalaz")("7.2.8")(
    "scalaz-core"// ,
    // "scalaz-concurrent",
    // "scalaz-effect"
  )

  val mixed = Seq()

  libraryDependencies ++= scalaz ++ mixed
}

lazy val tests = {
  val dependencies = {
    val specs2 = dep("org.specs2")("3.8.8")(
      "specs2-core",
      "specs2-matcher-extra",
      "specs2-scalaz",
      "specs2-scalacheck"
    )

    val mixed = Seq(
      "org.scalacheck" %% "scalacheck" % "1.13.4"
    )

    libraryDependencies ++= (specs2 ++ mixed).map(_ % "test")
  }

  val frameworks =
    testFrameworks := Seq(TestFrameworks.Specs2)

  val unitFilter =
    testOptions in Test += Tests.Filter(name => name endsWith "Spec")

  Seq(dependencies, frameworks, unitFilter)
}
