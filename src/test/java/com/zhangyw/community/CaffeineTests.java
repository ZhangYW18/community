package com.zhangyw.community;

import com.zhangyw.community.entity.DiscussPost;
import com.zhangyw.community.service.DiscussPostService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class CaffeineTests {

    @Autowired
    private DiscussPostService discussPostService;

    @Test
    public void initTestData() {
        for (int i=0; i<300000; i++) {
            DiscussPost post = new DiscussPost();
            post.setUserId(111);
            post.setTitle("Post Game Thread");
            post.setContent(
                "The Phoenix Suns close out the series 4-2 against the Los Angeles Clippers by a score of 130 - 103 behind 41 points and 8 assists from Chris \"Point God\" Paul and advance to the NBA Finals for the first time since 1993");
            post.setCreateTime(new Date());
            post.setScore(Math.random() * 2000);
            discussPostService.addDiscussPost(post);
        }

    }

    @Test
    public void testCache() {
        for (int i=0; i<3; i++) {
            discussPostService.findDiscussPosts(0, 0, 10, true);
        }
    }

}
