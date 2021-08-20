package services

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import play.api.Logger

object ActorRefManager {
  val logger: Logger = Logger("akka.actor.ActorRefManager")

  sealed trait ManagerCommand
  case class Register(actorRef: ActorRef[String])
      extends ManagerCommand
      with CborSerializable
  case class UnRegister(actorRef: ActorRef[String])
      extends ManagerCommand
      with CborSerializable
  case class SendSignal(signal: String)
      extends ManagerCommand
      with CborSerializable

  def apply(): Behavior[ManagerCommand] = {
    def updated(actors: Set[ActorRef[String]]): Behavior[ManagerCommand] = {
      Behaviors.setup { ctx =>
        logger
          .debug(s"${ctx.self} is started")

        Behaviors
          .receiveMessage[ManagerCommand](msg =>
            msg match {
              case Register(actorRef) =>
                logger.debug(s"receive a message to register $actorRef")
                updated(actors + actorRef)
              case UnRegister(actorRef) =>
                logger.debug(s"receive a message to unRegister $actorRef")
                val updatedActors = actors - actorRef
                updatedActors.size match {
                  case 0 => {
                    logger
                      .debug(s"${ctx.self} is stopped")
                    Behaviors.stopped
                  }
                  case _ => updated(updatedActors)
                }
              case SendSignal(signal) =>
                logger.debug(
                  s"receive a message to sendSignal to all ActorRef ${actors.toString}"
                )
                actors.foreach(_ ! signal)
                Behaviors.same
            }
          )
      }
    }
    updated(Set.empty)
  }
}
