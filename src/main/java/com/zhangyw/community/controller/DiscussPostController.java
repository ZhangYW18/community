package com.zhangyw.community.controller;

import com.zhangyw.community.common.constant.constant;
import com.zhangyw.community.entity.*;
import com.zhangyw.community.event.EventProducer;
import com.zhangyw.community.service.CommentService;
import com.zhangyw.community.service.DiscussPostService;
import com.zhangyw.community.service.LikeService;
import com.zhangyw.community.service.UserService;
import com.zhangyw.community.util.HostHolder;
import com.zhangyw.community.util.JsonUtil;
import com.zhangyw.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

@Controller
@RequestMapping("/discuss")
public class DiscussPostController {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private RedisTemplate redisTemplate;

    @RequestMapping(path = "/add", method = RequestMethod.POST)
    @ResponseBody
    public String addDiscussPost(String title, String content) {
        User user = hostHolder.getUser();
        if (user == null) {
            return JsonUtil.getJSONString(403, "You have not logged in!");
        }

        DiscussPost post = new DiscussPost();
        post.setUserId(user.getId());
        post.setTitle(title);
        post.setContent(content);
        post.setCreateTime(new Date());
        discussPostService.addDiscussPost(post);

        // fire an event and save the post in ElasticSearch later
        Event event = new Event()
                .setTopic(constant.TOPIC_PUBLISH)
                .setUserId(user.getId())
                .setEntityType(constant.ENTITY_TYPE_POST)
                .setEntityId(post.getId());
        eventProducer.fireEvent(event);

        // ??????????????????
        String redisKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(redisKey, post.getId());

        return JsonUtil.getJSONString(0, "Add Post Success!");
    }

    @RequestMapping(path = "/detail/{discussPostId}", method = RequestMethod.GET)
    public String getDiscussPost(@PathVariable("discussPostId") int discussPostId, Model model, Page page) {
        // ??????
        DiscussPost post = discussPostService.findDiscussPostById(discussPostId);
        if (post.getStatus() == constant.DISCUSS_POST_STATUS_DELETED) {
            return "redirect:/denied";
        }
        model.addAttribute("post", post);
        // ??????
        User user = userService.findUserById(post.getUserId());
        model.addAttribute("user", user);
        // ????????????
        long likeCount = likeService.findEntityLikeCount(constant.ENTITY_TYPE_POST, discussPostId);
        model.addAttribute("likeCount", likeCount);
        // ????????????
        int likeStatus = hostHolder.getUser() == null ? 0 :
                likeService.findEntityLikeStatus(hostHolder.getUser().getId(), constant.ENTITY_TYPE_POST, discussPostId);
        model.addAttribute("likeStatus", likeStatus);

        // ??????????????????
        page.setLimit(5);
        page.setPath("/discuss/detail/" + discussPostId);
        page.setRows(post.getCommentCount());

        // ??????: ??????????????????
        // ??????: ??????????????????
        // ????????????
        List<Comment> commentList = commentService.findCommentsByEntity(
                constant.ENTITY_TYPE_POST, post.getId(), page.getOffset(), page.getLimit());
        // ??????VO??????
        List<Map<String, Object>> commentVoList = new ArrayList<>();
        if (commentList != null) {
            for (Comment comment : commentList) {
                // ??????VO
                Map<String, Object> commentVo = new HashMap<>();
                // ??????
                commentVo.put("comment", comment);
                // ??????
                commentVo.put("user", userService.findUserById(comment.getUserId()));
                // ????????????
                likeCount = likeService.findEntityLikeCount(constant.ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("likeCount", likeCount);
                // ????????????
                likeStatus = hostHolder.getUser() == null ? 0 :
                        likeService.findEntityLikeStatus(hostHolder.getUser().getId(), constant.ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("likeStatus", likeStatus);

                // ????????????
                List<Comment> replyList = commentService.findCommentsByEntity(
                        constant.ENTITY_TYPE_COMMENT, comment.getId(), 0, Integer.MAX_VALUE);
                // ??????VO??????
                List<Map<String, Object>> replyVoList = new ArrayList<>();
                if (replyList != null) {
                    for (Comment reply : replyList) {
                        Map<String, Object> replyVo = new HashMap<>();
                        // ??????
                        replyVo.put("reply", reply);
                        // ??????
                        replyVo.put("user", userService.findUserById(reply.getUserId()));
                        // ????????????
                        User target = reply.getTargetId() == 0 ? null : userService.findUserById(reply.getTargetId());
                        replyVo.put("target", target);
                        // ????????????
                        likeCount = likeService.findEntityLikeCount(constant.ENTITY_TYPE_COMMENT, reply.getId());
                        replyVo.put("likeCount", likeCount);
                        // ????????????
                        likeStatus = hostHolder.getUser() == null ? 0 :
                                likeService.findEntityLikeStatus(hostHolder.getUser().getId(), constant.ENTITY_TYPE_COMMENT, reply.getId());
                        replyVo.put("likeStatus", likeStatus);

                        replyVoList.add(replyVo);
                    }
                }
                commentVo.put("replys", replyVoList);

                // ????????????
                int replyCount = commentService.findCommentCount(constant.ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("replyCount", replyCount);

                commentVoList.add(commentVo);
            }
        }

        model.addAttribute("comments", commentVoList);

        return "/site/discuss-detail";
    }

    // ??????
    @RequestMapping(path = "/sticky", method = RequestMethod.POST)
    @ResponseBody
    public String setSticky(int id, int postType) {
        int newType = (postType == constant.DISCUSS_POST_TYPE_DEFAULT) ? constant.DISCUSS_POST_TYPE_STICKY : constant.DISCUSS_POST_TYPE_DEFAULT;
        discussPostService.updateType(id, newType);

        Map<String, Object> map = new HashMap<>();
        map.put("type", newType);

        // ??????????????????
        Event event = new Event()
                .setTopic(constant.TOPIC_PUBLISH)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(constant.ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);

        return JsonUtil.getJSONString(0, null, map);
    }

    // ??????
    @RequestMapping(path = "/digested", method = RequestMethod.POST)
    @ResponseBody
    public String setDigested(int id, int postStatus) {
        int newStatus = (postStatus == constant.DISCUSS_POST_STATUS_DEFAULT) ? constant.DISCUSS_POST_STATUS_DIGESTED : constant.DISCUSS_POST_STATUS_DEFAULT;
        discussPostService.updateStatus(id, newStatus);

        Map<String, Object> map = new HashMap<>();
        map.put("status", newStatus);

        // ??????????????????
        Event event = new Event()
                .setTopic(constant.TOPIC_PUBLISH)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(constant.ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);

        // ??????????????????
        String redisKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(redisKey, id);

        return JsonUtil.getJSONString(0, null, map);
    }

    // ??????
    @RequestMapping(path = "/delete", method = RequestMethod.POST)
    @ResponseBody
    public String setDelete(int id) {
        discussPostService.updateStatus(id, constant.DISCUSS_POST_STATUS_DELETED);

        // ??????????????????
        Event event = new Event()
                .setTopic(constant.TOPIC_DELETE)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(constant.ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);

        return JsonUtil.getJSONString(0);
    }

}
