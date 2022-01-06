package rushb.parser

import java.io.InputStream
import java.nio.file.{Files, Path, Paths, StandardCopyOption}

object Prepare {
  def prepare(): Option[String] = for {
    unrar <- prepareUnrar()
  } yield unrar

  private def prepareUnrar(): Option[String] = {
    val os = System.getProperty("os.name")
    val unrarInputStream: Option[InputStream] = Option(
      if(os.contains("Mac")) {
        getClass.getClassLoader.getResourceAsStream("unrar/unrar_mac")
      }
      else if(os.contains("nux")) {
        getClass.getClassLoader.getResourceAsStream("unrar/unrar_linux")
      }
      else null
    )
    unrarInputStream match {
      case Some(is) =>
        val path = Paths.get("/tmp/unrar")
        Files.copy(is, path, StandardCopyOption.REPLACE_EXISTING)
        None
      case None =>
        Some("Cannot find proper unrar executable")
    }
  }

}
