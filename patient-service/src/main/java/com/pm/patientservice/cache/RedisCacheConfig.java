package com.pm.patientservice.cache;

import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
@EnableCaching
public class RedisCacheConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        ObjectMapper  objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());  // This is a plugin required to serialize the java object so that it can be stored in Redis.
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); //This disables the spring-redis to store the time stamp in epoch format and then it will get stored in the format of ISO-8601
        objectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                As.PROPERTY
        );   //  Whenever we serialize any complex object this code helps jackson to store the type information of different data-fields with the same serialized object so that later at the time of reading the same object jackson can successfully deserialize it.

        // Creating Redis-serializer
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        //Configuration
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))  // This exists to insert property of storing the data present only till 10 minutes
                .disableCachingNullValues() // This exists only to tell to disable property of storing null values if they come
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer())) //This to serialize "key" value
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer)); // This to serialize "value" value

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .build();
    }
}
