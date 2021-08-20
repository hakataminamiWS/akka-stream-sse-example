package services

import play.api.libs.concurrent.ActorModule

import com.google.inject.Provides

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior

import akka.actor.typed.scaladsl.Behaviors

import play.api.Logger
import akka.actor.typed.receptionist.Receptionist
import akka.actor.typed.receptionist.ServiceKey

object ReceptionistManager extends ActorModule {
  type Message = ReceptionistManagerCommand

  val logger: Logger = Logger("akka.actor.ReceptionistManager")

  sealed trait ReceptionistManagerCommand
  case class RegisterSourceActorRef(
      sourceActorRef: ActorRef[String]
  ) extends ReceptionistManagerCommand
      with CborSerializable
  private case class ResponseForRegister(
      sourceActorRef: ActorRef[String],
      listing: Receptionist.Listing
  ) extends ReceptionistManagerCommand
      with CborSerializable

  case class UnRegisterSourceActorRef(
      sourceActorRef: ActorRef[String]
  ) extends ReceptionistManagerCommand
      with CborSerializable
  private case class ResponseForUnRegister(
      sourceActorRef: ActorRef[String],
      listing: Receptionist.Listing
  ) extends ReceptionistManagerCommand
      with CborSerializable

  case class SendSignal(
      signal: String
  ) extends ReceptionistManagerCommand
      with CborSerializable
  private case class ResponseForSendSignal(
      signal: String,
      listing: Receptionist.Listing
  ) extends ReceptionistManagerCommand
      with CborSerializable

  val myServiceKey = ServiceKey[ActorRefManager.ManagerCommand]("test")

  @Provides
  def apply(): Behavior[ReceptionistManagerCommand] = {
    Behaviors
      .setup[ReceptionistManagerCommand] { context =>
        def adapterForRegister(
            sourceActorRef: ActorRef[String]
        ): ActorRef[Receptionist.Listing] = {
          context.messageAdapter[Receptionist.Listing](listings =>
            ResponseForRegister(sourceActorRef, listings)
          )
        }

        def adapterForUnRegister(
            sourceActorRef: ActorRef[String]
        ): ActorRef[Receptionist.Listing] = {
          context.messageAdapter[Receptionist.Listing](listings =>
            ResponseForUnRegister(sourceActorRef, listings)
          )
        }

        def adapterForSendSignal(
            signal: String
        ): ActorRef[Receptionist.Listing] = {
          context.messageAdapter[Receptionist.Listing](listings =>
            ResponseForSendSignal(signal, listings)
          )
        }

        Behaviors.receiveMessagePartial {
          case RegisterSourceActorRef(sourceActorRef) => {
            logger
              .debug(s"Register: ${sourceActorRef}")
            context.system.receptionist ! Receptionist.Find(
              myServiceKey,
              adapterForRegister(sourceActorRef)
            )
            Behaviors.same
          }
          case ResponseForRegister(sourceActorRef, listings) => {
            myServiceKey.Listing
              .unapply(listings)
              .map(_.find(ref => !(ref.path.toString contains ("@"))))
              .flatten match {
              case Some(localActorRefManager) => {
                logger
                  .debug(
                    s"Response: Register ${sourceActorRef} in ${localActorRefManager}"
                  )
                localActorRefManager ! ActorRefManager.Register(sourceActorRef)
              }
              case None => {
                logger
                  .debug(
                    s"Response: Spawn Router and Register ${sourceActorRef}"
                  )
                val actorRefManager =
                  context.spawnAnonymous(ActorRefManager.apply())
                context.system.receptionist ! Receptionist.Register(
                  myServiceKey,
                  actorRefManager
                )
                actorRefManager ! ActorRefManager.Register(sourceActorRef)

              }
            }
            Behaviors.same
          }

          case UnRegisterSourceActorRef(sourceActorRef) => {
            logger
              .debug(s"UnRegister: ${sourceActorRef}")
            context.system.receptionist ! Receptionist.Find(
              myServiceKey,
              adapterForUnRegister(sourceActorRef)
            )
            Behaviors.same
          }
          case ResponseForUnRegister(sourceActorRef, listings) => {
            myServiceKey.Listing
              .unapply(listings) match {
              case Some(set) => {
                set.foreach(actorRefManager => {
                  logger
                    .debug(
                      s"Response: UnRegister ${sourceActorRef} in ${actorRefManager}"
                    )
                  actorRefManager ! ActorRefManager.UnRegister(
                    sourceActorRef
                  )
                })
              }
              case None =>
            }
            Behaviors.same
          }

          case SendSignal(signal) => {
            logger
              .debug(s"SendSignal: ${signal}")
            context.system.receptionist ! Receptionist.Find(
              myServiceKey,
              adapterForSendSignal(signal)
            )
            Behaviors.same
          }
          case ResponseForSendSignal(signal, listings) => {
            myServiceKey.Listing
              .unapply(listings)
              .foreach(set => {
                logger.debug(s"ActorRefManager: ${set.toString()}")
                set.foreach(ref => {
                  logger
                    .debug(
                      s"Response: SendSignal ${signal} to ${ref}"
                    )
                  ref ! ActorRefManager.SendSignal(signal)
                })
              })
            Behaviors.same
          }
        }
      }
  }
}
