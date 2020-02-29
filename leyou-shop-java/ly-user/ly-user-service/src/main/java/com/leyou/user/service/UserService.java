package com.leyou.user.service;

import com.leyou.common.enums.ExceptionEnums;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.NumberUtils;
import com.leyou.user.mapper.UserMapper;
import com.leyou.user.pojo.User;
import com.leyou.user.utils.CodecUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class UserService {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AmqpTemplate amqpTemplate;
    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String KEY_PREFIX = "user:verify:phone:";
    public Boolean checkData(String data, Integer type) {
        User user = new User();

        switch (type){
            case 1:
                user.setUsername(data);
                break;
            case 2:
                user.setPhone(data);
                break;
            default:
                throw new LyException(ExceptionEnums.USER_DATA_TYPE_ERROR);
        }
        int count = userMapper.selectCount(user);
        return count == 0;
    }

    public void sendCode(String phone) {
        // 生成key
        String key = KEY_PREFIX + phone;
        // 生成验证码
        String code = NumberUtils.generateCode(6);
        Map<String, String> msg = new HashMap<>();
        msg.put("phone", phone);
        msg.put("code", code);
        // 发送验证码
        amqpTemplate.convertAndSend("ly.sms.exchange", "sms.verify.code", msg);
        // 保存验证码
        redisTemplate.opsForValue().set(key,code,5, TimeUnit.MINUTES);
    }

    public void register(User user, String code) {
        // 校验验证码, 从redis中取出验证码
        String cacheCode = redisTemplate.opsForValue().get(KEY_PREFIX + user.getPhone());
        if (!StringUtils.equals(code, cacheCode)) {
            throw new LyException(ExceptionEnums.INVALID_VERIFY_CODE);
        }
        // 生成盐
        String salt = CodecUtils.generateSalt();
        user.setSalt(salt);
        // 对密码加密
        user.setPassword(CodecUtils.md5Hex(user.getPassword(), salt));
        // 写入数据库
        user.setCreated(new Date());
        userMapper.insert(user);
    }
    public User queryUserByUsernameAndPassword(String username, String password) {
        User record = new User();
        record.setUsername(username);
        User user = userMapper.selectOne(record);
        if (user == null) {
            throw new LyException(ExceptionEnums.INVALID_USERNAME_PASSWORD);
        }
        // 校验密码
        if (!StringUtils.equals(user.getPassword(), CodecUtils.md5Hex(password, user.getSalt()))) {
            throw new LyException(ExceptionEnums.INVALID_USERNAME_PASSWORD);
        }
        return user;
    }
}
