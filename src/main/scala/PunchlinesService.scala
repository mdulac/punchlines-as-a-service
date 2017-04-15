import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.MediaTypes.`application/json`
import akka.http.scaladsl.model.{ContentType, HttpEntity}
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import fr.mdulac.model.Punchline
import play.api.libs.json.Json.{parse, toJson}

import scala.io.Source.fromResource
import scala.util.Random

trait Router {

  val punchlines: List[Punchline]

  val routes = pathPrefix("punchlines") {
    path("random") {
      get {
        val random = punchlines(Random.nextInt(punchlines.length))
        complete {
          HttpEntity(
            ContentType(`application/json`.withParams(Map("charset" -> "utf-8"))),
            toJson(random).toString()
          )
        }
      }
    }
  }

}

object PunchlinesService extends App with Router {

  implicit val system = ActorSystem()
  implicit val executor = system.dispatcher
  implicit val materializer = ActorMaterializer()

  val config = ConfigFactory.load()

  override lazy val punchlines = parse(fromResource("punchlines.json").mkString).as[List[Punchline]]

  Http().bindAndHandle(routes, config.getString("http.interface"), config.getInt("http.port"))

}