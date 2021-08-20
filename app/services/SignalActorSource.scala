package services

import akka.actor.typed.ActorRef

import scala.concurrent.ExecutionContext

import akka.stream.scaladsl.Source

import akka.stream.typed.scaladsl.ActorSource

import akka.stream.OverflowStrategy

import play.api.Logger

object SignalActorSource {
  val logger: Logger = Logger("akka.actor.SignalActorSource")

  def apply(
      receptionistManager: ActorRef[
        ReceptionistManager.ReceptionistManagerCommand
      ]
  ): Source[String, ActorRef[String]] = {

    ActorSource
      .actorRef[String](
        completionMatcher =
          PartialFunction.empty, // never complete the stream because of a message
        failureMatcher =
          PartialFunction.empty, // never fail the stream because of a message
        bufferSize = 1,
        overflowStrategy = OverflowStrategy.dropHead
      )
      //
      .mapMaterializedValue { actorRef =>
        logger.debug(s"${actorRef} is created")
        receptionistManager ! ReceptionistManager.RegisterSourceActorRef(
          actorRef
        )
        actorRef
      }
      //
      .watchTermination() { case (actorRef, done) =>
        implicit val ec = ExecutionContext.global
        done.onComplete { case _ =>
          logger.debug(s"${actorRef} is terminated")
          receptionistManager ! ReceptionistManager.UnRegisterSourceActorRef(
            actorRef
          )
        }
        actorRef
      }
  }

}