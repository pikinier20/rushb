package rushb
package crawler

import monix.reactive.Observable
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import rushb.model.DemoLink

import java.net.URL
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import scala.annotation.tailrec
import scala.jdk.CollectionConverters._
import scala.concurrent.duration.DurationInt

class HltvHtmlCrawler(querySettings: QuerySettings) {
  private val linksPerPage = 3
  def getLinks: Observable[Either[String, DemoLink]] =
    resultsPages
      .flatMap(processResultPage)
      .map(getDemo)

  private def jsoupParseWithTimeout(link: String): Document = try {
    Jsoup.parse(new URL(link), 5000)
  } catch {
    case e: org.jsoup.HttpStatusException if e.getStatusCode == 429 =>
      println("Too many requests")
      Thread.sleep(5000)
      jsoupParseWithTimeout(link)
  }

  private def resultsPages: Observable[Document] =
    // TODO: Check count of results and get all
    Observable(0)
      .map(_ * 100)
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

  private def processResultPage(page: Document): Observable[Document] = {
    val demoLinks = page.select(".allres div.result-con > a[href]")
      .asScala.toList.map(_.absUrl("href")).take(linksPerPage)
    Observable.fromIterable(demoLinks)
      .delayOnNext(500.millisecond)
      .map(jsoupParseWithTimeout)
  }

  private def getDemo(matchPage: Document): Either[String, DemoLink] = {
    val title = matchPage
      .select("div.teamsBox div.teamName")
      .asScala
      .map(_.text())
      .mkString(" - ")
    val unixDate = Option(
      matchPage
        .selectFirst("div.timeAndEvent div.date[data-unix]")
    ).map(_.attr("data-unix").toLong)
      .map(Right(_))
      .getOrElse(Left("Can't find demo date"))

    val downloadLink = matchPage
      .getElementsContainingText("GOTV Demo")
      .asScala
      .find(e => e.hasAttr("href"))
      .map(_.absUrl("href"))
      .map(Right(_))
      .getOrElse(Left("Can't find download link"))

    val id = downloadLink.map(_.split("/").last)

    for {
      id <- id
      uDate <- unixDate
      link <- downloadLink
    } yield DemoLink(id, title, uDate, link)
  }
}
