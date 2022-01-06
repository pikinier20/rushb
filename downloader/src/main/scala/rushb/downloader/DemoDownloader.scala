package rushb
package downloader

import rushb.model.DemoLink

import java.io.InputStream

class DemoDownloader(link: DemoLink) {
  def download(): Either[String, Response] = HttpClient.get(link.link)
}
