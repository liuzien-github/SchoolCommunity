package cn.mine.community.util;

public interface ConstantUtil {
    int ACTIVATION_REPEAT = 0;
    int ACTIVATION_SUCCESS = 1;
    int ACTIVATION_FAILURE = 2;
    int ACTIVATION_UNREGISTER = 3;

    int ENTITY_TYPE_POST = 1;
    int ENTITY_TYPE_COMMENT = 2;
    int ENTITY_TYPE_USER = 3;

    String TOPIC_COMMENT = "comment";
    String TOPIC_LIKE = "like";
    String TOPIC_FOLLOW = "follow";
    String TOPIC_PUBLISH = "publish";
    String TOPIC_DELETE = "delete";

    int SYSTEM_USER_ID = 1;
}
