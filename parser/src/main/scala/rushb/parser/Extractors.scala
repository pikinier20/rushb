package rushb.parser

import org.apache.commons.io.FileUtils

import java.io.{File, InputStream}
import java.nio.file.{Files, Paths}

object Extractors {
  implicit class Extractable[T <: ContentType](res: Response) {
    def extractInputStream: Seq[Either[String, InputStream]] = res.c match {
      case RarContent => RarExtractor.extract(res.is)
    }
  }

  sealed trait Extractor[T <: ContentType] {
    def extract(is: InputStream): Seq[Either[String, InputStream]]
  }

  object RarExtractor extends Extractor[RarContent.type] {
    override def extract(is: InputStream): Seq[Either[String, InputStream]] =
      try {
        val tempDir = Files.createTempDirectory("unpacked-demos")
        val tempFile = File.createTempFile("demo", ".rar")
        println(s"Downloading demo to ${tempFile.toPath}")
        FileUtils.copyInputStreamToFile(is, tempFile)
        println("Finished downloading")
        println(s"Unraring to $tempDir")
        import scala.sys.process._
        val path = Paths.get("unrar").toAbsolutePath
        val permissions = Seq("chmod", "+x", path.toString).!
        val cmd = Seq(path.toString, "e", tempFile.getAbsolutePath)
        val process = Process(cmd, tempDir.toFile).!(ProcessLogger(s => println(s)))
        if (process != 0) {
          Seq(Left(s"Unraring failed with value $process"))
        } else {
          println("Unraring finished")
          tempFile.delete()
          tempDir.toFile
            .listFiles
            .toSeq
            .filter(_.getName.endsWith(".dem"))
            .map(_.toPath)
            .map(Files.newInputStream(_))
            .map(Right(_))
        }
      } catch {
        case e: Exception => Seq(Left(s"Extracting rar failed with exception: ${e.getMessage}"))
      }
  }
}

sealed trait ContentType {
  def str: String
}

object ContentType {
  private val children = Seq(RarContent)
  def parse(str: String): Option[ContentType] = children.find(_.str == str)
}

case object RarContent extends ContentType {
  override def str: String = "application/x-rar-compressed"
}

case class Response(is: InputStream, c: ContentType)
