package helpers

import org.specs2.execute.AsResult
import org.specs2.specification.Around
import org.specs2.specification.Scope
import org.specs2.execute.Result

import play.api.Play.current
import play.api.test.FakeApplication

import play.modules.reactivemongo.ReactiveMongoPlugin

abstract class WithMongoDB(val app: FakeApplication = FakeApplication()) extends Around with Scope {
  val db = ReactiveMongoPlugin.db

  implicit def implicitApp = app

  override def around[T: AsResult](t: => T): Result = try {
    val res = t
    AsResult.effectively(res)
  } finally {
    db.connection.close()
  }
}
