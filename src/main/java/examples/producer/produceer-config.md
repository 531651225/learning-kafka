# 概念


Kafka producer在概念上比consumer要简单很多，因为不同producer之间不需要进行协调(不像生产者group那样)。
它的主要功能是把每条消息对应到一个topic partition， 然后把一个产生请求发给那个partition的leader。
第一个功能是通过一个partitioner来做的，它个partitioner使用hash函数选择消息对应的分区。
Kafka自带的partition保证所有相同且非空的key对应的消息会被发给同一个分区。
如果没有提供key，那么就会使用round-robin的方式来确保消息均匀地发送给不同的分区。

每个分区都有一个leader和多个副本，它们分布于集群的不同broker上。所以写请求都被发给分区的leader。
副本通过从leader拉取消息来保持同步。当leader停机或者挂掉，新的Leader会从in-sync replicas（即保持同步的副本）中选取。
根据生产者的配置，发送给leader的生产请求可以被leader挂起一段时间，直到所有的副本都确认写入成功。
但也可以只有leader确定写入成功，或者不需要确定写入成功。这样就使得producer可以控制消息的持久性级别，代价是吞吐量。


不管producer对于ack的配置如何，写到partition leader的消息并不能立即被consumer读取。
当所有in-sync replicas都确认某消息写入成功，这条消息才被认为committed，只有committed的肖息才能被读取。
这使得消息如果已经被读取，它就不会由于单台机器的故障而丢失。这也意味着，如果一个消息只被leader确认写入成功(ack=1)，
那么如果在其它副本拉取到这条消息之前leader挂掉，这么消息就丢失了。但是，在实践中把ack设为1是一个合理的妥协，这使得消息在大部分情况下都有持久性保证，
而又不会使得吞吐量下降太厉害。

producer的精妙之处在于使用batching/compression来实现高吞吐，以及保证消息的可靠传递(就像前边提到的那样)。

# 配置

可以在[Kafka文档](https://kafka.apache.org/documentation.html#producerconfigs)里找到所有的配置项。
下面列举了一些关键的数据项以及它们是怎么影响producer的行为。

## 关键配置

需要配置`bootstrap.servers`以使得producer可以找到Kafka集群。
然后需要配置`client.id`以使得broker知道是哪个client发送的请求。这些设置对于Java, C/C++和Python的客户端都是通用。

## 消息持久性

你可以通过配置ack来值来控制写到Kafka的消息的持久性。这里的持久性是指消息对于服务器故障的忍受度。
ack的默认值是"1"，意味着需要分区的leader来确认写入成功。Kafka所能提供的最强的持久性保证是"ack=all"，
这样可以保证不仅分区的leader写入成功，而且所有的in-sync副本都写入成功。
你也可以把ack设成0, 这样可以使得吞吐量最大化，但是你就不能保证消息被成功的写入到了Kafka，
因为broker这时候甚至都不会发响应给你。这也就意味着你不能获知你发送的消息对应的offset。
注意，对于C/C++和Python的客户端，可以对不同的topic分别配置不同的ack值，
但是可以使用`default_topic_conf`来在C/C++客户端上进行全局配置，
以及在Python和Go客户端里使用`default.topic.conf`进行全局配置。

## 如何保证消息顺序

通常，消息会按照它们使用producer客户端发送的顺序写入到broker。但是，如果你把`retries`设成大于0的值(默认值是0)，
那么可能被重试发送的消息后面的消息发送成功了，那么顺序就乱了。
为了既能重试发送，又不会乱序，可以把`max.in.flight.requests.per.connection`设成1，
这样一次就只有一个in-flight请求(指已经发送成功，但是还未收到响应的请求)。
如果不使用retry，那么broker可以保持消息发送的顺序，但是就可能会由于个别的发送失败造成消息丢失。

## 批量发送和压缩

Kafka的Producer客户端会尝试把数组收集起来做为一个batch发送，来提高吞吐量。
当使用Java客户端时，可以使用`batch.size`来控制每个batch以byte计的最大大小。
为了使得有时间来累积数据，可以设置`linger.ms`来使得producer延迟发送。
可以使用`compression.type`来启用压缩。压缩时是对整个batch压缩，因此大的batch通常会有更高的压缩率。

当使用C/C++，Python和Go的客户端时，可以使用`batch.num.messages`来设置每个batch的最大消息条数。可以使用`compression.codec`来启用压缩。

## 发送队列的控制

可以使用`buffer.memory`来控制Java客户端中未发送数据队列所占用的内存大小。
当达到这个大小时，producer会阻塞send操作最多`max.block.ms`时间，如果仍然没能降低内存使用，就会抛出一个异常。
另外，如果你不想让消息在发送队列中停留无限长的时间，你可以设置`request.timeout.ms`。
如果这个时间超过了，但是消息还没有成功发送，那么就它就会被从发送队列中移除并且会抛出一个异常。

在C/C++, Python和Go的客户端中也有类似的配置。使用`queue.buffering.max.messages`来控制总的未发送消息的数量。
可以用`queue.buffering.max.ms`控制一个消息可以在队列中的等待时间。

#核心配置
max.in.flight.requests.per.connection:指定了生产者在收到服务器相应之前可以发送多少个消息
设成1可以保证消息是按发送顺序写入服务器的,即使发生了重试。

linger.ms :该参数指定了生产者在发送批次之前等待更多消息加入批次的时间 起到延迟效果。默认情况下,只要有可用线程，哪怕批次里只有一条数据,生产值也会将消息发送出去



