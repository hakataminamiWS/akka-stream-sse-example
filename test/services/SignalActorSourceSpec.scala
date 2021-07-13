package services

import org.scalatestplus.play._
import org.scalatestplus.play.guice._

import org.scalatest.BeforeAndAfterAll

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.actor.typed.ActorRef
import scala.concurrent.ExecutionContext
import akka.stream.scaladsl.Sink
import akka.stream.Materializer
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Keep
import akka.stream.KillSwitches

class SignalActorSourceSpec
    extends PlaySpec
    with GuiceOneAppPerTest
    with BeforeAndAfterAll {
  implicit val ec = ExecutionContext.Implicits.global

  val testKit = ActorTestKit()

  implicit val system = testKit.system

  override protected def beforeAll(): Unit = {}
  override protected def afterAll(): Unit = {
    testKit.shutdownTestKit()
    // system.terminate()
    println("afterAll")
  }

  "SignalActorSource" should {
    "send Register and UnRegister massages to manager in a source lifecycle" in {

      val probManager =
        testKit.createTestProbe[ActorRefManager.ManagerCommand]()

      val ((in, killSwitch), out) = SignalActorSource
        .apply(probManager.ref)
        .viaMat(KillSwitches.single)(Keep.both)
        .toMat(Sink.cancelled)(Keep.both)
        .run()

      probManager.expectMessage(ActorRefManager.Register(in))
      probManager.expectMessage(ActorRefManager.UnRegister(in))

    }
  }
}
