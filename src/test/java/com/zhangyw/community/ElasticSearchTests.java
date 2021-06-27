package com.zhangyw.community;

import com.zhangyw.community.dao.DiscussPostMapper;
import com.zhangyw.community.dao.elasticsearch.DiscussPostRepository;
import com.zhangyw.community.entity.DiscussPost;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchPage;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class ElasticSearchTests {

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private DiscussPostRepository discussPostRepository;

    @Test
    public void testInsertList() {
        // discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(0, 0, 1000));
    }

    @Test
    public void testSearchList() {
        SearchPage<DiscussPost> discussPostsPage = discussPostRepository.findByTitleOrContentOrderByTypeDescStatusDescCreateTimeDesc(
                "管理员",
                "管理员",
                PageRequest.of(0, 10)
        );
        System.out.println(discussPostsPage.getTotalElements());
        System.out.println(discussPostsPage.getTotalPages());

        for (SearchHit<DiscussPost> postSearchHit: discussPostsPage.getSearchHits()) {
            DiscussPost post = postSearchHit.getContent();

            List<String> highlightTitle = postSearchHit.getHighlightField("title");
            if (!highlightTitle.isEmpty()) {
                post.setTitle(highlightTitle.get(0));
            }
            List<String> highlightContent = postSearchHit.getHighlightField("content");
            if (!highlightContent.isEmpty()) {
                post.setContent(highlightContent.get(0));
            }

            System.out.println(post);
        }

    }
}
