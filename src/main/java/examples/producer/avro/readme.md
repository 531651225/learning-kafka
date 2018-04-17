这个例子使用了Confluent的Schema Registry和Avro 序列化和反序列化器。

这个例子使用了Avro的code generation，此需要运行mvn clean package来触发maven插件来根据avro的schema生成代码。
生产的代码在target/generated-sources下,schama的namespace可以配置生产代码的包名

github上例子: https://github.com/confluentinc/example

文档地址:https://docs.confluent.io/3.3.0/avro.html#defining-an-avro-schema
