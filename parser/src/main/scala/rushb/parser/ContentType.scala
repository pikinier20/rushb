package rushb.parser

sealed trait ContentType {
  def str: String
}

object ContentType {
  private val children = Seq(RarContent)
  final def parse(str: String): ContentType = children.find(_.str == str).getOrElse(DefaultContent)
}

case object RarContent extends ContentType {
  override def str: String = "application/x-rar-compressed"
}

case object DefaultContent extends ContentType {
  override def str: String = ""
}