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
        int n=10;
        String[] titles = {
                "Zach LaVine preparing for travel calls in the Olympics",
                "Monty mic’d up: You are going to the Finals Chris",
                "Only 5 players from the 2015 Draft are still with their original teams",
                "Breaking News",
                "Bobby Portis lists his favorite players growing up",
                "[Stein] The Lakers have interviewed former Wizards coach",
                "Deandre Ayton",
                "[Peters] NYC judge rips Lamar Odom for skipping child support despite boxing match",
                "[Highlight] Kevin Knox dunks on Ben Simmons",
                "What's a game that a player had that truly epitomizes what their career was all about?",
        };
    String[] contents = {
      "Go USA",
      "The NBA released the mic’d up version of when Chris Paul and Monty Williams embraced at the end of Game 6. A strong, beautiful moment:",
      "It has now been 6 seasons since the 2015 NBA draft, and only 5 players are still with their original teams: #1 Karl-Anthony Towns, #11 Myles Turner, #13 Devin Booker, #30 Kevon Looney, and #31 Cedi Osman.",
      "[Dumas] The Sixers have already fielded offers for Ben Simmons but continue to hold a stance that they will only trade him for an all-star caliber player. They most recently turned down a deal with the Pacers that included Malcolm Brogdon and a 1st round pick.",
      "Bobby Portis lists his favorite players growing up: \"Kevin Garnett, Zach Randolph and Rasheed Wallace.\"",
      "[Stein] The Lakers have interviewed former Wizards coach Scott Brooks for a potential spot on Frank Vogel's staff, league sources say. ESPN reported last week that former Knicks and Grizzlies coach David Fizdale would soon join the Lakers after Jason Kidd's departure to coach Dallas.",
      "Deandre Ayton has held opponents to 37.5% shooting these playoffs, the best out of any player with at least 125 FGA defended. Ayton leads all players with 267 FGA defended in the postseason.",
      "sportsgrindentertainment.com/",
      "Kevin Knox dunks on Ben Simmons",
      "Shaq on Nov 19, 1999 - 41 PTS 17 REB and 19/31 from the line: inside dominance on top of not great free throw shooting\n",
    };
        for (int i=0; i<n; i++) {
            DiscussPost post = new DiscussPost();
            post.setUserId(111);
            post.setTitle(titles[i]);
            post.setContent(contents[i]);
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
