package rushb.crawler

import cats.effect.{ExitCode, IO, IOApp}
import monix.eval.Task
import monix.execution.Scheduler.Implicits.global
import monix.reactive.Observable
import rushb.model.DemoLink

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}
import scala.concurrent.{Await, Future}
import scala.concurrent.duration.DurationInt

object Main extends IOApp {
  def observableToCsv(obs: Observable[DemoLink]): Observable[String] = obs
    .foldLeft[Seq[DemoLink]](Seq())(_ :+ _)
    .map { entries => entries
      .map(e => s"${e.id};${e.title};${e.date.getTime};${e.link}")
      .mkString("id;title;date;link\n","\n","")
    }

  def queriesObservable: Observable[QuerySettings] = Observable.fromIterable(Range(2, 10))
    .flatMap(i => Observable.fromIterable(Range(1, 32)).map(_ -> i))
    .map {
      case (day, month) => String.format("%02d", day) -> String.format("%02d", month)
    }
    .map {
      case (day, month) => QuerySettings(s"2021-$month-$day", s"2021-$month-$day")
    }

  override def run(args: List[String]): IO[ExitCode] = {
    val obs = queriesObservable
      .map(q => q -> new HltvHtmlCrawler(q))
      .flatMap {
        case (q, crawler) =>
          val demoLinksObs = crawler.getLinks
            .flatMap {
              case Right(value) =>
                Observable(value)
              case Left(error) =>
                println(error)
                Observable.empty
            }
          observableToCsv(demoLinksObs).map(q -> _)
      }
    val future: Future[Unit] = obs.foreach {
      case (q, csv) => Files.write(Paths.get(s"${q.from.get}.csv"), csv.getBytes(StandardCharsets.UTF_8))
    }
    IO.fromFuture(IO(future)).as(ExitCode.Success)
  }
}
