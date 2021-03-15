package com.zhangyw.community.controller;

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

import java.util.Map;

@Controller
public class LoginController {

    @Autowired
    private UserService userService;

//    @Autowired
//    private Logger logger = LoggerFactory.getLogger(LoginController.class);

    @RequestMapping(path="/register", method= RequestMethod.GET)
    public String getRegisterPage() {
        return "/site/register";
    }

    @RequestMapping(path = "/login", method = RequestMethod.GET)
    public String getLoginPage() {
        return "/site/login";
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
        } catch (Exception e) {
//            logger.error("Caught Invalid Argument Exception");
            e.printStackTrace();
            model.addAttribute("usernameMsg", "Internal server error, please try again later.");
            return "/site/register";
        }
    }

    @RequestMapping(path="/activation/{userId}/{activationCode}", method = RequestMethod.GET)
    public String activation(Model model, @PathVariable("userId") int userId, @PathVariable("activationCode") String activationCode) {
        try{
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
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("target", "/index");
            model.addAttribute("msg", "Internal server error, please try again later.");
        }
        return "/site/operate-result";
    }
}
