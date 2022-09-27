package rushb.crawler

import cats.effect.{ContextShift, ExitCode, IO, IOApp}
import monix.eval.Task
import monix.execution.Scheduler.Implicits.global
import monix.reactive.Observable
import rushb.model._

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration.DurationInt


// object Main extends IOApp {
//   implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
//   def observableToCsv(obs: Observable[DemoLink]): Observable[String] = obs
//     .foldLeft[Seq[DemoLink]](Seq())(_ :+ _)
//     .map { entries => entries
//       .map(e => s"${e.id};${e.title};${e.date};${e.link}")
//       .mkString("id;title;date;link\n","\n","")
//     }

//   override def run(args: List[String]): IO[ExitCode] = {
//     val q = QuerySettings(
//       from = Some("2021-01-01"),
//       to = Some("2022-04-01"),
//       stars = Some(2),
//       maps = Seq("de_mirage", "de_inferno", "de_dust2", "de_overpass")
//     )
//     val obs = new DemoLinksCrawler(q)
//       .getLinks
//       .flatMap {
//         case Right(value) =>
//           Observable(value)
//         case Left(error) =>
//           println(error)
//           Observable.empty
//       } match {
//         case obs => observableToCsv(obs)
//       }

//     val future: Future[Unit] = obs.foreach {
//       case csv => Files.write(Paths.get(s"${q.from.getOrElse("nodate")}.csv"), csv.getBytes(StandardCharsets.UTF_8))
//     }
//     IO.fromFuture(IO(future)).as(ExitCode.Success)
//   }
// }

object SecondMain extends IOApp {
  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
  def observableToCsv(obs: Observable[HltvStats]): Observable[String] = {
    obs
      .foldLeft(Seq.empty[HltvStats])(_ :+ _)
      .map { entries =>
        val header = "id;teamName;mapName;win;date;" + Range(0, 5).flatMap(i => Seq("kills", "deaths", "adr", "kast", "rating20").map(stat => s"player${i}_$stat")).mkString(";")
        entries
          .map { e => 
            val playerStrings = e.players.flatMap { p => Seq(p.kills, p.deaths, p.adr, p.kast, p.rating20) }.mkString(";")
            s"${e.id};${e.teamName};${e.mapName};${e.win};${e.date};${playerStrings}"
          }
          .mkString(s"id;teamName;mapName;win;date;$header\n","\n","")
    }
  }

  override def run(args: List[String]): IO[ExitCode] = {
    val q = QuerySettings(
      from = Some("2021-01-01"),
      to = Some("2022-04-01"),
      stars = Some(2),
      // maps = Seq("de_mirage", "de_inferno", "de_dust2", "de_overpass")
      maps = Seq.empty
    )
    val obs = new HltvStatsCrawler(q)
      .getLinks
      .flatMap {
        case Right(value) =>
          Observable.fromIterable(value)
        case Left(error) =>
          println(error)
          Observable.empty
      } match {
        case obs => observableToCsv(obs)
      }

    val future: Future[Unit] = obs.foreach {
      case csv => Files.write(Paths.get(s"${q.from.map(s => s"stats$s").getOrElse("nodate")}.csv"), csv.getBytes(StandardCharsets.UTF_8))
    }
    IO.fromFuture(IO(future)).as(ExitCode.Success)
  }
}
