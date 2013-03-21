### What is migmongo?

MigMongo is a MongoDB schema migration manager written in Scala and inspired by mongeez.

### Add migmongo to your project
```xml
<dependency>
    <groupId>com.synhaptein</groupId>
	<artifactId>migmongo_2.9.2</artifactId>
	<version>1.0.1</version>                        s
</dependency>
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