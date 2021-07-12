package services

import com.google.inject.Provides

import play.api.libs.concurrent.ActorModule

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.PostStop
import akka.actor.typed.PreRestart

import akka.actor.typed.scaladsl.Behaviors

object ActorRefManager extends ActorModule {
  type Message = ManagerCommand

  sealed trait ManagerCommand
  case class Register(actorRef: ActorRef[String])   extends ManagerCommand
  case class UnRegister(actorRef: ActorRef[String]) extends ManagerCommand
  case class SendSignal(signal: String)             extends ManagerCommand

  @Provides
  def apply(actors: Set[ActorRef[String]]): Behavior[ManagerCommand] =
    Behaviors.setup { ct =>
      println(s"setup ${ct.self}")

      Behaviors
        .receiveMessage[ManagerCommand](msg =>
          msg match {
            case Register(actorRef)   =>
              println(s"register $actorRef")
              ActorRefManager(actors + actorRef)
            case UnRegister(actorRef) =>
              println(s"unRegister $actorRef")
              ActorRefManager(actors - actorRef)
            case SendSignal(signal)   =>
              println(s"send a signal to all ActorSources ${actors.toString}")
              actors.foreach(_ ! signal)
              Behaviors.same
          }
        )
        .receiveSignal { signal =>
          signal match {
            case (context, PreRestart) =>
              println(s"PreRestart send to ${context.self}")
              Behaviors.same
            case (context, PostStop) =>
              println(s"PostStop send to ${context.self}")
              Behaviors.same
          }
        }
    }
}
