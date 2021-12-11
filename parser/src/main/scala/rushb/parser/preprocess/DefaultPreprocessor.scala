package rushb.parser.preprocess

import rushb.parser.DefaultContent

import java.io.InputStream

class DefaultPreprocessor extends Preprocessors.Preprocessor[DefaultContent.type] {
  override def extract(is: InputStream): Seq[Either[String, InputStream]] = Seq(Right(is))
}
