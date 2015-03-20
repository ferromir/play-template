package ithelpers

import de.flapdoodle.embed.mongo.MongodStarter
import de.flapdoodle.embed.mongo.config.{Net, MongodConfigBuilder}
import de.flapdoodle.embed.mongo.distribution.Version
import de.flapdoodle.embed.process.runtime.Network
import org.specs2.specification.{Fragments, Example}

trait EmbeddedMongoHelper {

  //Override this method to personalize testing port
  def embedConnectionPort(): Int = { 27017 }

  //Override this method to personalize MongoDB version
  def embedMongoDBVersion(): Version.Main = { Version.Main.PRODUCTION }

  lazy val network = new Net(embedConnectionPort, Network.localhostIsIPv6)

  lazy val mongodConfig = new MongodConfigBuilder()
    .version(embedMongoDBVersion)
    .net(network)
    .build

  lazy val runtime = MongodStarter.getDefaultInstance

  lazy val mongodExecutable = runtime.prepare(mongodConfig)

  def startMongo() = {
    println("Start Mongo")
    mongodExecutable.start
  }

  def stoptMongo() = {
    println("Stop Mongo")
    mongodExecutable.stop
  }


}
