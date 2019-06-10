package com.example.pingpong.client

import cats.effect.{Effect, _}
import cats.syntax.either._
import cats.syntax.functor._
import com.example.pingpong.protocol._
import fs2.Stream
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import pureconfig.{ConfigReader, Derivation}
import pureconfig.generic.auto._

class ClientProgram[F[_] : ConcurrentEffect : ContextShift : Timer] {

  def runProgram(args: List[String]): Stream[F, ExitCode] = for {
    config <- Stream.eval(serviceConfig[PingPongClientConfig])
    logger <- Stream.eval(Slf4jLogger.fromName[F](config.name))
    exitCode <- clientProgram(config)(logger)
  } yield exitCode

  def clientProgram(config: PingPongClientConfig)(implicit L: Logger[F]): Stream[F, ExitCode] = for {
    serviceApi <- PingPongServiceApi.createInstance(config.host, config.port, sslEnabled = false)
    response <- Stream.eval(serviceApi.pingPong(Ping("Ping!")))
    _ <- Stream.eval(Logger[F].info(s"The response is: \${response.value}"))
  } yield ExitCode.Success

  def serviceConfig[Config](implicit reader: Derivation[ConfigReader[Config]]): F[Config] =
    Effect[F].fromEither(
      pureconfig.loadConfig[Config].leftMap(e => new IllegalStateException(s"Error loading configuration: \$e")))

}

object ClientApp extends IOApp {
  def run(args: List[String]): IO[ExitCode] =
    new ClientProgram[IO]
      .runProgram(args)
      .compile
      .toList
      .map(_.headOption.getOrElse(ExitCode.Error))
}
