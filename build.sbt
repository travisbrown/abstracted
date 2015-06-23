lazy val sharedSettings = Seq(
  organization := "io.travisbrown",
  scalaVersion := "2.11.6",
  crossScalaVersions := Seq("2.10.5", "2.11.6"),
  libraryDependencies ++= Seq(
    "org.scala-lang" % "scala-compiler" % scalaVersion.value,
    "org.scalatest" %% "scalatest" % "2.2.5" % "test"
  ),
  libraryDependencies ++= (
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, 10)) => Seq(
        compilerPlugin(
          "org.scalamacros" % "paradise" % "2.0.1" cross CrossVersion.full
        ),
        "org.scalamacros" %% "quasiquotes" % "2.0.1"
      )
      case _ => Nil
    }
  ),
  ScoverageSbtPlugin.ScoverageKeys.coverageHighlighting := (
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, 10)) => false
      case _ => true
    }
  )
)

lazy val root = project.in(file("."))
  .settings(sharedSettings)
  .aggregate(core)
  .dependsOn(core)

lazy val core = project
  .settings(moduleName := "abstracted")
  .settings(sharedSettings)
  .settings(
    libraryDependencies ++= Seq(
      /**
       * We use Scalaz only in our tests.
       */
      "org.scalaz" %% "scalaz-core" % "7.1.3" % "test"
    )
  )

lazy val demo = project
  .settings(moduleName := "abstracted-demo")
  .settings(sharedSettings)
  .settings(
    resolvers += Resolver.sonatypeRepo("snapshots"),
    crossScalaVersions := Seq("2.11.6")
  )
  .dependsOn(
    ProjectRef(uri("git://github.com/travisbrown/catbird.git"), "finagle"),
    core
  )
