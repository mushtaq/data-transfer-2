package tmt.integration.bridge

import akka.actor.ActorSystem
import akka.stream.Materializer
import tmt.integration.camera.Simulator

object SourceSimulator {
  def apply[T](producer: () => Iterator[T])(implicit materializer: Materializer, system: ActorSystem) = {
    val simulator = new Simulator(producer())
    val listener = new StreamListener[T]
    simulator.subscribe(listener)
    listener.source
  }
}
