package tmt.server

import akka.actor.{ActorRef, Actor, Props}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{Publish, Subscribe, Unsubscribe}
import akka.dispatch.{RequiresMessageQueue, BoundedMessageQueueSemantics}
import akka.stream.scaladsl.{Sink, Source}
import tmt.app.ActorConfigs
import tmt.integration.bridge.Connector

class Publisher(actorConfigs: ActorConfigs) {
  import actorConfigs._

  private val mediator = DistributedPubSub(system).mediator
  val forwarder = system.actorOf(BoundedForwarder.props(mediator))

  def publish(role: Role, xs: Source[Any, Any]) = xs.runForeach { x =>
    if(role != Role.MetricsCumulative) {
      println(s"publishing: $role: $x")
    }
    forwarder ! Publish(role.entryName, x)
  }
}

class Subscriber[T](actorConfigs: ActorConfigs) {
  import actorConfigs._

  private val mediator = DistributedPubSub(system).mediator

  private val (actorRef, _source) =  Connector.coupling(Sink.fanoutPublisher[T](2, 2))

  def source = _source
  def subscribe(role: Role) = mediator ! Subscribe(role.entryName, actorRef)
  def unsubscribe(role: Role) = mediator ! Unsubscribe(role.entryName, actorRef)
}

class BoundedForwarder(ref: ActorRef) extends Actor with RequiresMessageQueue[BoundedMessageQueueSemantics] {
  def receive = {
    case x => ref forward  x
  }
}

object BoundedForwarder {
  def props(ref: ActorRef) = Props(new BoundedForwarder(ref))
}
