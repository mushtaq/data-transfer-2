package tmt.integration.bridge

import akka.actor.ActorSystem
import akka.stream.scaladsl.{Keep, Sink, Source}
import akka.stream.{Materializer, OverflowStrategy}
import org.reactivestreams.Publisher
import tmt.server.BoundedForwarder

object Connector {
  def coupling[T](sink: Sink[T, Publisher[T]])(implicit mat: Materializer, system: ActorSystem) = {
    val (actorRef, publisher) = Source.actorRef[T](2, OverflowStrategy.dropHead).toMat(sink)(Keep.both).run()
    val forwarder = system.actorOf(BoundedForwarder.props(actorRef))
    (forwarder, Source(publisher))
  }
}
