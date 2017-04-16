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
              val p = PunchlinesRepository.forDate(now().atStartOfDay().atZone(of("UTC")))
              complete {
                HttpEntity(
                  ContentType(`application/json`.withParams(Map("charset" -> "utf-8"))),
                  p.toJsonString
                )
              }
            } ~
            path("pretty") {
              val p = PunchlinesRepository.random
              val artist = p.artist
              val title = p.title.getOrElse("")
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
                     |      <h1>${p.punchline}</h1>
                     |      <h3><u>$title</u>, $artist</h3>
                     |    </div>
                     |  </body>
                     |</html>
                     |""".stripMargin
                )
              }
            } ~
            path("random") {
              val p = PunchlinesRepository.random
              complete {
                HttpEntity(
                  ContentType(`application/json`.withParams(Map("charset" -> "utf-8"))),
                  p.toJsonString
                )
              }
            } ~
            pathPrefix("artist") {
              path(Segment) { artist =>
                val ps = PunchlinesRepository.findByArtist(artist)
                complete {
                  HttpEntity(
                    ContentType(`application/json`.withParams(Map("charset" -> "utf-8"))),
                    toJson(ps).toString()
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