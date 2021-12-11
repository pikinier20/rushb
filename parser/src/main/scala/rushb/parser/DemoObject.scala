package rushb.parser

import rushb.model.DemoLink

import java.io.InputStream

case class DemoObject(is: InputStream, c: ContentType, link: DemoLink)
