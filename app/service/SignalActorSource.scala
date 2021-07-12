package service

import service._
import play.api.libs.concurrent.ActorModule
import akka.stream.scaladsl.Source
import akka.stream.typed.scaladsl.ActorSource
import akka.actor.typed.ActorRef
import akka.stream.OverflowStrategy

import com.google.inject
import scala.concurrent.ExecutionContext

import akka.stream.typed.scaladsl.ActorSource

class SignalActorSource(
    manager: ActorRef[ActorRefManager.ManagerCommand],
    implicit val ec: ExecutionContext
)

object SignalActorSource {

  def apply(
      manager: ActorRef[ActorRefManager.ManagerCommand],
  ) = ???

// ): Source[String, ActorRef[String]] =
//     ActorSource
//       .actorRef[String](
//         completionMatcher =
//           PartialFunction.empty, // never complete the stream because of a message
//         failureMatcher =
//           PartialFunction.empty, // never fail the stream because of a message
//         bufferSize = 8,
//         overflowStrategy = OverflowStrategy.dropHead
//       )
//       .mapMaterializedValue { actorRef =>
//         manager ! ActorRefManager.Register(actorRef)
//         actorRef
//       }
//       .watchTermination() { case (actorRef, done) =>
//         done.onComplete { _ =>
//           manager ! ActorRefManager.UnRegister(actorRef)
//         }
//         actorRef
//       }
}
