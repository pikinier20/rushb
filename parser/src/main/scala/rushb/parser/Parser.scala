package rushb.parser

import demoparser.config.ParserConfig
import demoparser.parser.DemoParser
import demoparser.serialization.JsonDemoSerializer

import java.io.File
import java.nio.file.Files

object Parser {
  def parseDemo(demoObj: DemoObject): Seq[Either[String, String]] = {
    import rushb.parser.preprocess.Preprocessors._
    val res = demoObj.extractInputStream
     .map(_.flatMap(is => {
       println("Parsing started")
       val res = DemoParser.parseFromInputStream(is, ParserConfig.Default)
       println("Parsing finished")
       res
     }))
     .map(_.map(d => JsonDemoSerializer.serializeWithMetadata(d, demoObj.link.date, demoObj.link.id, demoObj.link.title)))
    res
  }
}
