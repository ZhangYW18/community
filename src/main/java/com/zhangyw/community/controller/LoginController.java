package com.zhangyw.community.controller;

import com.google.code.kaptcha.Producer;
import com.zhangyw.community.common.constant.constant;
import com.zhangyw.community.entity.User;
import com.zhangyw.community.service.UserService;
import com.zhangyw.community.util.CommunityUtil;
import com.zhangyw.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
public class LoginController {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private Producer kaptchaProducer;

    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @RequestMapping(path="/register", method= RequestMethod.GET)
    public String getRegisterPage() {
        return "/site/register";
    }

    @RequestMapping(path = "/login", method = RequestMethod.GET)
    public String getLoginPage() {
        return "/site/login";
    }

    @RequestMapping(path = "/kaptcha", method = RequestMethod.GET)
    public void getKaptcha(HttpServletResponse response, HttpSession session) {
        // Produce kaptcha image and save text to session
        String text = kaptchaProducer.createText();
        BufferedImage image = kaptchaProducer.createImage(text);

        // session.setAttribute("kaptcha", text);
        // 验证码的归属
        String kaptchaOwner = CommunityUtil.generateUUID();
        Cookie cookie = new Cookie("kaptchaOwner", kaptchaOwner);
        cookie.setMaxAge(60);
        cookie.setPath(contextPath);
        response.addCookie(cookie);
        // 将验证码存入Redis
        String redisKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
        redisTemplate.opsForValue().set(redisKey, text, 60, TimeUnit.SECONDS);

        response.setContentType("image/png");
        // Spring MVC automatically closes this stream
        try {
            OutputStream os = response.getOutputStream();
            ImageIO.write(image, "png", os);
        } catch (IOException e) {
            logger.error("Unable to get kaptcha image" + e.getMessage());
        }
    }

    @RequestMapping(path="/register", method= RequestMethod.POST)
    public String register(Model model, User user) {
        try {
            Map<String, Object> map = userService.register(user);

            // invalid situation, return the error message
            if (map!=null && !map.isEmpty()) {
                for (Map.Entry<String, Object> entry : map.entrySet()) {
                    model.addAttribute(entry.getKey(), entry.getValue());
                }
                return "/site/register";
            }

            model.addAttribute("msg", "Register Success! We have sent an activation email to your mailbox.");
            model.addAttribute("target", "/index");
            return "/site/operate-result";
        } catch (IllegalArgumentException e) {
//            logger.error("Caught Invalid Argument Exception");
            logger.error("Unable to process register request:" + e.getMessage());
            model.addAttribute("usernameMsg", "Internal server error, please try again later.");
            return "/site/register";
        }
    }

    @RequestMapping(path="/activation/{userId}/{activationCode}", method = RequestMethod.GET)
    public String activation(Model model, @PathVariable("userId") int userId, @PathVariable("activationCode") String activationCode) {
        int result = userService.activate(userId, activationCode);
        if (result == constant.ACTIVATION_FAILURE) {
            model.addAttribute("target", "/index");
            model.addAttribute("msg", "Activation failed!");
        } else if (result == constant.ACTIVATION_REPEAT) {
            model.addAttribute("target", "/index");
            model.addAttribute("msg", "Activation repeated!");
        } else {
            model.addAttribute("target", "/login");
            model.addAttribute("msg", "Activation Success!");
        }
        return "/site/operate-result";
    }

    @RequestMapping(path = "/login", method = RequestMethod.POST)
    public String login(String username, String password, String code, boolean rememberMe,
                        Model model,
                        @CookieValue("kaptchaOwner") String kaptchaOwner, // HttpSession session,
                        HttpServletResponse response) {
        // 检查验证码
        // String kaptcha = (String) session.getAttribute("kaptcha");
        String kaptcha = null;
        if (StringUtils.isNotBlank(kaptchaOwner)) {
            String redisKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
            kaptcha = (String) redisTemplate.opsForValue().get(redisKey);
        }

        if (StringUtils.isBlank(kaptcha) || StringUtils.isBlank(code) || !kaptcha.equalsIgnoreCase(code)) {
            model.addAttribute("codeMsg", "验证码不正确!");
            return "/site/login";
        }

        // 检查账号,密码
        long expiredSeconds = rememberMe ? constant.REMEMBER_ME_EXPIRED_SECONDS : constant.DEFAULT_LOGIN_EXPIRED_SECONDS;
        Map<String, Object> map = userService.login(username, password, expiredSeconds);
        if (map.containsKey("ticket")) {
            Cookie cookie = new Cookie("ticket", map.get("ticket").toString());
            cookie.setPath(contextPath);
            cookie.setMaxAge((int)expiredSeconds);
            response.addCookie(cookie);
            return "redirect:/index";
        } else {
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            return "/site/login";
        }
    }

    @RequestMapping(path = "/logout", method = RequestMethod.GET)
    public String logout(@CookieValue("ticket") String ticket) {
        userService.logout(ticket);
        SecurityContextHolder.clearContext();
        return "redirect:/login";
    }

}
