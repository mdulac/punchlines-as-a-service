package fr.mdulac.model

import play.api.libs.json.Json

case class Punchline(punchline: String, artist: String, album: Option[String], title: Option[String]) {

  def toJson = Json.toJson(this)

  def toJsonString = toJson.toString()

}

object Punchline {

  implicit val reads = Json.reads[Punchline]
  implicit val writes = Json.writes[Punchline]

}