package com.synhaptein.migmongo.commands

import com.mongodb.casbah.MongoDB

trait Change {
  def run(db: MongoDB)
}
