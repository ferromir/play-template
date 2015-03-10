package helpers

import de.flapdoodle.embed.mongo.distribution.Version
import de.flapdoodle.embed.mongo.config.Net
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder
import de.flapdoodle.embed.process.runtime.Network
import de.flapdoodle.embed.mongo.MongodStarter
import org.junit.runner._
import play.api.test._
import play.api.test.Helpers._
import org.specs2.main.Arguments
import org.specs2.mutable._
import org.specs2.runner._
import org.specs2.specification.Fragments
import org.specs2.specification.Example
import org.specs2.specification.FragmentsBuilder
import de.flapdoodle.embed.mongo.distribution.Feature
import de.flapdoodle.embed.mongo.distribution.IFeatureAwareVersion
import virtualizedlimbo.MongoVersion

trait SpecificationWithMongo extends Specification with FragmentsBuilder {

  //Override this method to personalize testing port
  def embedConnectionPort(): Int = { 12345 }

  //Override this method to personalize MongoDB version
  def embedMongoDBVersion(): IFeatureAwareVersion = { new MongoVersion("2.6.7", Feature.SYNC_DELAY) }

  lazy val network = new Net(embedConnectionPort, Network.localhostIsIPv6())

  lazy val mongodConfig = new MongodConfigBuilder()
    .version(embedMongoDBVersion)
    .net(network)
    .build

  lazy val runtime = MongodStarter.getDefaultInstance

  lazy val mongodExecutable = runtime.prepare(mongodConfig)

  override def map(fs: => Fragments) = startMongo ^ fs ^ stopMongo

  private def startMongo() = {
    Example("Start Mongo", {
      mongodExecutable.start;
      success
    })
  }

  private def stopMongo() = {
    Example("Stop Mongo", {
      mongodExecutable.stop;
      success
    })
  }

}
