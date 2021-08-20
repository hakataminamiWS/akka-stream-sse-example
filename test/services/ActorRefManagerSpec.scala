package services

import org.scalatestplus.play._
import org.scalatestplus.play.guice._

import org.scalatest.BeforeAndAfterAll

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.Behavior

class ActorRefManagerSpec
    extends PlaySpec
    with GuiceOneAppPerTest
    with BeforeAndAfterAll {
  val testKit = ActorTestKit()

  override protected def beforeAll(): Unit = {}
  override protected def afterAll(): Unit = {
    testKit.shutdownTestKit()
  }

  "ActorRefManager" should {
    "send a signal string to only registered ActorRefs" in {
      val manager: ActorRef[ActorRefManager.ManagerCommand] =
        testKit.spawn(ActorRefManager.apply())

      val probeA = testKit.createTestProbe[String]()
      val probeB = testKit.createTestProbe[String]()
      val probeC = testKit.createTestProbe[String]()

      manager ! ActorRefManager.Register(probeA.ref)
      manager ! ActorRefManager.Register(probeB.ref)
      manager ! ActorRefManager.Register(probeC.ref)

      manager ! ActorRefManager.UnRegister(probeC.ref)

      val sendSignalString = "red"
      manager ! ActorRefManager.SendSignal(sendSignalString)

      val expectMessageString = "red"
      probeA.expectMessage(expectMessageString)
      probeB.expectMessage(expectMessageString)
      probeC.expectNoMessage()
    }
  }

  "ActorRefManager" should {
    "stopped if all Registered ActorRefs are UnRegistered" in {
      val probe = testKit.createTestProbe[ActorRefManager.ManagerCommand]()
      val actorRefManager: ActorRef[ActorRefManager.ManagerCommand] =
        testKit.spawn(Behaviors.monitor(probe.ref, ActorRefManager.apply()))

      object ActorNothing {
        def apply(): Behavior[String] = { Behaviors.same }
      }
      val ref: ActorRef[String] = testKit.spawn(ActorNothing.apply())

      actorRefManager ! ActorRefManager.Register(ref)
      actorRefManager ! ActorRefManager.UnRegister(ref)

      probe.expectTerminated(actorRefManager.ref)
    }
  }
}
