package com.zhangyw.community.common.constant;

public interface constant {

    /**
     * 用户激活状态，0:激活成功, 1:重复激活, 2:激活失败
     */
    int ACTIVATION_SUCCESS = 0;
    int ACTIVATION_REPEAT = 1;
    int ACTIVATION_FAILURE = 2;

    /**
     *  用户登录状态保存时间，分为勾选/不勾选"记住我"
     */
    long REMEMBER_ME_EXPIRED_SECONDS = 3600*24*30;
    long DEFAULT_LOGIN_EXPIRED_SECONDS = 3600*12;

    /**
     *  评论回复对象的类型，0
     */
    int ENTITY_TYPE_POST = 1;
    int ENTITY_TYPE_COMMENT = 2;
    int ENTITY_TYPE_USER = 3;

    /**
     * Kafka主题: 评论/点赞/关注/发帖
     */
    String TOPIC_COMMENT = "comment";
    String TOPIC_LIKE = "like";
    String TOPIC_FOLLOW = "follow";
    String TOPIC_PUBLISH = "publish";
    String TOPIC_DELETE = "delete";

    /**
     * 系统用户ID
     */
    int SYSTEM_USER_ID = 1;

    /**
     * 权限: 用户、管理员、版主
     */
    String AUTHORITY_USER = "user";
    String AUTHORITY_ADMIN = "admin";
    String AUTHORITY_MODERATOR = "moderator";

    /**
     * 帖子默认或置顶
     */
    int DISCUSS_POST_TYPE_DEFAULT = 0;
    int DISCUSS_POST_TYPE_STICKY = 1;

    /**
     * 帖子状态 默认、加精、删除
     */
    int DISCUSS_POST_STATUS_DEFAULT = 0;
    int DISCUSS_POST_STATUS_DIGESTED = 1;
    int DISCUSS_POST_STATUS_DELETED = 2;
}
