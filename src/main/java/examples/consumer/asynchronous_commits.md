# 异步提交offset

每次调用commit API，都会有一个offset commit request被发送给broker。
在使用同步提交的API时，consumer会阻塞，直到broker确认执行成功。
这样做会降低consumer的吞吐量，因为阻塞的这段时间本来可以做别的事情。

为了解决这个问题，一种方法是增加每次`poll()`返回的数量量。
consumer有一个配置项`fetch.min.bytes`控制每次fetch返回多少数据。broker会等待一段时间，
直到数据足够多了或者`fetch.max.wait.ms`超时了。这样做不好的方面在于，
当出现严重故障时，会增加重复处理的消息的数量。另一个选择是使用异步提交，
此时consumer不会等待offset commit request执行成功，因此不会阻塞。