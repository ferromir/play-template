package controllers

import com.github.athieriot.EmbedConnection
import de.flapdoodle.embed.mongo.distribution.Version
import helpers.MongoFixtureHelpers
import play.api.libs.json.{ Json, JsValue, JsArray }
import play.api.test._
import org.specs2.specification.BeforeAll
import org.specs2.specification.Action

object TodoControllerIntegrationSpec extends PlaySpecification with EmbedConnection with MongoFixtureHelpers with BeforeAll {

  override def embedConnectionPort(): Int = { 27017 }
  override def embedMongoDBVersion(): Version.Main = { Version.Main.V2_7 }

  def beforeAll: Any = loadFixture("todolist_test", "todoitem", "test/fixtures/todoitem-1.json")

  sequential

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
        val id = "550073d2428081fe37ed4410"
        val uri = routes.TodoController.get(id).url
        val req = FakeRequest("GET", uri)
        val Some(result) = route(req)

        status(result) must be equalTo OK
        contentAsJson(result) must beAnInstanceOf[JsValue]
      }
    }

    "#save" should {
      "store a new todo item" in new WithApplication(FakeApplication()) {
        val uri = routes.TodoController.save().url
        val req = FakeRequest("POST", uri) withJsonBody (Json.obj("description" -> "My Task"))
        val Some(result) = route(req)

        status(result) must be equalTo 201
      }
    }

    "#update" should {
      "update a todo item" in new WithApplication(FakeApplication()) {
        val id = "550073d2428081fe37ed4410"
        val uri = routes.TodoController.update(id).url
        val req = FakeRequest("PUT", uri) withFormUrlEncodedBody ("completed" -> "true")
        val Some(result) = route(req)

        status(result) must be equalTo 200
      }
    }

    "#delete" should {
      "delet a todo item" in new WithApplication(FakeApplication()) {
        val id = "550073d2428081fe37ed4410"
        val uri = routes.TodoController.delete(id).url
        val req = FakeRequest("DELETE", uri)
        val Some(result) = route(req)

        status(result) must be equalTo 200
      }
    }
  }

}
