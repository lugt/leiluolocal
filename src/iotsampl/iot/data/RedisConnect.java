package iotsampl.iot.data;

import com.lambdaworks.redis.*;
import com.lambdaworks.redis.api.StatefulRedisConnection;
import com.lambdaworks.redis.api.sync.RedisCommands;
import iotsampl.Constant;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Frapo on 2017/1/31.
 * Version :17
 * Earth - Moudule earth.server.message
 */
public class RedisConnect {

    private static RedisClient client;

    /**
     *
     * DB是这样的：
     * 0 是默认的token-value库
     * 1 是user-tokens（可优化成静态死数据+动态活跃数据,分表）
     * 2 是facial库 user-facialparams
     * 3 是user group
     *
     * 5.
     *
     */

    private static Map<Integer,RedisClient> clients = new HashMap<Integer,RedisClient>();
    private static Map<Integer,StatefulRedisConnection<String,String>> conns = new HashMap<>();

    public RedisConnect(int db) throws NullPointerException{
        if(!clients.containsKey(db)){
            RedisURI redisUri = RedisURI.Builder.redis(Constant.REDIS_SERVER)
                    //.withSsl(true)
                    //.withPassword("authentication")
                    .withDatabase(db)
                    .build();
            client = RedisClient.create(redisUri);
            clients.put(db,client);
        }else {
            client = clients.get(db);
        }
        init(db);
    }

    private RedisCommands<String, String> sync = null;

    public RedisCommands<String,String> init(int db)
            throws NullPointerException, RedisCommandExecutionException,
            RedisConnectionException, RedisCommandInterruptedException,
            RedisCommandTimeoutException{
        //RedisClient client = RedisClient.create("redis://127.0.0.1");

        if(conns.containsKey(db) && conns.get(db).isOpen()){
            connection = conns.get(db);
        }else {
            connection = client.connect();
            conns.put(db,connection);
        }
        sync = connection.sync();
        return sync;
    }

    private StatefulRedisConnection<String, String> connection;

    public String setValue(String a, String b) {
        return sync.set(a, b);
    }

    public String getValue(String a){
        return sync.get(a);
    }

    public static void main(String[] args) throws Exception {
        /*
        RedisConnect connect = new RedisConnect(3);
        for (int i = 2010; i < 2020; i++) {
            connect.setValue(i+"=","3e9+-3ea+-3eb+");
        }
        connect.close();
        */

    }

    public void close() throws Exception {
        connection.close();
        //client.shutdown();
    }

    public Long rpush(String s, String p)throws Exception {
        return sync.rpush(s, p);
    }

    public List<String> lstart(String s, int start, int len)throws Exception{
        return sync.lrange(s, start, len);
    }

    public String lpop(String key) throws Exception{
        return sync.lpop(key);
    }

    public Long remove(String key) throws Exception{
        return sync.del(key);
    }

    public Long lrem(String s, long count,String mId) {
        return sync.lrem(s, count, mId);
    }

    public long zadd(String s, long time, String data) {
        return sync.zadd(s, (double) time, data);
    }


    public List<String> zrange(String s,long start, long end,long len){
        Range rg = Range.create(start, end);
        Limit lmt = Limit.create(0,len);
        List<String> x = sync.zrangebyscore(s, rg, lmt);
        return x;
    }

    public List<String> zrevrange(String s, long start, long end, long len) {
        Range rg = Range.create(start, end);
        Limit lmt = Limit.create(0, len);
        List<String>  x = sync.zrevrangebyscore(s, rg, lmt);
        return x;
    }

    public Long getZcount(String s, long start, long end) {
        Range rg = Range.create(start, end);
        return sync.zcount(s,rg);
    }

    public boolean isConnected() {
        return sync.isOpen();
    }

    public Long zremoveScore(String id, long min, long max) {
        Range rg = Range.create(min,max);
        return sync.zremrangebyscore(id,rg);
    }
}
