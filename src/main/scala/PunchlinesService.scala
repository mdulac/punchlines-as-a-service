import java.time.LocalDate.now
import java.time.ZoneId.of

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.MediaTypes.`application/json`
import akka.http.scaladsl.model.{ContentType, ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import fr.mdulac.model.Punchline
import play.api.libs.json.Json.{parse, toJson}

import scala.io.Source.fromResource
import scala.util.Random

trait Router {

  val punchlines: List[Punchline]

  val routes =
    pathPrefix("artists") {
      get {
        pathEnd {
          complete {
            HttpEntity(
              ContentType(`application/json`.withParams(Map("charset" -> "utf-8"))),
              toJson(punchlines.groupBy(_.artist).keys.toList).toString()
            )
          }
        }
      }
    } ~
      pathPrefix("punchlines") {
        get {
          pathEnd {
            complete {
              HttpEntity(
                ContentType(`application/json`.withParams(Map("charset" -> "utf-8"))),
                toJson(punchlines).toString()
              )
            }
          } ~
            path("daily") {
              val epochSecond = now().atStartOfDay().atZone(of("UTC")).toEpochSecond
              val p = punchlines((epochSecond % punchlines.length).toInt)
              complete {
                HttpEntity(
                  ContentType(`application/json`.withParams(Map("charset" -> "utf-8"))),
                  toJson(p).toString()
                )
              }
            } ~
            path("pretty") {
              val random = punchlines(Random.nextInt(punchlines.length))
              val artist = random.artist
              val title = random.title.getOrElse("")
              complete {
                HttpEntity(
                  ContentTypes.`text/html(UTF-8)`,
                  s"""<html>
                     |  <head>
                     |    <style>
                     |      html, body, .container {
                     |        height: 100%;
                     |      }
                     |      .container {
                     |        display: flex;
                     |        align-items: center;
                     |        justify-content: center;
                     |        flex-direction: column;
                     |      }
                     |    </style>
                     |  </head>
                     |  <body>
                     |    <div class="container">
                     |      <h1>${random.punchline}</h1>
                     |      <h3><u>$title</u>, $artist</h3>
                     |    </div>
                     |  </body>
                     |</html>
                     |""".stripMargin
                )
              }
            } ~
            path("random") {
              val random = punchlines(Random.nextInt(punchlines.length))
              complete {
                HttpEntity(
                  ContentType(`application/json`.withParams(Map("charset" -> "utf-8"))),
                  toJson(random).toString()
                )
              }
            } ~
            pathPrefix("artist") {
              path(Segment) { artist =>
                val p = punchlines.filter(_.artist.toLowerCase == artist.toLowerCase)
                complete {
                  HttpEntity(
                    ContentType(`application/json`.withParams(Map("charset" -> "utf-8"))),
                    toJson(p).toString()
                  )
                }
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