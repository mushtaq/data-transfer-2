package tmt.integration.bridge

import akka.actor.Status.{Failure, Success}
import akka.stream.scaladsl.Sink
import akka.stream.{Materializer, OverflowStrategy}
import tmt.integration.camera.Listener

class StreamListener[T](implicit mat: Materializer) extends Listener[T] {
  val (actorRef, source) = Connector.coupling[T](Sink.publisher)
  override def onEvent(event: T) = actorRef ! event
  override def onError(ex: Throwable) = actorRef ! Failure(ex)
  override def onComplete() = actorRef ! Success("done")
}
