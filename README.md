### What is migmongo?

MigMongo is a MongoDB schema migration manager written in Scala and inspired by mongeez.

### Note on Versions

Version 1.1.1 works for mongodb >= 2.0, but since 2.4 you need to have admin access to use migmongo because of [db.eval](http://docs.mongodb.org/manual/reference/method/db.eval/) (end-of-life)

Version 2.x works for mongodb >= 2.0, but lost support for db.eval and use casbah directly (current)

### Add migmongo to your project

#### Scala 2.9.2
```xml
<dependency>
    <groupId>com.synhaptein</groupId>
    	<artifactId>migmongo_2.9.2</artifactId>
    	<version>2.0.0</version>
</dependency>
```

```scala
libraryDependencies += "com.synhaptein" % "migmongo_2.9.2" % "2.0.0"
```

#### Scala 2.10.x
```xml
<dependency>
    <groupId>com.synhaptein</groupId>
	<artifactId>migmongo_2.10</artifactId>
	<version>2.1.1</version>
</dependency>
```

```scala
libraryDependencies += "com.synhaptein" % "migmongo_2.10" % "2.1.1"
```

### Create your migrations
```scala
case class Migmongo(db: MongoDB) extends MigmongoEngine {
    changeGroups(
        MigrationV1("v1")
    )
}

case class MigrationV1(group: String) extends ChangeGroup {
    changeSet("ChangeSet-1", "author1") { db =>
      db("table1").drop()
      db("table2").ensureIndex(MongoDBObject("field1" -> 1, "field2" -> -1))
    }

    // Will be fire-and-forget
    asyncChangeSet("ChangeSet-2", "author2") { db =>
      db("collection1").update(MongoDBObject(), MongoDBObject("$set" -> MongoDBObject("price" -> 180)), false, true)
    }
}
```

### Launch the migrations

```scala
Migmongo(db).process()
```