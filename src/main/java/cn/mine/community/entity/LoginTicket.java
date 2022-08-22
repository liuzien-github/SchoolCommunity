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
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@TableName("login_ticket")
public class LoginTicket implements Serializable {
    private static final long serialVersionUID = 1267300158788398691L;

    private Integer id;

    private Integer userId;

    private String ticket;

    private Integer status;

    private Date expired;

    @TableField(exist = false)
    private Map<String, Object> attributes = new HashMap<>();
}
