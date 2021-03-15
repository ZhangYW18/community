package com.zhangyw.community.controller;

import com.google.code.kaptcha.Producer;
import com.zhangyw.community.common.constant.constant;
import com.zhangyw.community.entity.User;
import com.zhangyw.community.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

@Controller
public class LoginController {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private Producer kaptchaProducer;

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
        session.setAttribute("kaptcha", text);
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
}
