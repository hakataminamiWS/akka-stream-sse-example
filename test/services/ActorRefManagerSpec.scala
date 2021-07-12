package services

import org.scalatestplus.play._
import org.scalatestplus.play.guice._

import org.scalatest.BeforeAndAfterAll

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.actor.typed.ActorRef

class ActorRefManagerSpec
    extends PlaySpec
    with GuiceOneAppPerTest
    with BeforeAndAfterAll {
  val testKit                       = ActorTestKit()
  val actors: Set[ActorRef[String]] = Set.empty
  val manager: ActorRef[ActorRefManager.ManagerCommand] =
    testKit.spawn(ActorRefManager.apply(actors))

  val probeA = testKit.createTestProbe[String]()
  val probeB = testKit.createTestProbe[String]()
  val probeC = testKit.createTestProbe[String]()

  override protected def beforeAll(): Unit = {}
  override protected def afterAll(): Unit = {
    testKit.shutdownTestKit()
  }

  "ActorRefManager" should {
    "send a signal string to only registered ActorRefs" in {
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
}
