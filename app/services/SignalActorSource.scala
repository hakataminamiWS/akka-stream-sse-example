package services

import akka.actor.typed.ActorRef

import scala.concurrent.ExecutionContext

import akka.stream.scaladsl.Source

import akka.stream.typed.scaladsl.ActorSource

import akka.stream.OverflowStrategy

object SignalActorSource {
  def apply(
      manager: ActorRef[ActorRefManager.ManagerCommand]
  )(implicit ec: ExecutionContext): Source[String, ActorRef[String]] =
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
        manager ! ActorRefManager.Register(actorRef)
        actorRef
      }
      //
      .watchTermination() { case (actorRef, done) =>
        done.onComplete { _ =>
          println(s"unRegister on signal ${actorRef}")
          manager ! ActorRefManager.UnRegister(actorRef)
        }
        actorRef
      }
}
