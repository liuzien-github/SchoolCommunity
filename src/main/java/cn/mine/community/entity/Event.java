package cn.mine.community.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class Event implements Serializable {
    private static final long serialVersionUID = -7988278191410831938L;

    private String topic;

    private int userId;

    private int entityType;

    private int entityId;

    private int entityUserId;

    @TableField(exist = false)
    private Map<String, Object> attributes = new HashMap<>();
}
