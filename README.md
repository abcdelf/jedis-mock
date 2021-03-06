# Redis-server
Pure Java implementation redis-server. Embedded redis service when unit testing. You no longer need to Mock redis apis.
Support redis api 2.8

## maven dependency

```
<dependency>
    <groupId>com.github.microwww</groupId>
    <artifactId>redis-server</artifactId>
    <version>0.0.1-2.8</version>
    <scope>test</scope>
</dependency>
```
## Using
```
    RedisServer server = new RedisServer();
    server.listener("127.0.0.1", 6379); // Redis runs in the background
    InetSocketAddress address = (InetSocketAddress) server.getServerSocket().getLocalSocketAddress();
    logger.info("Redis start :: [{}:{}]", address.getHostName(), address.getPort());
```
> You can set port to 0, server will bind at **random port**, you can get it by server..getLocalSocketAddress

if you using spring boot, you can start like this :
```
    @Bean
    public RedisServer mockRedisServer(RedisProperties redisProperties) throws IOException {
        RedisServer server = new RedisServer();
        server.listener(redisProperties.getHost(), redisProperties.getPort());
        InetSocketAddress address = (InetSocketAddress) server.getServerSocket().getLocalSocketAddress();
        logger.info("Mocker Redis start :: [{}:{}], set 'server.redis.host' to match it", address.getHostName(), address.getPort());
        return server;
    }

```
server will run with spring config : `spring.redis.host`, `spring.redis.port`

### Supported commands
Supported redis commands :

ConnectionOperation
>  AUTH, ECHO, PING, QUIT, SELECT, 

HashOperation
>  HDEL, HEXISTS, HGET, HGETALL, HINCRBY, HINCRBYFLOAT, HKEYS, HLEN, HMGET, HMSET, HSCAN, HSET, HSETNX, HVALS, 

KeyOperation
>  DEL, EXISTS, EXPIRE, EXPIREAT, KEYS, MOVE, PERSIST, PEXPIRE, PEXPIREAT, PTTL, RANDOMKEY, RENAME, RENAMENX, SCAN, SORT, TTL, TYPE, 

ListOperation
>  BLPOP, BRPOP, LINDEX, LINSERT, LLEN, LPOP, LPUSH, LPUSHX, LRANGE, LREM, LSET, LTRIM, RPOP, RPOPLPUSH, RPUSH, RPUSHX, 

~~PubSubOperation~~

~~ScriptOperation~~

ServerOperation
>  DBSIZE, FLUSHALL, FLUSHDB, TIME, 0.0.2+, CLIENT GETNAME, CLIENT KILL, CLIENT LIST, CLIENT SETNAME,

SetOperation
>  SADD, SCARD, SDIFF, SDIFFSTORE, SINTER, SINTERSTORE, SISMEMBER, SMEMBERS, SMOVE, SPOP, SRANDMEMBER, SREM, SSCAN, SUNION, SUNIONSTORE, 

SortedSetOperation
>  ZADD, ZCARD, ZCOUNT, ZINCRBY, ZINTERSTORE, ZRANGE, ZRANGEBYSCORE, ZRANK, ZREM, ZREMRANGEBYRANK, ZREMRANGEBYSCORE, ZREVRANGE, ZREVRANGEBYSCORE, ZREVRANK, ZSCAN, ZSCORE, ZUNIONSTORE, 

StringOperation
>  APPEND, BITCOUNT, BITOP, DECR, DECRBY, GET, GETBIT, GETRANGE, GETSET, INCR, INCRBY, INCRBYFLOAT, MGET, MSET, MSETNX, PSETEX, SET, SETBIT, SETEX, SETNX, SETRANGE, STRLEN, 

TransactionOperation
> 0.0.2+, DISCARD, EXEC, MULTI, UNWATCH, WATCH

## Unsupported 
If you find unsupported operation, you can add it by yourself, you must `implements AbstractOperation` add it to server :
> `server.configScheme(16, new YourOperation1(), ...)` 

You can add a method, Method signature like this:
 `public void name(RedisRequest request) throws IOException`
Method name is same as the commend, and Method names are all lowercase.

In `RedisRequest` , you can get RedisServer -> Scheme -> database and so on !

You can create a issue also, I will add it as much as I can.

Good luck !
