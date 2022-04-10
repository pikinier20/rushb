package rushb
package crawler

import monix.reactive.Observable
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import rushb.model._

import java.net.URL
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import scala.annotation.tailrec
import scala.jdk.CollectionConverters._
import scala.concurrent.duration.DurationInt
import javax.management.Query

abstract class HltvHtmlCrawler[T](querySettings: QuerySettings) {
  private val linksPerPage = 100
  def getLinks: Observable[Either[String, T]] =
    resultsPages
      .flatMap(processResultPage)
      .map(processMatchPage)

  private def jsoupParseWithTimeout(link: String): Document = try {
    Jsoup.parse(new URL(link), 5000)
  } catch {
    case e: org.jsoup.HttpStatusException if e.getStatusCode == 429 =>
      println("Too many requests")
      Thread.sleep(5000)
      jsoupParseWithTimeout(link)
  }

  private def resultsPages: Observable[Document] = {
    val firstPage = jsoupParseWithTimeout(s"https://www.hltv.org/results${querySettings.toQueryString}")
    val totalResults = firstPage.select(".results .pagination-data").text()
    val ResultsNumberR = raw""".* of ([0-9]+)""".r
    val resultsNumber = totalResults match {
      case ResultsNumberR(resultsNumberStr) => resultsNumberStr.toInt
    }
    Observable.fromIterable(Range.inclusive(0, resultsNumber, 100))
      .map(offset => {
        val nextQuerySettings = querySettings
          .copy(
            offset = querySettings.offset.fold(Some(offset))(o => Some(o + offset))
          ).toQueryString
        val link = s"https://www.hltv.org/results$nextQuerySettings"
        link
      })
      .delayOnNext(500.millisecond)
      .map(jsoupParseWithTimeout)
  }

  private def processResultPage(page: Document): Observable[Document] = {
    val demoLinks = page.select(".allres div.result-con > a[href]")
      .asScala.toList.map(_.absUrl("href")).take(linksPerPage)
    Observable.fromIterable(demoLinks)
      .delayOnNext(500.millisecond)
      .map(jsoupParseWithTimeout)
  }

  def getUnixDate(matchPage: Document): Either[String, Long] = Option(
      matchPage
        .selectFirst("div.timeAndEvent div.date[data-unix]")
    ).map(_.attr("data-unix").toLong)
      .map(Right(_))
      .getOrElse(Left("Can't find demo date"))

  def getLink(matchPage: Document): Either[String, String] = matchPage
      .getElementsContainingText("GOTV Demo")
      .asScala
      .find(e => e.hasAttr("href"))
      .map(_.absUrl("href"))
      .map(Right(_))
      .getOrElse(Left("Can't find download link"))

  def getId(matchPage: Document): Either[String, String] = getLink(matchPage)
    .map(_.split("/").last)

  def processMatchPage(page: Document): Either[String, T]
}

class DemoLinksCrawler(q: QuerySettings) extends HltvHtmlCrawler[DemoLink](q) {
  override def processMatchPage(matchPage: Document): Either[String, DemoLink] = {
    val title = matchPage
      .select("div.teamsBox div.teamName")
      .asScala
      .map(_.text())
      .mkString(" - ")
    val unixDate = getUnixDate(matchPage)

    val downloadLink = getLink(matchPage)

    val id = getId(matchPage)

    for {
      id <- id
      uDate <- unixDate
      link <- downloadLink
    } yield DemoLink(id, title, uDate, link)
  }
}

class HltvStatsCrawler(q: QuerySettings) extends HltvHtmlCrawler[Seq[HltvStats]](q) {
  override def processMatchPage(matchPage: Document): Either[String, Seq[HltvStats]] = {
    import scala.math._
    val id = getId(matchPage)
    val unixDate = getUnixDate(matchPage)

    val hltvStats = {
      val ids = matchPage
        .select(".stats-menu-link .dynamic-map-name-full")
        .asScala.toList.filterNot(_.id() == "all").map { map =>
          map.text() -> map.id()
        }
      val statsContent = ids.flatMap { 
        case (mapName, id) => matchPage.select(s"#${id}-content.stats-content .totalstats").asScala.toList.map(mapName -> _)
      }
      statsContent.map {
        case (mapName, statsContent) => 
          val teamName = statsContent
            .select(".header-row .players a.teamName")
            .text()

          val wonTeam = matchPage
            .select(".mapholder")
            .asScala
            .toList
            .filter(_.select(".map-name-holder .mapname").text() == mapName)
            .head
            .select(".results.played .won .results-teamname").text()

          val won = if (wonTeam == teamName) 1 else 0

          val ratings20 = statsContent
            .select("tr:not(.header-row) .rating")
            .eachText()
            .asScala
            .map(_.toDouble)

          val killsDeaths = statsContent
            .select("tr:not(.header-row) .kd")
            .eachText()
            .asScala
            .map(_.split("-").map(_.toInt))
            .map(elem => elem(0) -> elem(1))

          val adrs = statsContent
            .select("tr:not(.header-row) .adr")
            .eachText()
            .asScala
            .map(_.toDouble)

          val kasts = statsContent
            .select("tr:not(.header-row) .kast")
            .eachText()
            .asScala
            .map(_.dropRight(1).toDouble)

          val playerStats = ratings20.zip(killsDeaths).zip(adrs).zip(kasts).map {
            case ((((rating20, (kills, deaths)), adr), kast)) => PlayerStats(rating20, kills, deaths, adr, kast)
          }.toList

          (teamName, mapName, won, playerStats)
      }
    }
    for {
      _id <- id
      _date <- unixDate
    } yield hltvStats.toSeq.map {
      case (teamName, mapName, won, playerStats) =>
        HltvStats(_id, _date, teamName, mapName, won, playerStats)
    }
  }
}
