package ren.kujoka.IncanGold
import scala.util.Random

class Player(pNum: Int) {
  private val playerNum_ = pNum
  private val playerName = if (pNum == 0) "Your" else (pNum + 1) + "P's"
  private var temp_ = 0
  private var tent = 0
  private var relics = 0
  private var isExploring_ = false

  def go() {
    println(playerName + " action:Go")
  }
  
  def back(gem: Int, relic: Int) {
    val reward = "gem:" + (gem + temp) + ", relic:" + relic
    println(playerName + " action:Back(Reward:" + reward + ")")
    tent += (gem + temp)
    relics += relic
    isExploring_ = false
    temp = 0
  }

  def death() {
    temp = 0
    println(playerName + " action led to death")
  }

  def scoring(): Int = {
    var score = if (relics >= 4) relics * 10 - 15 else relics * 5
    score += tent
    score
  }

  def showScore() {
    println(playerName + " score is " + scoring + " points")
  }

  def think(gem: Int, relic: Int)(traps: Int*)(removedTraps: Int*): String = {
    traps.sum match {
      case 0 | 1 => "g"
      case 2 =>
        var goVote = 0
        var backVote = 0
        for (i <- 0 until traps.length) {
          if (traps(i) > 0) {
            backVote += 1
            if (removedTraps(i) > 0) {
              goVote += 1
            } else {
              backVote += 1
            }
          } else {
            goVote += 1
          }
        }
        if (relic > 0) backVote += 2 else goVote += 1
        if (gem > 7) backVote += 2 else goVote += 1
        if (goVote >= backVote) {
          "g"
        } else {
          "b"
        }
      case _ => 
        if (temp > 0) "b" else "g"
    }
  }

  def playerNum = playerNum_

  def temp = temp_

  def temp_= (newTemp: Int) = temp_ = newTemp

  def isExploring = isExploring_

  def isExploring_= (newIsExploring: Boolean) = isExploring_ = newIsExploring
}

class Chicken(pNum: Int) extends Player(pNum) {
  override def think(gem: Int, relic: Int)(traps: Int*)(removedTraps: Int*): String = {
    traps.sum match {
      case 0 => "g"
      case 1 =>
        if ((relic > 0 || gem > 4) && temp > 0) "b" else "g"
      case _ => 
        if (temp > 0) "b" else "g"
    }
  }
}

class Boldness(pNum: Int) extends Player(pNum) {
  override def think(gem: Int, relic: Int)(traps: Int*)(removedTraps: Int*): String = {
    traps.sum match {
      case 0 | 1 | 2 => "g"
      case 3 =>
        if (relic > 0 || gem > 7) "b" else "g"
      case 4 =>
        if (relic > 0 || gem > 4) "b" else "g"
      case _ => 
        if (temp > 0) "b" else "g"
    }
  }
}
