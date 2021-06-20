package com.zhangyw.community.controller;

import com.zhangyw.community.common.constant.constant;
import com.zhangyw.community.entity.DiscussPost;
import com.zhangyw.community.entity.Page;
import com.zhangyw.community.service.ElasticSearchService;
import com.zhangyw.community.service.LikeService;
import com.zhangyw.community.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchPage;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class SearchController {

    @Autowired
    private ElasticSearchService elasticSearchService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    // search?keyword=xxx
    @RequestMapping(path = "/search", method = RequestMethod.GET)
    public String search(String keyword, Page page, Model model) {
        // Search posts
        SearchPage<DiscussPost> searchResult =
                elasticSearchService.searchDiscussPost(keyword, page.getCurrent() - 1, page.getLimit());
        // Aggregate data
        List<Map<String, Object>> discussPosts = new ArrayList<>();
        if (searchResult != null) {
            for (SearchHit<DiscussPost> postSearchHit : searchResult.getSearchHits()) {

                Map<String, Object> map = new HashMap<>();
                // 帖子
                DiscussPost post = postSearchHit.getContent();
                map.put("post", post);
                // 作者
                map.put("user", userService.findUserById(post.getUserId()));
                // 点赞数量
                map.put("likeCount", likeService.findEntityLikeCount(constant.ENTITY_TYPE_POST, post.getId()));

                discussPosts.add(map);
            }
        }
        model.addAttribute("discussPosts", discussPosts);
        model.addAttribute("keyword", keyword);

        // Set page information
        page.setPath("/search?keyword=" + keyword);
        page.setRows(searchResult == null ? 0 : (int) searchResult.getTotalElements());

        return "/site/search";
    }
}
