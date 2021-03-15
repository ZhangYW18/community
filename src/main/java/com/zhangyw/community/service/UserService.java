package com.zhangyw.community.service;

import com.zhangyw.community.common.constant.constant;
import com.zhangyw.community.dao.UserMapper;
import com.zhangyw.community.entity.User;
import com.zhangyw.community.util.CommunityUtil;
import com.zhangyw.community.util.MailClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${community.path.domain}")
    private String domain;

    public User findUserById(int id) {
        return userMapper.selectById(id);
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
        if (user.getStatus()==1) {
            return constant.ACTIVATION_REPEAT;
        }
        if (!user.getActivationCode().equals(code)) {
            return constant.ACTIVATION_FAILURE;
        }
        userMapper.updateStatus(userId, 1);
        return constant.ACTIVATION_SUCCESS;
    }
}
