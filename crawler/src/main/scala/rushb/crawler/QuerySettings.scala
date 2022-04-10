package rushb.crawler

import java.text.SimpleDateFormat
import java.util.Date
import scala.language.implicitConversions

case class QuerySettings(from: Option[String] = None, to: Option[String] = None, offset: Option[Int] = None, stars: Option[Int] = None, maps: Seq[String] = Seq.empty) {
  def toQueryString: String =
    Seq(
      from.map(f => s"startDate=$f"),
      to.map(t => s"endDate=$t"),
      offset.map(o => s"offset=$o"),
      stars.map(s => s"stars=$s"),
      maps.map(m => s"map=$m")
    ).flatten match {
      case Nil => ""
      case list => "?" + list.mkString("&")
    }
}

object QuerySettings {
  implicit def formatDate(date: Long): String = {
    val format = new SimpleDateFormat("yyyy-MM-dd")
    format.format(new Date(date))
  }
}