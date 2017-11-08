## Generate proto classes

```
protoc --proto_path=src/main/resources/protos --java_out=src/main/java src/main/resources/protos/registerDoctor.proto
```

```
protoc --proto_path=src/main/resources/protos --go_out=src/main/go/src/care.solve.schedule/protocol src/main/resources/protos/registerDoctor.proto
```