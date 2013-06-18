### What is migmongo?

MigMongo is a MongoDB schema migration manager written in Scala and inspired by mongeez.

### Note on Version 1.x

Version 1.x works for mongodb >= 2.0, but since 2.4 you need to have admin access to use migmongo because of [db.eval](http://docs.mongodb.org/manual/reference/method/db.eval/)

### Add migmongo to your project
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