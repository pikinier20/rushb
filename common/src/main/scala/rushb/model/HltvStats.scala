package rushb.model

import upickle.default.{ReadWriter => RW, macroRW}

case class HltvStats(
  id: String, 
  date: Long, 
  teamName: String,
  mapName: String,
  win: Int,
  players: Seq[PlayerStats]
)

case class PlayerStats(
  rating20: Double,
  kills: Int,
  deaths: Int,
  adr: Double,
  kast: Double
)

object HltvStats {
  implicit val rw: RW[HltvStats] = macroRW
}

object PlayerStats {
  implicit val rw: RW[PlayerStats] = macroRW
}
