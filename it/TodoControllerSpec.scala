import play.api.mvc.Results
import play.api.test.{FakeApplication, WithServer, PlaySpecification}

object TodoControllerSpec extends PlaySpecification with Results {

  val port = 9000
  val url = s"http://localhost:$port/hc"

  "GET to /todos" should {
    "return status=OK and all persisted data" in new WithServer(FakeApplication(), port) {
      pending
    }
  }

  "GET to /todos/:id" should {
    "return status=OK and the expected todo item" in new WithServer(FakeApplication(), port) {
      pending
    }
  }

}
