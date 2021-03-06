package com.github.microwww.redis.protocal.operation;

import com.github.microwww.redis.ExpectRedisRequest;
import com.github.microwww.redis.database.Bytes;
import com.github.microwww.redis.database.HashData;
import com.github.microwww.redis.database.HashKey;
import com.github.microwww.redis.protocal.AbstractOperation;
import com.github.microwww.redis.protocal.RedisOutputProtocol;
import com.github.microwww.redis.protocal.RedisRequest;
import com.github.microwww.redis.protocal.ScanIterator;
import com.github.microwww.redis.util.Assert;
import redis.clients.jedis.Protocol;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

public class HashOperation extends AbstractOperation {

    //HDEL
    public void hdel(RedisRequest request) throws IOException {
        request.expectArgumentsCountBigger(1);
        Optional<HashData> data = this.getHashData(request);
        int count = 0;
        if (data.isPresent()) {
            HashData e = data.get();
            ExpectRedisRequest[] args = request.getArgs();
            for (int i = 1; i < args.length; i++) {
                HashKey hk = args[i].byteArray2hashKey();
                Bytes remove = e.remove(hk);
                if (remove != null) {
                    count++;
                }
            }
        }
        RedisOutputProtocol.writer(request.getOutputStream(), count);
    }

    //HEXISTS
    public void hexists(RedisRequest request) throws IOException {
        request.expectArgumentsCount(2);
        ExpectRedisRequest[] args = request.getArgs();
        HashKey hk = args[1].byteArray2hashKey();
        Optional<Map<HashKey, Bytes>> opt = this.getHashMap(request);
        if (opt.isPresent()) {
            boolean exist = opt.get().containsKey(hk);
            if (exist) {
                RedisOutputProtocol.writer(request.getOutputStream(), 1);
                return;
            }
        }
        RedisOutputProtocol.writer(request.getOutputStream(), 0);
    }

    //HGET
    public void hget(RedisRequest request) throws IOException {
        request.expectArgumentsCount(2);
        ExpectRedisRequest[] args = request.getArgs();
        HashKey hk = args[1].byteArray2hashKey();
        Optional<HashData> opt = this.getHashData(request);
        if (opt.isPresent()) {
            Bytes dh = opt.get().getData().get(hk);
            if (dh != null) {
                RedisOutputProtocol.writer(request.getOutputStream(), dh);
                return;
            }
        }
        RedisOutputProtocol.writerNull(request.getOutputStream());
    }

    //HGETALL
    public void hgetall(RedisRequest request) throws IOException {
        request.expectArgumentsCount(1);
        Optional<Map<HashKey, Bytes>> opt = this.getHashMap(request);
        if (opt.isPresent()) {
            // Map<HashKey, byte[]> map = new HashMap<>(opt.get());
            List<byte[]> list = new ArrayList<>();
            opt.get().forEach((k, v) -> {
                list.add(k.getBytes());
                list.add(v.getBytes());
            });
            RedisOutputProtocol.writerMulti(request.getOutputStream(), list.toArray(new byte[list.size()][]));
        } else {
            RedisOutputProtocol.writerMulti(request.getOutputStream()); //  null ?
        }
    }

    //HINCRBY
    public void hincrby(RedisRequest request) throws IOException {
        request.expectArgumentsCount(3);
        HashKey key = request.getArgs()[0].byteArray2hashKey();
        HashKey hk = request.getArgs()[1].byteArray2hashKey();
        int inc = request.getArgs()[2].byteArray2int();
        HashData oc = this.getOrCreate(request);
        Bytes bytes = oc.incrBy(hk, inc);
        RedisOutputProtocol.writer(request.getOutputStream(), bytes.toLong());
    }

    //HINCRBYFLOAT
    public void hincrbyfloat(RedisRequest request) throws IOException {
        request.expectArgumentsCount(3);
        HashKey key = request.getArgs()[0].byteArray2hashKey();
        HashKey hk = request.getArgs()[1].byteArray2hashKey();
        BigDecimal inc = request.getArgs()[2].byteArray2decimal();
        HashData oc = this.getOrCreate(request);
        Bytes bytes = oc.incrByFloat(hk, inc);
        RedisOutputProtocol.writer(request.getOutputStream(), bytes);
    }

