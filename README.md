### What is migmongo?

MigMongo is a MongoDB schema migration manager written in Scala and inspired by mongeez.

### Add migmongo to your project

#### Scala 2.9.2
```xml
<dependency>
    <groupId>com.synhaptein</groupId>
    	<artifactId>migmongo_2.9.2</artifactId>
    	<version>1.0.2</version>
</dependency>
```

```scala
libraryDependencies += "com.synhaptein" % "migmongo_2.9.2" % "1.0.2"
```
#### Scala 2.10.0
```xml
<dependency>
    <groupId>com.synhaptein</groupId>
    	<artifactId>migmongo_2.10.0</artifactId>
    	<version>1.1.1</version>
</dependency>
```

```scala
libraryDependencies += "com.synhaptein" % "migmongo_2.10.0" % "1.1.1"
```

### Create your migrations
```scala
case class Migmongo(db: MongoDB) extends MigmongoEngine {
  changeGroups(
    MigrationV1("v1")
  )
}

case class MigrationV1(group: String) extends ChangeGroup {
  changeSet("ChangeSet-1", "author1",
    Script("""db.table1.drop();"""),
    Script("""db.table2.ensureIndex({"field1":1, "field2":-1});"""),
    CustomChange()
  )

  // Will be fire-and-forget
  asyncChangeSet("ChangeSet-2", "author2",
    Script("""db.collection1.update({}, {$set:{price:180}}, false ,true);""")
  )
}

case class CustomChange() extends Change {
  def run(db: MongoDB) {
    //Use db directly to do whatever you want!
  }
}
```

### Launch the migrations

```scala
Migmongo(db).process()
```