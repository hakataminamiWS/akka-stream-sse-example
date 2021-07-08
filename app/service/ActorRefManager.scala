package service

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import play.api.libs.concurrent.ActorModule
import com.google.inject.Provides

class ActorRefManager private (actors: Set[ActorRef[String]])

object ActorRefManager extends ActorModule {
  type Message = ManagerCommand

  sealed trait ManagerCommand
  case class SendSignal(signal: String)             extends ManagerCommand
  case class Register(actorRef: ActorRef[String])   extends ManagerCommand
  case class UnRegister(actorRef: ActorRef[String]) extends ManagerCommand

  @Provides
  def apply(actors: Set[ActorRef[String]]): Behavior[ManagerCommand] =
    Behaviors.receiveMessage(msg =>
      msg match {
        case Register(actorRef)   =>
          println(s"register $actorRef")
          ActorRefManager(actors + actorRef)
        case UnRegister(actorRef) =>
          println(s"unRegister $actorRef")
          ActorRefManager(actors - actorRef)
        case SendSignal(signal)   =>
          println(s"send a signal to all actors ${actors.toString}")
          actors.foreach(_ ! signal)
          Behaviors.same
      }
    )
}
