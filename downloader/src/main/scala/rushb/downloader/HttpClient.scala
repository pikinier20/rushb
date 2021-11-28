package rushb
package downloader

import java.io.{IOException, InputStream}
import java.net.URL
import scala.annotation.tailrec

case class Response(contentType: String, is: InputStream)

object HttpClient {
  private val userAgent = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11"
  private val waitTime = 10000L

  def get(link: String): Either[String, Response] = trial(link)

  @tailrec
  private def trial(link: String, n: Int = 10): Either[String, Response] =
    try {
      val con = new URL(link).openConnection()
      con.setRequestProperty(
        "User-Agent",
        userAgent
      )
      val contentType = con.getContentType
      Right(Response(contentType,con.getInputStream))
    } catch {
      case io: IOException if n > 0 =>
        println(s"Downloading from link: $link failed with exception: ${io.toString}")
        Thread.sleep(waitTime)
        trial(link, n - 1)
      case io: IOException =>
        Left(s"Downloading from link: $link failed with exception: ${io.toString}")
    }
}