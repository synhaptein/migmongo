package com.synhaptein.migmongo.commands

import com.mongodb.casbah.MongoDB

case class Script(script: String*) extends Change {
  def run(db: MongoDB) {
    db.eval(script.mkString(""))
  }

  override def toString() = script.mkString("\n")
}
