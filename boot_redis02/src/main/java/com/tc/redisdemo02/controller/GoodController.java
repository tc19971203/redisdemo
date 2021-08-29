package com.tc.redisdemo02.controller;

import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
public class GoodController {

    public static final String REDIS_LOCK = "redisLock";

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Value("${server.port}")
    private  String serverPort;

    @Autowired
    private Redisson redisson;

    @GetMapping("/buy_Goods")
    public String buy_Goods() throws Exception {

        String value = UUID.randomUUID().toString()+Thread.currentThread().getName();

        RLock redissonLock = redisson.getLock(REDIS_LOCK);

        redissonLock.lock();
        try {
            String result = stringRedisTemplate.opsForValue().get("goods:001");
            int goodNumber = result == null ? 0 : Integer.parseInt(result);

            if(goodNumber > 0){
                int realNumber = goodNumber - 1;
                stringRedisTemplate.opsForValue().set("goods:001",String.valueOf(realNumber));
                System.out.println("成功买到商品，库存还剩下："+realNumber+"件"+"\t 服务提供端口 "+serverPort);
                return "成功买到商品，库存还剩下："+realNumber+"件"+"\t 服务提供端口 "+serverPort;
            }else {
                System.out.println("商品已经售完，欢迎下次光临 "+"\t 服务提供端口 "+serverPort);
                return "商品已经售完，欢迎下次光临 "+"\t 服务提供端口 "+serverPort;
            }
        } finally {
            if(redissonLock.isLocked()){
                if(redissonLock.isHeldByCurrentThread()){
                    redissonLock.unlock();
                }
            }
        }
    }

}