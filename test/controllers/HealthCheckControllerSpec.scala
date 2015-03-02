import controllers._
import play.api.test._
import play.api.mvc._
import play.api.Play.current

object HealthCheckControllerSpec extends PlaySpecification with Results {

  "HealthCheckController" should {

    class TestController extends Controller with HealthCheckController
    val controller = new TestController()

    "#status" should {
      "return OK" in {
        running(FakeApplication()) {
          val uri = routes.HealthCheckController.get().url
          val req = FakeRequest("GET", uri)
          val Some(result) = route(req)
          status(result) must be equalTo (OK)
        }
      }
    }
  }
}
