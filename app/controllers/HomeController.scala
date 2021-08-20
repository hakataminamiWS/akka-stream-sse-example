package controllers

import javax.inject._

import play.api.mvc._

import services._

import akka.actor.typed.ActorRef

import scala.concurrent.ExecutionContext
import akka.actor.typed.receptionist.Receptionist

@Singleton
class HomeController @Inject() (
    rm: ActorRef[ReceptionistManager.ReceptionistManagerCommand],
    cc: ControllerComponents
)(implicit ec: ExecutionContext)
    extends AbstractController(cc) {

  def index = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.index())
  }

  def getSignal = Action {
    import scala.util.Random

    val signalList   = List("red", "yellow", "green")
    val randomSignal = signalList(Random.nextInt(signalList.length))
    rm ! ReceptionistManager.SendSignal(randomSignal)
    Ok
  }

  def sse = Action {
    import play.api.http.ContentTypes

    import play.api.libs.EventSource

    import akka.stream.scaladsl.Source

    import scala.concurrent.duration._

    val source: Source[String, _] = SignalActorSource.apply(rm)

    // for setting "event: signal"
    implicit def pair[E]: EventSource.EventNameExtractor[E] =
      EventSource.EventNameExtractor[E](p => Some(("signal")))

    // for keep connection, add "event: heartbeat"
    val heartbeat = EventSource.Event("", None, Some("heartbeat"))
    Ok.chunked(
      source
        .via(EventSource.flow)
        .keepAlive(30.second, () => heartbeat)
    ).as(ContentTypes.EVENT_STREAM)
  }
}
