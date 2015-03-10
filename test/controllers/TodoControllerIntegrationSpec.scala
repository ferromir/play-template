package controllers

import com.github.athieriot.EmbedConnection
import de.flapdoodle.embed.mongo.distribution.Version
import play.api.libs.json.{ JsValue, JsArray }
import play.api.test._

object TodoControllerIntegrationSpec extends PlaySpecification with EmbedConnection {

  sequential

  override def embedMongoDBVersion(): Version.Main = { Version.Main.V2_7 }

  "TodoController" should {

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
      "return the todo item by its id" in new WithApplication(FakeApplication()) {
        val id = "todoitem-1"
        val uri = routes.TodoController.get(id).url
        val req = FakeRequest("GET", uri)
        val Some(result) = route(req)

        status(result) must be equalTo OK
        contentAsJson(result) must beAnInstanceOf[JsValue]
      }
    }

    /*"#save" should {
      "store a new todo item" in {
        val uri = routes.TodoController.save().url
        val req = FakeRequest("POST", uri) withJsonBody (Json.obj("description" -> "My Task"))
        val Some(result) = route(req)

        status(result) must be equalTo 201
      }
    }

    "#update" should {
      "update a todo item" in {
        val id = "todoitem-1"
        val uri = routes.TodoController.update(id).url
        val req = FakeRequest("PUT", uri) withFormUrlEncodedBody ("completed" -> "true")
        val Some(result) = route(req)

        status(result) must be equalTo 200
      }
    }

    "#delete" should {
      "delet a todo item" in {
        val id = "54f8bf000d00000e00c5a8b4"
        val uri = routes.TodoController.delete(id).url
        val req = FakeRequest("DELETE", uri)
        val Some(result) = route(req)

        status(result) must be equalTo 201
      }
    }*/
  }

}
