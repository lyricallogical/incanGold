package ren.kujoka.IncanGold
import ren.kujoka.common.Reader._
import scala.util.Random.shuffle
import scala.io.StdIn.readLine
import scala.collection.mutable.ArrayBuffer

object IncanGold {
  private var players: ArrayBuffer[Player] = _
  private var way: ArrayBuffer[Int] = _
  private var explorationCards: ArrayBuffer[Int] = _
  // 0:zombie, 1:poison spider, 2:big snake, 3:flame, 4:rockfall
  private var traps: Array[Int] = _
  private var removedTraps: Array[Int] = _

  def main(args: Array[String]) {
    way = ArrayBuffer.empty[Int]
    traps = Array.fill(5)(0)
    removedTraps = Array.fill(5)(0)
    var yn = ""
    do {
      game
      yn = yesOrNo("Play again?", "y", "n")
    } while (yn == "y")
  }

  def game() {
    setPlayers
    setExplorationCards(0)
    for (i <- 1 to 5) {
      addRelic
      explorationCards = shuffle(explorationCards)
      if (i > 1) players(0).showScore
      println("[ROUND" + i + "]")
      exploration
    }
    println("[Result]")
    for (player <- players)
      player.showScore
  }

  def exploration() {
    var progress = 0
    players.foreach(n => n.isExploring = true)
    traps = Array.fill(5)(0)
    do {
      if (traps.max > 0) {
        var recent = Array.fill(players.length)("")
        for (player <- players if player.isExploring == true) {
          if (player.playerNum == 0) {
            recent(0) = yesOrNo("Go or Back?", "g", "b")
          } else {
            recent(player.playerNum) = 
              player.think(waySum(1), relic(false))(traps:_*)(removedTraps:_*)
          }
        }
        var backRecent = 0
        for (r <- recent if r == "b") backRecent += 1
        for (i <- 0 until recent.length if recent(i) != "") {
          recent(i) match {
            case "g" => players(i).go
            case "b" =>
              if (backRecent == 1)
                players(i).back(waySum(backRecent), relic(true))          
              else
                players(i).back(waySum(backRecent), 0)
            case _ => None
          }
        }
        if (backRecent > 0) wayRefresh(backRecent)
      }
      if (progress == 0) println("Go up to trap exists")
      if (inTheRuins > 0) {
        way += explorationCards.remove(0)
        showCards(way(progress))
        showWay
        if (way(progress) < 99) {
          for (player <- players if player.isExploring == true)
            player.temp += (way(progress) / inTheRuins)
          way(progress) %= inTheRuins
        }
        progress += 1
        Thread.sleep(1000)
      }
    } while (traps.max < 2 && inTheRuins > 0)
    val relicInTheRuin = relic(true)
    if (traps.max >= 2) {
      for (player <- players if player.isExploring == true)
        player.death
      removedTraps(way.remove(way.length - 1) - 100) += 1
    }
    setExplorationCards(relicInTheRuin)
    way.clear
    traps = Array.fill(5)(0)
  }

  def inTheRuins(): Int = {
    var n = players.length
    for (player <- players)
      if (player.isExploring == false) n -= 1
    n
  }

  def showWay() {
    for (i <- 0 until way.length) {
      if (i % 5 == 0 && i != 0) println("->")
      print("|" + "%1$3d".format(way(i)) + "|")
    }
    println
  }

  def showCards(card: Int) {
    val trap = card match {
      case 100 => "zombie"
      case 101 => "poison spider"
      case 102 => "big snake"
      case 103 => "flame"
      case 104 => "rockfall"
      case _ => ""
    }
    if (card >= 100 && card <= 104) {
      traps(card - 100) += 1
      println("!!!We were ataccked by a " + trap + "!!!")
    } else if (card == 99) {
      println("***We found the relic***")
    } else {
      println("We have found " + card + " gems")
    }
  }

  def waySum(backRecent: Int): Int = {
    var sum = 0
    for (i <- 0 until way.length if (way(i) < 99)) {
      sum += (way(i) / backRecent)
    }
    sum
  }

  def wayRefresh(backRecent: Int) {
    for (i <- 0 until way.length if (way(i) < 99))
      way(i) %= backRecent
  }

  def relic(back: Boolean): Int = {
    var sum = 0
    for (i <- 0 until way.length if (way(i) == 99)) {
      if (back == true) way(i) = 0
      sum += 1
    }
    sum
  }

  def setExplorationCards(relicInTheRuin: Int) {
    explorationCards = ArrayBuffer.empty[Int]
    // add gems
    for (i <- 1 to 15)
      explorationCards += i
    
    // add traps
    // 100:zombie, 101:poison spider, 102:big snake, 103:flame, 104:rockfall
    for (i <- 100 to 104)
      for (j <- 0 until 3 - removedTraps(i - 100))
        explorationCards += i
    // add relics
    for (i <- 0 until relicInTheRuin)
      addRelic
    explorationCards = shuffle(explorationCards)
  }

  def addRelic() {
    // 99:relic
    explorationCards += 99
  }

  def setPlayers() {
    players = ArrayBuffer.empty[Player]
    val numberOfPersons = readIntLoop("Please enter the number of persons(3~8) > ",
      "Please enter the correct value", 3, 8)
    players += new Player(0)
    println("Please enter the nature of the opponent")
    for (i <- 1 until numberOfPersons) {
      val nature = 
          readIntLoop("0:Chicken, 1:Normal, 2:Boldness > ",
          "Please enter the correct value", 0, 2)
      nature match {
        case 0 => players += new Chicken(i)
        case 1 => players += new Player(i)
        case 2 => players += new Boldness(i)
      }
    }
  }
}
