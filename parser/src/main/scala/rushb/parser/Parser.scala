package rushb.parser

import demoparser.config.ParserConfig
import demoparser.parser.DemoParser
import demoparser.serialization.JsonDemoSerializer

object Parser {
  def parseDemo(demoObj: DemoObject): Seq[Either[String, String]] = {
    import rushb.parser.preprocess.Preprocessors._
   demoObj.extractInputStream
     .map(_.flatMap(is => DemoParser.parseFromInputStream(is, ParserConfig.Default)))
     .map(_.map(d => JsonDemoSerializer.serializeWithMetadata(d, demoObj.link.date, demoObj.link.id, demoObj.link.title)))
  }
}
