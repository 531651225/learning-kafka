# 同步提交offset

 在BasicConsumeLoop的例子中，我们假定消费者被配置成自动提交offset(这是默认的行为)。自动提交offset就像是cron一样，隔一段时间
 提交一次offset，时间间隔由`auto.commit.interval.ms`来配置。
 如果消费者挂掉，那么当它重启或者rebalance被触发后，这个挂掉的consumer之前拥有的所有partition新的拥有者会从这些partition最近一次提交
 的offset开始消费。

 因此，如果你想尽量减少被重复读取的消息，你就要减少自动提交offset的间隔。但是，有些用户需要对offset进行更细粒度的控制，因此，consumer
 支持用户对提交offset的行为进行直接控制。这个支持是通过consumer的commit api来进行的。最简单可靠的手动提交offset的方法是使用
 `commitSync()`进行同步提交。正像这个方法的名字暗示的一样，这个方法会阻塞至提交成功。

 需要说明的是，如果你想要使用commit API，你需要首先在配置中设置`enable.auto.commit`为`false`以关闭自动提交。

