lazy val clients = Seq(client)
lazy val scalaV = "2.11.7"

lazy val commonSettings = Seq(
  scalaVersion := scalaV,
  transitiveClassifiers in Global := Seq(Artifact.SourceClassifier),
  updateOptions := updateOptions.value.withCachedResolution(true),
  libraryDependencies += "me.chrons" %%% "boopickle" % "1.1.0",
  libraryDependencies += "com.softwaremill.macwire" %% "macros" % "1.0.7"
)

lazy val backend = project.in(file("backend"))
  .settings(commonSettings: _*)
  .settings(
    fork := true,
    libraryDependencies ++= Dependencies.jvmLibs
  )
  .dependsOn(sharedJvm)


lazy val frontend = (project in file("frontend"))
  .enablePlugins(PlayScala)
  .dependsOn(sharedJvm)
  .aggregate(clients.map(Project.projectToRef): _*)
  .settings(commonSettings: _*)
  .settings(
    scalaJSProjects := clients,
    pipelineStages := Seq(scalaJSProd, gzip),
    libraryDependencies ++= Seq(
      "com.vmunier" %% "play-scalajs-scripts" % "0.3.0",
      "org.webjars" % "jquery" % "1.11.1"
    )
  )

lazy val client = project.in(file("client"))
  .enablePlugins(ScalaJSPlugin, ScalaJSPlay)
  .dependsOn(sharedJs)
  .settings(commonSettings: _*)
  .settings(
    persistLauncher := true,
    persistLauncher in Test := false,
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "0.8.0",
      "org.monifu" %%% "monifu" % "1.0-M11"
    )
  )

lazy val shared = crossProject.crossType(CrossType.Pure)
  .in(file("shared"))
  .jsConfigure(_ enablePlugins ScalaJSPlay)
  .settings(commonSettings: _*)

lazy val sharedJvm = shared.jvm
lazy val sharedJs = shared.js

// loads the Play project at sbt startup
onLoad in Global := (Command.process("project frontend", _: State)) compose (onLoad in Global).value