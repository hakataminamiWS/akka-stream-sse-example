package services

import org.scalatestplus.play._
import org.scalatestplus.play.guice._

import org.scalatest.BeforeAndAfterAll

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.actor.typed.ActorRef
import akka.actor.typed.receptionist.Receptionist
import akka.actor.typed.receptionist.ServiceKey
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

class ReceptionistManagerSpec
    extends PlaySpec
    with GuiceOneAppPerTest
    with BeforeAndAfterAll {

  val testKit = ActorTestKit()

  override protected def beforeAll(): Unit = {}
  override protected def afterAll(): Unit = {
    testKit.shutdownTestKit()
  }

  "ReceptionistManager" should {
    "send Register SourceActorRef message in ActorRefManager, " +
      "and send a UnRegister SourceActorRef message too" in {
        val probActorRefManager =
          testKit.createTestProbe[ActorRefManager.ManagerCommand]()

        testKit.system.receptionist ! Receptionist.Register(
          ReceptionistManager.myServiceKey,
          probActorRefManager.ref
        )

        object ActorNothing {
          def apply(): Behavior[String] = { Behaviors.stopped }
        }
        val actorNothing: ActorRef[String] = testKit.spawn(ActorNothing.apply())

        val testManager
            : ActorRef[ReceptionistManager.ReceptionistManagerCommand] =
          testKit.spawn(ReceptionistManager.apply())

        // Register
        testManager ! ReceptionistManager.RegisterSourceActorRef(
          actorNothing
        )
        // check
        probActorRefManager
          .expectMessage(ActorRefManager.Register(actorNothing))

        // UnRegister
        testManager ! ReceptionistManager.UnRegisterSourceActorRef(
          actorNothing
        )
        // check
        probActorRefManager
          .expectMessage(ActorRefManager.UnRegister(actorNothing))

      }
  }

  "ReceptionistManager" should {
    "send a signal to ActorRefManager" in {
      val probActorRefManager =
        testKit.createTestProbe[ActorRefManager.ManagerCommand]()
      testKit.system.receptionist ! Receptionist.Register(
        ReceptionistManager.myServiceKey,
        probActorRefManager.ref
      )

      val testManager
          : ActorRef[ReceptionistManager.ReceptionistManagerCommand] =
        testKit.spawn(ReceptionistManager.apply())

      testManager ! ReceptionistManager.SendSignal(
        "testSignal"
      )

      probActorRefManager
        .expectMessage(ActorRefManager.SendSignal("testSignal"))
    }
  }
}
