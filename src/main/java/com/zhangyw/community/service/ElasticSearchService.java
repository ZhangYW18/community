package com.zhangyw.community.service;

import com.zhangyw.community.dao.elasticsearch.DiscussPostRepository;
import com.zhangyw.community.entity.DiscussPost;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchPage;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ElasticSearchService {

    @Autowired
    private DiscussPostRepository discussPostRepository;

    public void saveDiscussPost(DiscussPost post) {
        discussPostRepository.save(post);
    }

    public void deleteDiscussPost(int id) {
        discussPostRepository.deleteById(id);
    }

    public SearchPage<DiscussPost> searchDiscussPost(String keyword, int pageNumber, int pageSize) {
        SearchPage<DiscussPost> discussPostsPage = discussPostRepository.findByTitleOrContentOrderByTypeDescStatusDescCreateTimeDesc(
                keyword,
                keyword,
                PageRequest.of(pageNumber, pageSize)
        );

        // Highlight keywords included in certain fields
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
        }

        return discussPostsPage;
    }
}
