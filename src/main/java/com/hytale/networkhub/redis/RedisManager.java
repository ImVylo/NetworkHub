package com.hytale.networkhub.redis;

import com.google.gson.Gson;
import com.hytale.networkhub.config.RedisConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class RedisManager {
    private final Logger logger;
    private final RedisConfig config;
    private final Gson gson;
    private JedisPool jedisPool;
    private final Map<String, JedisPubSub> subscribers = new ConcurrentHashMap<>();
    private final Map<String, Thread> subscriberThreads = new ConcurrentHashMap<>();

    public RedisManager(Logger logger, RedisConfig config, Gson gson) {
        this.logger = logger;
        this.config = config;
        this.gson = gson;
    }

    public void initialize() {
        if (!config.getConfig().enabled) {
            logger.info("Redis is disabled in config");
            return;
        }

        logger.info("Initializing Redis connection...");

        JedisPoolConfig poolConfig = new JedisPoolConfig();
        RedisConfig.PoolConfig pool = config.getConfig().pool;
        poolConfig.setMaxTotal(pool.maxTotal);
        poolConfig.setMaxIdle(pool.maxIdle);
        poolConfig.setMinIdle(pool.minIdle);
        poolConfig.setTestOnBorrow(pool.testOnBorrow);
        poolConfig.setTestOnReturn(pool.testOnReturn);
        poolConfig.setTestWhileIdle(pool.testWhileIdle);

        String password = config.getConfig().password;
        if (password != null && !password.isEmpty()) {
            jedisPool = new JedisPool(poolConfig,
                config.getConfig().host,
                config.getConfig().port,
                2000, // timeout
                password,
                config.getConfig().database);
        } else {
            jedisPool = new JedisPool(poolConfig,
                config.getConfig().host,
                config.getConfig().port,
                2000, // timeout
                null, // no password
                config.getConfig().database);
        }

        // Test connection
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.ping();
            logger.info("Redis connection successful");
        } catch (Exception e) {
            logger.severe("Failed to connect to Redis: " + e.getMessage());
            throw new RuntimeException("Redis connection failed", e);
        }
    }

    public void publish(String channel, Object message) {
        if (!isEnabled()) return;

        try (Jedis jedis = jedisPool.getResource()) {
            String json = gson.toJson(message);
            jedis.publish(channel, json);
        } catch (Exception e) {
            logger.warning("Failed to publish to Redis channel " + channel + ": " + e.getMessage());
        }
    }

    public void subscribe(String channel, Consumer<String> handler) {
        if (!isEnabled()) return;

        JedisPubSub pubSub = new JedisPubSub() {
            @Override
            public void onMessage(String ch, String message) {
                try {
                    handler.accept(message);
                } catch (Exception e) {
                    logger.severe("Error handling Redis message on channel " + ch + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }

            @Override
            public void onSubscribe(String channel, int subscribedChannels) {
                logger.info("Subscribed to Redis channel: " + channel);
            }

            @Override
            public void onUnsubscribe(String channel, int subscribedChannels) {
                logger.info("Unsubscribed from Redis channel: " + channel);
            }
        };

        Thread thread = new Thread(() -> {
            try (Jedis jedis = jedisPool.getResource()) {
                jedis.subscribe(pubSub, channel);
            } catch (Exception e) {
                logger.severe("Redis subscription error on channel " + channel + ": " + e.getMessage());
            }
        }, "Redis-Sub-" + channel);

        thread.setDaemon(true);
        thread.start();

        subscribers.put(channel, pubSub);
        subscriberThreads.put(channel, thread);
    }

    public void unsubscribe(String channel) {
        JedisPubSub pubSub = subscribers.remove(channel);
        if (pubSub != null && pubSub.isSubscribed()) {
            pubSub.unsubscribe(channel);
        }

        Thread thread = subscriberThreads.remove(channel);
        if (thread != null && thread.isAlive()) {
            thread.interrupt();
        }
    }

    public String getChannel(String channelName) {
        return config.getConfig().channels.getOrDefault(channelName, channelName);
    }

    public boolean isEnabled() {
        return config.getConfig().enabled && jedisPool != null;
    }

    public void close() {
        logger.info("Closing Redis connections...");

        // Unsubscribe from all channels
        subscribers.forEach((channel, pubSub) -> {
            if (pubSub.isSubscribed()) {
                pubSub.unsubscribe();
            }
        });

        // Interrupt all subscriber threads
        subscriberThreads.values().forEach(Thread::interrupt);

        if (jedisPool != null && !jedisPool.isClosed()) {
            jedisPool.close();
            logger.info("Redis connection pool closed");
        }
    }
}
