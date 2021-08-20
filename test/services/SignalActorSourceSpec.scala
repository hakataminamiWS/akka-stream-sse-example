package services

import org.scalatestplus.play._
import org.scalatestplus.play.guice._

import org.scalatest.BeforeAndAfterAll

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.actor.typed.ActorRef
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Keep
import akka.stream.KillSwitches

class SignalActorSourceSpec
    extends PlaySpec
    with GuiceOneAppPerTest
    with BeforeAndAfterAll {

  val testKit = ActorTestKit()

  implicit val system = testKit.system

  override protected def beforeAll(): Unit = {}
  override protected def afterAll(): Unit = {
    testKit.shutdownTestKit()
  }

   "SignalActorSource" should {
    "send Register and unRegister messages to ReceptionistManager at lifecycle" in {

      val probReceptionistManager =
        testKit
          .createTestProbe[ReceptionistManager.ReceptionistManagerCommand]()

      val signalActorSource =
        SignalActorSource.apply(probReceptionistManager.ref)

      implicit val system = testKit.system
      val (ref, out) = signalActorSource
        .viaMat(KillSwitches.single)(Keep.both)
        .toMat(Sink.cancelled)(Keep.left)
        .run()

      probReceptionistManager
        .expectMessage(ReceptionistManager.RegisterSourceActorRef(ref))
      probReceptionistManager
        .expectMessage(
          ReceptionistManager.UnRegisterSourceActorRef(ref)
        )
    }
  }
}
