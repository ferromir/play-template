import com.github.athieriot.{CleanAfterExample, EmbedConnection}
import de.flapdoodle.embed.mongo.distribution.Version
import helpers.MongoTestHelpers
import models.TodoItem
import org.specs2.specification.BeforeEach
import play.api.libs.json.{Json, JsArray}
import play.api.mvc.Results
import play.api.Play.current
import play.api.test.{FakeApplication, WithServer, PlaySpecification}
import play.api.libs.ws._
import play.test.Helpers._

import scala.concurrent._
import scala.concurrent.duration._

object TodoControllerSpec extends PlaySpecification with Results with MongoTestHelpers {

  val port = 9000
  val url = s"http://localhost:$port/todos"

  //override def embedConnectionPort(): Int = { 27017 }
  //override def embedMongoDBVersion(): Version.Main = { Version.Main.V2_7 }

  // TODO: To be removed to avoid DRY (it's app code also)
  implicit val todoItemFormat = Json.format[TodoItem]

  sequential

  "GET to /todos" should {
    "return all persisted data" in new WithServer(FakeApplication(), port) {
      val todos = for (n <- 0 until 5) yield TodoItem(newId, s"Task $n for GET: /todos", false)
      Await.result(saveDocuments[TodoItem](todos, "todoitem"), 10 seconds)

      val response = await(WS.url(url).get())
      response.status must equalTo(OK)

      response.json must beAnInstanceOf[JsArray]
      (response.json \\ "id").size must be equalTo 5

      Await.result(Future.sequence(todos map { t => removeDocument[TodoItem](Map("id" -> t.id), "todoitem") }), 10 seconds)
    }
  }

  "GET to /todos/:id" should {
    "return the expected todo item" in new WithServer(FakeApplication(), port) {
      pending
    }
  }

  "POST to /todos/" should {
    "store a new todo" in new WithServer(FakeApplication(), port) {
      pending
    }
  }

  "PUT to /todos/:id" should {
    "update a todo" in new WithServer(FakeApplication(), port) {
      pending
    }
  }

  "DELETE to /todos/:id" should {
    "delete a todo item" in new WithServer(FakeApplication(), port) {
      pending
    }
  }

}
