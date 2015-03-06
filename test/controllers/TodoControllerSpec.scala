import controllers._
import models.datastore.MongoDBDataStore

import play.api.libs.json._
import play.api.test._
import play.api.mvc._

object TodoControllerSpec extends PlaySpecification with Results {

  "TodoController" should {

    class TestController extends Controller with TodoController with MongoDBDataStore
    val controller = new TestController()

    "#all" should {
      "return all todo items" in {
        running(FakeApplication()) {
          val uri = routes.TodoController.all().url
          val req = FakeRequest("GET", uri)
          val Some(result) = route(req)

          status(result) must be equalTo OK
          contentAsJson(result) must beAnInstanceOf[JsArray]
        }
      }
    }

    "#get" should {
      "return the todo item by its id" in {
        running(FakeApplication()) {
          val id = "54f8bf000d00000e00c5a8b4"
          val uri = routes.TodoController.get(id).url
          val req = FakeRequest("GET", uri)
          val Some(result) = route(req)

          status(result) must be equalTo OK
          contentAsString(result) must not beEmpty
        }
      }
    }

    "#save" should {
      "store a new todo item" in {
        running(FakeApplication()) {
          val uri = routes.TodoController.save().url
          val req = FakeRequest("POST", uri) withJsonBody (Json.obj("description" -> "My Task"))
          val Some(result) = route(req)

          status(result) must be equalTo 201
          //contentAsString(result) must not be "INVALID_JSON"
        }
      }
    }

    "#update" should {
      "update a todo item" in {
        pending("To be fixed")

        running(FakeApplication()) {
          val id = "54f8bf000d00000e00c5a8b4"

          val uri = routes.TodoController.update(id).url
          val req = FakeRequest("PUT", uri).withFormUrlEncodedBody("description" -> "My task updated", "completed" -> "true")
          val Some(result) = route(req)

          status(result) must be equalTo 201
        }
      }
    }
  }
}
