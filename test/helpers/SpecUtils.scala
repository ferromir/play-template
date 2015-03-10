package helpers

import play.api.test.FakeApplication

object SpecUtils {

  def printRoutes(app: FakeApplication): Unit = {
    app.routes.get.documentation.foreach(doc => {
      print(doc)
      print("\n")
    })
  }

  def printConfiguration(app: FakeApplication): Unit = {
    app.configuration.entrySet.foreach(config => {
      print(config._1)
      print(" - ")
      print(config._2)
      print("\n")
    })
  }

  def getFakeApplicationWithMongo(): FakeApplication = {
    new FakeApplication(
      new java.io.File("."),
      classOf[FakeApplication].getClassLoader,
      Seq("play.modules.reactivemongo.ReactiveMongoPlugin"),
      Nil,
      Map("mongodb.uri" -> "mongodb://localhost:12345/test")
    )
  }
}