    //HKEYS
    public void hkeys(RedisRequest request) throws IOException {
        request.expectArgumentsCount(1);
        Optional<Map<HashKey, Bytes>> map = this.getHashMap(request);
        if (map.isPresent()) {
            byte[][] ks = map.get().keySet().stream().map(HashKey::getBytes).toArray(byte[][]::new);
            RedisOutputProtocol.writerMulti(request.getOutputStream(), ks);
        } else {
            RedisOutputProtocol.writerMulti(request.getOutputStream());
        }
    }

    //HLEN
    public void hlen(RedisRequest request) throws IOException {
        request.expectArgumentsCount(1);
        Optional<Map<HashKey, Bytes>> map = this.getHashMap(request);
        int count = map.map(e -> e.size()).orElse(0);
        RedisOutputProtocol.writer(request.getOutputStream(), count);
    }

    //HMGET
    public void hmget(RedisRequest request) throws IOException {
        request.expectArgumentsCountGE(2);
        ExpectRedisRequest[] args = request.getArgs();
        Optional<Map<HashKey, Bytes>> opt = this.getHashMap(request);
        byte[][] bytes = Arrays.stream(args, 1, args.length)
                .map(e -> e.byteArray2hashKey())
                .map(e -> {//
                    return opt.flatMap(e1 -> Optional.ofNullable(e1.get(e))).map(Bytes::getBytes).orElse(null);
                }).toArray(byte[][]::new);
        RedisOutputProtocol.writerMulti(request.getOutputStream(), bytes); //  null ?
    }

    //HMSET
    public void hmset(RedisRequest request) throws IOException {
        request.expectArgumentsCountGE(3);
        HashData oc = this.getOrCreate(request);
        ExpectRedisRequest[] args = request.getArgs();
        Assert.isTrue(args.length % 2 == 1, "k, k-v, k-v, k-v, ...");
        oc.multiSet(args, 1);
        RedisOutputProtocol.writer(request.getOutputStream(), Protocol.Keyword.OK.name()); //  null ?
    }

    //HSET
    public void hset(RedisRequest request) throws IOException {
        request.expectArgumentsCount(3);
        ExpectRedisRequest[] args = request.getArgs();

        HashData data = this.getOrCreate(request);
        byte[] hk = args[1].getByteArray();
        byte[] val = args[2].getByteArray();

        Bytes origin = data.put(new HashKey(hk), val);

        // new: 1, over-write: 0
        RedisOutputProtocol.writer(request.getOutputStream(), origin == null ? 1 : 0);
    }

    //HSETNX
    public void hsetnx(RedisRequest request) throws IOException {
        request.expectArgumentsCount(3);
        ExpectRedisRequest[] args = request.getArgs();

        HashData data = this.getOrCreate(request);
        byte[] hk = args[1].getByteArray();
        byte[] val = args[2].getByteArray();

        Bytes origin = data.putIfAbsent(new HashKey(hk), val);

        RedisOutputProtocol.writer(request.getOutputStream(), origin == null ? 1 : 0);
    }

    //HVALS
    public void hvals(RedisRequest request) throws IOException {
        request.expectArgumentsCount(1);
        Optional<Map<HashKey, Bytes>> map = this.getHashMap(request);
        if (map.isPresent()) {
            byte[][] ks = map.get().values().stream().map(Bytes::getBytes).toArray(byte[][]::new);
            RedisOutputProtocol.writerMulti(request.getOutputStream(), ks);
        } else {
            RedisOutputProtocol.writerMulti(request.getOutputStream());
        }
    }

    //HSCAN
    public void hscan(RedisRequest request) throws IOException {
        HashData data = this.getHashData(request).orElse(new HashData());
        Set<HashKey> hk = data.getData().keySet();
        Iterator<HashKey> iterator = hk.iterator();
        new ScanIterator<HashKey>(request, 1)
                .skip(iterator)
                .continueWrite(iterator, e -> {// key
                    return e.getBytes();
                }, e -> {// value
                    return data.getData().get(e).getBytes();
                });
    }

    private Optional<Map<HashKey, Bytes>> getHashMap(RedisRequest request) {
        return getHashData(request).map(e -> e.getData());
    }

    private Optional<HashData> getHashData(RedisRequest request) {
        ExpectRedisRequest[] args = request.getArgs();
        HashKey key = args[0].byteArray2hashKey();
        return request.getDatabase().get(key, HashData.class);
    }

    private HashData getOrCreate(RedisRequest request) {
        ExpectRedisRequest[] args = request.getArgs();
        HashKey key = args[0].byteArray2hashKey();
        return request.getDatabase().getOrCreate(key, () -> {//
            return new HashData();
        });
    }
}
