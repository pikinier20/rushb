package rushb.crawler

import java.util.Date

case class QuerySettings(from: Option[String], to: Option[String], offset: Int = 0)

object QuerySettings {
  def apply(offset: Int): QuerySettings = QuerySettings(None, None, offset)

  def apply(): QuerySettings = QuerySettings(None, None)

  def apply(from: String, to: String): QuerySettings = QuerySettings(Some(from), Some(to))

  def apply(from: String, to: String, offset: Int): QuerySettings = QuerySettings(Some(from), Some(to), offset)
}