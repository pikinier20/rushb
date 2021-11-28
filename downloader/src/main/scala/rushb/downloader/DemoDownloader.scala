package rushb
package downloader

import rushb.model.DemoLink

import java.io.InputStream

class DemoDownloader(links: List[DemoLink]) {
  def download(): List[Either[String, Response]] = links
      .map(l => HttpClient.get(l.link))
}
