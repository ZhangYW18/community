package com.zhangyw.community.controller.interceptor;

import com.zhangyw.community.entity.LoginTicket;
import com.zhangyw.community.entity.User;
import com.zhangyw.community.service.UserService;
import com.zhangyw.community.util.CookieUtil;
import com.zhangyw.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@Component
public class LoginTicketInterceptor implements HandlerInterceptor {

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // get ticket from cookie
        String ticket = CookieUtil.getValue(request, "ticket");
        if (ticket!=null) {
            LoginTicket loginTicket = userService.findLoginTicket(ticket);
            // check whether the ticket is expired
            if (loginTicket!=null && loginTicket.getStatus()==0 && loginTicket.getExpired().after(new Date())) {
                User user = userService.findUserById(loginTicket.getUserId());
                // put User info in ThreadLocal
                hostHolder.setUser(user);
                // 构建用户认证的结果,并存入SecurityContext,以便于Security进行授权.
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        user, user.getPassword(), user.getAuthorities(user.getType()));
                SecurityContextHolder.setContext(new SecurityContextImpl(authentication));
            }
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        // add user in model
        User user = hostHolder.getUser();
        if (user!=null && modelAndView!=null) {
            modelAndView.addObject("loginUser", user);
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        hostHolder.clear();
    }
}
