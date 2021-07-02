package com.zhangyw.community.service;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.zhangyw.community.dao.DiscussPostMapper;
import com.zhangyw.community.entity.DiscussPost;
import com.zhangyw.community.util.SensitiveFilter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class DiscussPostService {

    private static final Logger logger = LoggerFactory.getLogger(DiscussPostService.class);

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${caffeine.posts.max-size}")
    private int maxLocalCacheSize;

    @Value("${caffeine.posts.expire-seconds}")
    private int expireSeconds;

    private LoadingCache<String, List<DiscussPost>> postListCache;

    @PostConstruct
    public void init() {
        // initiate postListCache
        postListCache = Caffeine.newBuilder()
                .maximumSize(maxLocalCacheSize)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .build(new CacheLoader<String, List<DiscussPost>>() {
                    @Nullable
                    @Override
                    public List<DiscussPost> load(@NonNull String key) throws Exception {
                        if (key.length() == 0) {
                            throw new IllegalArgumentException("Caffeine: argument is illegal: Cache key is empty when Loading post list");
                        }
                        String[] params = key.split(":");
                        if (params.length != 2) {
                            throw new IllegalArgumentException("Caffeine: argument is illegal: Cache key is invalid when Loading post list");
                        }
                        int offset = Integer.parseInt(params[0]);
                        int limit = Integer.parseInt(params[1]);

                        return discussPostMapper.selectDiscussPosts(0, offset, limit, true);
                    }
                });
    }

    public List<DiscussPost> findDiscussPosts(int userId, int offset, int limit, boolean orderWithScore) {
        if (userId==0 && orderWithScore) {
            logger.debug("loading data from cache...");
            return postListCache.get(offset + ":" + limit);
        }
        logger.debug("loading data from database...");
        return discussPostMapper.selectDiscussPosts(userId, offset, limit, orderWithScore);
    }

    public int findDiscussPostRows(int userId) {
        return discussPostMapper.selectDiscussPostRows(userId);
    }

    public int addDiscussPost(DiscussPost post) {
        if (post == null) {
            throw new IllegalArgumentException("参数不能为空!");
        }

        // 转义HTML标记
        post.setTitle(HtmlUtils.htmlEscape(post.getTitle()));
        post.setContent(HtmlUtils.htmlEscape(post.getContent()));
        // 过滤敏感词
        post.setTitle(sensitiveFilter.filter(post.getTitle()));
        post.setContent(sensitiveFilter.filter(post.getContent()));

        return discussPostMapper.insertDiscussPost(post);
    }

    public DiscussPost findDiscussPostById(int id) {
        return discussPostMapper.selectDiscussPostById(id);
    }

    public int updateCommentCount(int id, int commentCount) {
        return discussPostMapper.updateCommentCount(id, commentCount);
    }

    public int updateType(int id, int type) {
        return discussPostMapper.updateType(id, type);
    }

    public int updateStatus(int id, int status) {
        return discussPostMapper.updateStatus(id, status);
    }

    public int updateScore(int id, double score) {
        return discussPostMapper.updateScore(id, score);
    }
}
