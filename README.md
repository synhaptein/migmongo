### What is migmongo?

MigMongo is a MongoDB schema migration manager written in Scala and inspired by mongeez.

### Note on Versions

#### Current

Version 3.x is tested with mongodb 2.4.x and use reactivemongo instead of casbah (current)

#### Older (end-of-life)

Version 2.x works for mongodb >= 2.0, but lost support for db.eval and use casbah directly

Version 1.1.1 works for mongodb >= 2.0, but since 2.4 you need to have admin access to use migmongo because of [db.eval](http://docs.mongodb.org/manual/reference/method/db.eval/)

### Add migmongo to your project

#### Scala 2.10.x
```xml
<dependency>
    <groupId>com.synhaptein</groupId>
	<artifactId>migmongo_2.10</artifactId>
	<version>3.0.1</version>
</dependency>
```

```scala
libraryDependencies += "com.synhaptein" %% "migmongo" % "3.0.1"
```

### Create your migrations
```scala
case class Migmongo(db: DefaultDB) extends MigmongoEngine {
    changeGroups(
        MigrationMyApp("myApp")
    )
}

case class MigrationMyApp(group: String) extends ChangeGroup {
     changeSet("ChangeSet-1", "author1") { db =>
        List(
          db[BSONCollection]("table1").insert(BSONDocument("name" -> "John Doe")),
          db[BSONCollection]("table2").indexesManager.ensure(Index(Seq("field1" -> Ascending, "field2" -> Descending)))
        )
      }

      // Will be fire-and-forget
      asyncChangeSet("ChangeSet-2", "author2") { db =>
        List(
        db[BSONCollection]("collection1").update(
          selector = BSONDocument(),
          update = BSONDocument("$set" -> BSONDocument("price" -> 180)),
          multi = true)
        )
      }
}
```

### Launch the migrations

```scala
val db: DefaultDB = MigmongoEngine.db("mongodb://localhost/myApp")
Migmongo(db).process() // It blocks till all synchronous changeSet are done
```
