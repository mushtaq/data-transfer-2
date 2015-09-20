package tmt.io

import java.io.File
import java.nio.file.Files

import akka.http.scaladsl.model.ws.BinaryMessage
import akka.stream.io.SynchronousFileSource
import akka.stream.scaladsl.Source
import akka.util.ByteString
import tmt.app.{ActorConfigs, AppSettings}
import tmt.common._
import tmt.common.models.Image

import scala.concurrent.Future

class ImageReadService(actorConfigs: ActorConfigs, settings: AppSettings, producer: Producer) {
  private val parallelism = 1

  private def files = Source(() => producer.files(settings.framesInputDir))

  def sendBytes = files.mapAsync(parallelism)(readFile).map(ByteString.apply)
  def sendImages = files.mapAsync(parallelism)(readImage)
  def sendMessages = files.map(SynchronousFileSource(_)).map(BinaryMessage.apply)

  private def readImage(file: File) = readFile(file).map(data => Image(file.getName, data))(actorConfigs.ec)
  private def readFile(file: File) = Future {
    Thread.sleep(settings.imageReadThrottle)
    Files.readAllBytes(file.toPath)
  }(settings.fileIoDispatcher)
}
