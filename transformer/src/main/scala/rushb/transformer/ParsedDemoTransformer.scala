package rushb.transformer

import ujson.Value
import upickle.default._

import scala.collection.mutable

object ParsedDemoTransformer {
  val headerFields = Seq(
    "mapName",
    "playbackTime",
    "playbackTicks"
  )

  def transformJsonToCsv(jsonString: String): Map[String, String] = {
    val json = ujson.read(jsonString)
    val headerFields = stripHeader(json("header").obj.toMap)
    val date = json("date").num
    val id = json("id").str
    val events = json("events").arr.toSeq
    val groupedEvents = groupEvents(events)

    groupedEvents.map {
      case (str, value) => str -> value.map { event =>
        event += (("id", id))
        event += (("date", date))
        event ++= headerFields
        event.map {
          case (str, value) => (str, value.toString())
        }.toMap
      }
    }.map {
      case (str, value) => str -> generateCsv(value)
    }
  }

  def generateCsv(entries: Seq[Map[String, String]]): String = entries.headOption.fold("") { head =>
    val keys = head.keys.toList
    val header = keys.mkString(";")
    val values = entries.map { entry =>
      keys.map(entry(_)).mkString(";")
    }
    (header +: values).mkString("\n")
  }

  def stripHeader(header: Map[String, ujson.Value.Value]): Map[String, ujson.Value.Value] = header.filter {
    case (str, value) if headerFields.contains(str) => true
    case _ => false
  }

  def groupEvents(events: Seq[ujson.Value.Value]): Map[String, Seq[mutable.LinkedHashMap[String, Value]]] = events
    .map(_.obj)
    .groupBy(_("name").str)
    .map {
      case (str, value) => (str, value.map(_.filter {
        case (str, _) => str != "name"
      }.flatMap(flatten)
      ))
    }

  def flatten(arg: (String, ujson.Value.Value)): Map[String, ujson.Value.Value] =
    arg._2.objOpt.fold(Map((arg._1, arg._2))) { obj =>
      obj.flatMap {
        case (str, value) => flatten(s"${arg._1}_${str}" -> value)
      }.toMap
    }
}
