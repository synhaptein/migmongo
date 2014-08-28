### What is migmongo?

MigMongo is a MongoDB schema migration manager written in Scala and inspired by mongeez and liquibase.

### Note on Versions

#### Current

Version 3.x is tested with mongodb 2.4.x and use reactivemongo

### Add migmongo to your project

#### Scala 2.10.x or 2.11.x
```xml
<dependency>
    <groupId>com.synhaptein</groupId>
	<artifactId>migmongo_2.10</artifactId>
	<version>3.0.3</version>
</dependency>
```

```scala
libraryDependencies += "com.synhaptein" %% "migmongo" % "3.0.3"
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
val db: DefaultDB = getDatabase(...) // Get an instance of your db from reactivemongo
Migmongo(db).process() // It blocks till all synchronous changeSet are done
```


### License

Apache License Version 2.0
http://apache.org/licenses/LICENSE-2.0.txt
