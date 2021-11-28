package rushb.model

import upickle.default.{ReadWriter => RW, macroRW}

case class DemoLink(id: String, title: String, date: Long, link: String)

object DemoLink {
  implicit val rw: RW[DemoLink] = macroRW
}
