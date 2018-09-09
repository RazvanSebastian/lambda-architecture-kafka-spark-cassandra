# Lambda architecture : Kafka-Spark-Cassandra
## **1. Kafka install, config and run**

Download kafka_2.11-2.0.0 from [link](https://www.apache.org/dyn/closer.cgi?path=/kafka/2.0.0/kafka-2.0.0-src.tgz) and unzip it locally. 

##### **Start Zookeeper server**
```
> bin/zookeeper-server-start.bat config/zookeeper.properties
```

##### **Configuration and starting kafka cluster of brokers**
In the _config_ folder you must to create 3 server properites files : server.properites, server1.properites and server2.properites. Now in every properites file you have to change the followings
1. Listening port, with: _9092_, _9093_ and _9094_. You cand find the listening port under the _Socket Server Settings_ section (Ex : `listeners=PLAINTEXT://:9094` )
2. Broker id under the _Server Basics_ section. (Ex : `broker.id=1`)
3. Kafka log dir under the _Log Basics_ section. (Ex : `log.dirs=/tmp/kafka-logs-brokerId`)

__Start Kafka brokers:__

``` 
> bin/kafka-server-start.bat config/server1.properties
```

``` 
> bin/kafka-server-start.bat config/server2.properties
```

```
> bin/kafka-server-start.bat config/server3.properties
```

__Create the topic:__

``` 
> bin/kafka-topics.bat --create --zookeeper localhost:2181 --replication-factor 3 --partitions 1 --top
ic click-stream-topic
```

__Check the new topic:__ 

``` 
> bin/kafka-topics.bat --describe --zookeeper localhost:2181 --topic click-stream-topic
``` 

## **2. Cassandra install and run**

Download apache-cassandra-3.11.3 from [link](http://www.apache.org/dyn/closer.lua/cassandra/3.11.3/apache-cassandra-3.11.3-bin.tar.gz) and unzip it locally.

1. Start Cassandra node cluster

``` 
> bin/cassandra.bat
```

2. Start **cqlsh**

```
> bin/cqlsh.bat
```

3. Create a kayspace 

```
cqlsh> create keyspace lambda with replication = {'class':'SimpleStrategy','replication_factor':1};
```

4. Use the lambda keyspace

``` 
cqlsh> use lambda;
```

5. Create two tables

``` 	
cqlsh:lambda> create table batch_activity_by_product (
    product text,
    timestamp bigint,
    pageViewCount bigint, 
    addToCartCount bigint, 
    purchaseCount bigint, 
    primary key(product,timestamp)) with clustering order by (timestamp asc);
 ```
```
cqlsh:lambda> create table stream_activity_by_product (
  product text,
    timestamp bigint,
    pageViewCount bigint, 
    addToCartCount bigint, 
    purchaseCount bigint, 
    primary key(product,timestamp)) with clustering order by (timestamp asc);
 ```


## **3. Start Java applciations**

Run the **KafkaMain.class** from **kafka** project to generate messages to the cluster of Kafka brokers. You must be able to see in console at every 1 second one new printed message. 


Run the **BatchJob.class** and **StreamJob.class** from **sparkjobs** project. The duration between requests to kafka cluster is set to 30 seconds. 

**_Note 1_** Make sure that you have the _data.csv_ file udner the _src/main/resources_ folder in both projects.

**_Note 2_** To check the result you can execute query on Cassandra tables using

```
cqlsh:lambda> select * from select * from batch_activity_by_product;
```

```
cqlsh:lambda> select * from stream_activity_by_product;
```

