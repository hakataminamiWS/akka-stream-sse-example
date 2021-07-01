package controllers

import javax.inject._
import play.api._
import play.api.mvc._

import service._

import akka.stream.scaladsl.Source
import akka.stream.typed.scaladsl.ActorSource
import akka.actor.typed.ActorSystem
import scala.concurrent.ExecutionContext
import akka.actor.typed.ActorRef
import akka.stream.OverflowStrategy

/** This controller creates an `Action` to handle HTTP requests to the
  * application's home page.
  */
@Singleton
class HomeController @Inject() (
    manager: ActorRef[ActorRefManager.ManagerCommand],
    cc: ControllerComponents
)(implicit ec: ExecutionContext)
    extends AbstractController(cc) {

  var count = 0

  /** Create an Action to render an HTML page.
    *
    * The configuration in the `routes` file means that this method
    * will be called when the application receives a `GET` request with
    * a path of `/`.
    */
  def index() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.index())
  }

  def get = Action {
    manager ! ActorRefManager.SendSignal("red")
    Ok
  }

  def sse() = Action {
    import play.api.http.ContentTypes
    import play.api.libs.EventSource

    count += 1
    println(count)

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

    Ok.chunked(
      source
        .via(EventSource.flow)
    ).as(ContentTypes.EVENT_STREAM)
  }
}
