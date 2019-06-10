import ProjectPlugin._

lazy val client = project
  .in(file("client"))
  .settings(moduleName := "ping-pong-client")
  .settings(clientSettings)

lazy val root = project
  .in(file("."))
  .settings(name := "ping-pong")
  .settings(noPublishSettings)
  .aggregate(client)
  .dependsOn(client)

addCommandAlias("runClient", "client/runMain com.example.pingpong.client.ClientApp")