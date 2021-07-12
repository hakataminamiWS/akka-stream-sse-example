package controllers

import org.scalatestplus.play._
import org.scalatestplus.play.guice._

import play.api.test._
import play.api.test.Helpers._

/** Add your spec here.
  * You can mock out a whole application including requests, plugins etc.
  *
  * For more information, see https://www.playframework.com/documentation/latest/ScalaTestingWithScalaTest
  */
class HomeControllerSpec
    extends PlaySpec
    with GuiceOneAppPerTest
    with Injecting {

  "HomeController GET" should {
    "render the index page from the application" in {
      val controller = inject[HomeController]
      val index      = controller.index().apply(FakeRequest(GET, "/"))

      status(index) mustBe OK
      contentType(index) mustBe Some("text/html")
      contentAsString(index) must {
        include("red") or include("yellow") or include("green")
      }
    }
  }

  "HomeController GET /sse" should {
    "return content type text/event-stream" in {
      val controller = inject[HomeController]
      val sse        = controller.sse().apply(FakeRequest(GET, "/sse"))

      contentType(sse) mustBe Some("text/event-stream")
    }
  }

  "HomeController GET /signal" should {
    "return OK status" in {
      val controller = inject[HomeController]
      val getSignal  = controller.getSignal().apply(FakeRequest(GET, "/signal"))

      status(getSignal) mustBe OK
    }
  }

}
