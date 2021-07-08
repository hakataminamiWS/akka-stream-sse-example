package controllers

import javax.inject._
import play.api.mvc._

import service._

import akka.stream.scaladsl.Source
import akka.stream.typed.scaladsl.ActorSource
import akka.actor.typed.ActorRef
import akka.stream.OverflowStrategy

import scala.concurrent.ExecutionContext
import scala.util.Random
import scala.concurrent.duration._

@Singleton
class HomeController @Inject() (
    manager: ActorRef[ActorRefManager.ManagerCommand],
    cc: ControllerComponents
)(implicit ec: ExecutionContext)
    extends AbstractController(cc) {

  def index = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.index())
  }

  def getSignal = Action {
    val signalList   = List("red", "yellow", "green")
    val randomSignal = signalList(Random.nextInt(signalList.length))
    manager ! ActorRefManager.SendSignal(randomSignal)
    Ok
  }

  def sse = Action {
    import play.api.http.ContentTypes
    import play.api.libs.EventSource

    val source: Source[String, ActorRef[String]] =
      ActorSource
        .actorRef[String](
          completionMatcher =
            PartialFunction.empty, // never complete the stream because of a message
          failureMatcher =
            PartialFunction.empty, // never fail the stream because of a message
          bufferSize = 8,
          overflowStrategy = OverflowStrategy.dropHead
        )
        .watchTermination() { case (actorRef, done) =>
          manager ! ActorRefManager.Register(actorRef)
          done.onComplete(_ => manager ! ActorRefManager.UnRegister(actorRef))
          actorRef
        }

    // for setting "event: signal"
    implicit def pair[E]: EventSource.EventNameExtractor[E] =
      EventSource.EventNameExtractor[E](p => Some(("signal")))

    // for keep connection
    val heartbeat = EventSource.Event("", None, Some("heartbeat"))
    Ok.chunked(
      source
        .via(EventSource.flow)
        .keepAlive(30.second, () => heartbeat)
    ).as(ContentTypes.EVENT_STREAM)
  }
}
