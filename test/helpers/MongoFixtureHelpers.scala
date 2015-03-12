package helpers

import de.flapdoodle.embed.mongo.{ MongoImportProcess, MongoImportStarter }
import de.flapdoodle.embed.mongo.config.{ Net, MongoImportConfigBuilder }
import de.flapdoodle.embed.mongo.distribution.Version
import de.flapdoodle.embed.process.runtime.Network

trait MongoFixtureHelpers {

  def embedConnectionPort(): Int = { 12345 }
  def embedMongoDBVersion(): Version.Main = { Version.Main.PRODUCTION }

  def loadFixture(dbName: String, collection: String, jsonFile: String): MongoImportProcess = {
    val mongoImportConfig = new MongoImportConfigBuilder()
      .version(embedMongoDBVersion)
      .net(new Net(embedConnectionPort, Network.localhostIsIPv6()))
      .db(dbName)
      .collection(collection)
      .upsert(true)
      .dropCollection(true)
      .jsonArray(true)
      .importFile(jsonFile)
      .build();

    val mongoImportExecutable = MongoImportStarter.getDefaultInstance().prepare(mongoImportConfig);
    mongoImportExecutable.start()
  }

}

