package cn.mine.community.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@TableName("message")
public class Message implements Serializable {
    private static final long serialVersionUID = 4266086286582745637L;

    private Integer id;

    private Integer fromId;

    private Integer toId;

    private String conversationId;

    private String content;

    private Integer status;

    private Date createTime;

    @TableField(exist = false)
    private Map<String, Object> attributes = new HashMap<>();
}
