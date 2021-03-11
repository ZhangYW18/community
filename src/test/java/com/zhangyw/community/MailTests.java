package com.zhangyw.community;

import com.zhangyw.community.util.MailClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MailTests {

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Test
    public void testMailClient() {
        Context context = new Context();
        context.setVariable("username", "testUserName");
        String content = templateEngine.process("/mail/demo", context);
        mailClient.sendMail("young_dumb_broke@sina.com", "Test Mail", content);
    }
}
