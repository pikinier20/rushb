package rushb.parser.preprocess

import rushb.parser.{ContentType, DefaultContent, DemoObject, RarContent}

import java.io.InputStream

object Preprocessors {
  implicit class WithPreprocessor(demoObject: DemoObject) {
    def extractInputStream: Seq[Either[String, InputStream]] = demoObject.c match {
      case RarContent => rarPreprocessor.extract(demoObject.is)
      case DefaultContent => defaultPreprocessor.extract(demoObject.is)
    }
  }

  val rarPreprocessor: Preprocessor[RarContent.type] = new RarPreprocessor()
  val defaultPreprocessor: Preprocessor[DefaultContent.type] = new DefaultPreprocessor()

  trait Preprocessor[T <: ContentType] {
    def extract(is: InputStream): Seq[Either[String, InputStream]]
  }
}




