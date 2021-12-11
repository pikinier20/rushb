package rushb.parser.preprocess

import org.apache.commons.io.FileUtils
import rushb.parser.{Prepare, RarContent}

import java.io.{File, InputStream}
import java.nio.file.{Files, Paths}

class RarPreprocessor extends Preprocessors.Preprocessor[RarContent.type] {
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
