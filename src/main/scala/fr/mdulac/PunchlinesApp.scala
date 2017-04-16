package fr.mdulac

import java.time.LocalDate.now
import java.time.ZoneId.of

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.MediaTypes.`application/json`
import akka.http.scaladsl.model.{ContentType, ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import fr.mdulac.repository.PunchlinesRepository
import play.api.libs.json.Json.toJson

trait Router {

  val routes =
    pathPrefix("artists") {
      get {
        pathEnd {
          complete {
            HttpEntity(
              ContentType(`application/json`.withParams(Map("charset" -> "utf-8"))),
              toJson(PunchlinesRepository.findAll.groupBy(_.artist).keys.toList).toString()
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
                toJson(PunchlinesRepository.findAll).toString()
              )
            }
          } ~
            path("daily") {
              val epochSecond = now().atStartOfDay().atZone(of("UTC")).toEpochSecond
              val p = PunchlinesRepository.findAll((epochSecond % PunchlinesRepository.findAll.length).toInt)
              complete {
                HttpEntity(
                  ContentType(`application/json`.withParams(Map("charset" -> "utf-8"))),
                  toJson(p).toString()
                )
              }
            } ~
            path("pretty") {
              val random = PunchlinesRepository.random
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
              val random = PunchlinesRepository.random
              complete {
                HttpEntity(
                  ContentType(`application/json`.withParams(Map("charset" -> "utf-8"))),
                  toJson(random).toString()
                )
              }
            } ~
            pathPrefix("artist") {
              path(Segment) { artist =>
                val p = PunchlinesRepository.findByArtist(artist)
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

object PunchlinesApp extends App with Router {

  implicit val system = ActorSystem()
  implicit val executor = system.dispatcher
  implicit val materializer = ActorMaterializer()

  val config = ConfigFactory.load()

  Http().bindAndHandle(routes, config.getString("http.interface"), config.getInt("http.port"))

}