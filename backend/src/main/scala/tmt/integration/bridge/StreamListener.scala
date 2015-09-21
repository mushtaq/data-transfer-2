package tmt.integration.bridge

import akka.actor.ActorSystem
import akka.actor.Status.{Failure, Success}
import akka.stream.Materializer
import akka.stream.scaladsl.Sink
import tmt.integration.camera.Listener

class StreamListener[T](implicit mat: Materializer, system: ActorSystem) extends Listener[T] {
  val (actorRef, source) = Connector.coupling[T](Sink.publisher)
  override def onEvent(event: T) = actorRef ! event
  override def onError(ex: Throwable) = actorRef ! Failure(ex)
  override def onComplete() = actorRef ! Success("done")
}
