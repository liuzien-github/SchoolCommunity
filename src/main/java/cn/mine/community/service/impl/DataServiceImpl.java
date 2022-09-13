package cn.mine.community.service.impl;

import cn.mine.community.service.DataService;
import cn.mine.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class DataServiceImpl implements DataService {
    @Autowired
    private RedisTemplate redisTemplate;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

    @Override
    public void recordUV(String ip) {
        String key = RedisKeyUtil.getUVKey(dateFormat.format(new Date()));
        redisTemplate.opsForHyperLogLog().add(key, ip);
    }

    @Override
    public long calculateUV(Date startDate, Date endDate) {
        if (startDate == null || endDate == null)
            throw new IllegalArgumentException("参数都不能为空！");

        List<String> keyList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        while (!calendar.getTime().after(endDate)) {
            String key = RedisKeyUtil.getUVKey(dateFormat.format(calendar.getTime()));
            keyList.add(key);
            calendar.add(Calendar.DATE, 1);
        }

        String key = RedisKeyUtil.getUVKey(dateFormat.format(startDate), dateFormat.format(endDate));
        redisTemplate.opsForHyperLogLog().union(key, keyList.toArray());
        return redisTemplate.opsForHyperLogLog().size(key);
    }

    @Override
    public void recordDAU(int userId) {
        String key = RedisKeyUtil.getDAUKey(dateFormat.format(new Date()));
        redisTemplate.opsForValue().setBit(key, userId, true);
    }

    @Override
    public long calculateDAU(Date startDate, Date endDate) {
        if (startDate == null || endDate == null)
            throw new IllegalArgumentException("参数都不能为空！");

        List<byte[]> keyList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        while (!calendar.getTime().after(endDate)) {
            String key = RedisKeyUtil.getDAUKey(dateFormat.format(calendar.getTime()));
            keyList.add(key.getBytes());
            calendar.add(Calendar.DATE, 1);
        }

        return (long) redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {
                String key = RedisKeyUtil.getDAUKey(dateFormat.format(startDate), dateFormat.format(endDate));
                redisConnection.bitOp(RedisStringCommands.BitOperation.OR, key.getBytes(), keyList.toArray(new byte[0][0]));
                return redisConnection.bitCount(key.getBytes());
            }
        });
    }
}
