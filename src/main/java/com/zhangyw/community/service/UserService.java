package com.zhangyw.community.service;

import com.zhangyw.community.common.constant.constant;
import com.zhangyw.community.dao.UserMapper;
import com.zhangyw.community.entity.LoginTicket;
import com.zhangyw.community.entity.User;
import com.zhangyw.community.util.CommunityUtil;
import com.zhangyw.community.util.MailClient;
import com.zhangyw.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private RedisTemplate redisTemplate;

    // @Autowired
    // private LoginTicketMapper loginTicketMapper;

    @Value("${community.path.domain}")
    private String domain;

    public User findUserById(int id) {
        // return userMapper.selectById(id);
        User user = getCache(id);
        if (user == null) {
            user = initCache(id);
        }
        return user;
    }

    public Map<String, Object> register(User user) throws IllegalArgumentException {
        Map<String, Object> map = new HashMap<>();

        // handle invalid situations
        if (user==null) {
            throw new IllegalArgumentException("user cannot be empty");
        }
        if (StringUtils.isBlank(user.getUsername())) {
            map.put("usernameMsg", "username cannot be empty");
            return map;
        }
        if (StringUtils.isBlank(user.getPassword())) {
            map.put("passwordMsg", "password cannot be empty");
            return map;
        }
        if (StringUtils.isBlank(user.getPassword())) {
            map.put("emailMsg", "email cannot be empty");
            return map;
        }
        User u = userMapper.selectByName(user.getUsername());
        if (u!=null) {
            map.put("usernameMsg", "username already exists");
            return map;
        }
        u = userMapper.selectByEmail(user.getEmail());
        if (u!=null) {
            map.put("emailMsg", "email already exists");
            return map;
        }

        // register the user
        user.setSalt(CommunityUtil.generateUUID().substring(0,5));
        user.setPassword(CommunityUtil.md5Encrypt(user.getPassword()+user.getSalt()));
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        userMapper.insertUser(user);

        // send activation email
        Context context = new Context();
        context.setVariable("email", user.getEmail());
        String url = domain + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url", url);
        String content = templateEngine.process("/mail/activation", context);
        mailClient.sendMail(user.getEmail(), "Activate your account", content);

        return map;
    }

    public int activate(int userId, String code) {
        User user = userMapper.selectById(userId);
        // repeat activation
        if (user.getStatus()==1) {
            return constant.ACTIVATION_REPEAT;
        }
        // activation failed
        if (!user.getActivationCode().equals(code)) {
            return constant.ACTIVATION_FAILURE;
        }
        // activation succeed
        userMapper.updateStatus(userId, 1);
        clearCache(userId);
        return constant.ACTIVATION_SUCCESS;
    }

    public Map<String, Object> login(String username, String password, long expiredSeconds) {
        Map<String, Object> map = new HashMap<>();
        // handle invalid situations
        if (StringUtils.isBlank(username)) {
            map.put("usernameMsg", "账号不能为空!");
            return map;
        }
        if (StringUtils.isBlank(password)) {
            map.put("passwordMsg", "密码不能为空!");
            return map;
        }

        // 验证账号
        User user = userMapper.selectByName(username);
        if (user == null) {
            map.put("usernameMsg", "该账号不存在!");
            return map;
        }

        // 验证状态
        if (user.getStatus() == 0) {
            map.put("usernameMsg", "该账号未激活!");
            return map;
        }

        // 验证密码
        password = CommunityUtil.md5Encrypt(password + user.getSalt());
        if (!user.getPassword().equals(password)) {
            map.put("passwordMsg", "密码不正确!");
            return map;
        }

        // 生成登录凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000));
        System.out.println("expiredSeconds" + expiredSeconds);
        System.out.println("time: " + loginTicket.getExpired());
        // loginTicketMapper.insertLoginTicket(loginTicket);
        String redisKey = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
        redisTemplate.opsForValue().set(redisKey, loginTicket);

        map.put("ticket", loginTicket.getTicket());
        return map;
    }

    public void logout(String ticket) {
        // loginTicketMapper.updateStatus(ticket, 1);
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(redisKey);
        loginTicket.setStatus(1);
        redisTemplate.opsForValue().set(redisKey, loginTicket);
    }

    public LoginTicket findLoginTicket(String ticket) {
        // return loginTicketMapper.selectByTicket(ticket);
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        return (LoginTicket) redisTemplate.opsForValue().get(redisKey);
    }

    public int updateHeader(int userId, String headerUrl) {
        int rows = userMapper.updateHeader(userId, headerUrl);
        clearCache(userId);
        return rows;
    }

    public User findUserByName(String username) {
        return userMapper.selectByName(username);
    }

    // 1.优先从缓存中取值
    private User getCache(int userId) {
        String redisKey = RedisKeyUtil.getUserKey(userId);
        return (User) redisTemplate.opsForValue().get(redisKey);
    }

    // 2.取不到时初始化缓存数据
    private User initCache(int userId) {
        User user = userMapper.selectById(userId);
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.opsForValue().set(redisKey, user, 3600, TimeUnit.SECONDS);
        return user;
    }

    // 3.数据变更时清除缓存数据
    private void clearCache(int userId) {
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(redisKey);
    }
}
