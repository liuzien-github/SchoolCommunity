package cn.mine.community.quartz;

import cn.mine.community.entity.DiscussPost;
import cn.mine.community.service.DiscussPostService;
import cn.mine.community.service.ElasticsearchService;
import cn.mine.community.service.LikeService;
import cn.mine.community.util.ConstantUtil;
import cn.mine.community.util.RedisKeyUtil;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
public class DiscussPostScoreRefreshJob implements Job {
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private ElasticsearchService elasticsearchService;

    private static final Date epoch;

    static {
        try {
            epoch = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2022-08-30 00:00:00");
        } catch (ParseException e) {
            throw new RuntimeException("初始化网站纪元失败！");
        }
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        String key = RedisKeyUtil.getPostScoreKey();
        BoundSetOperations operations = redisTemplate.boundSetOps(key);

        if (operations.size() == 0) {
            log.info("[任务取消] 没有需要刷新的帖子！");
            return;
        }

        log.info("[任务开始] 正在刷新帖子的分数，需要刷新的帖子的数量为：" + operations.size());
        while (operations.size() > 0) {
            refresh((Integer) operations.pop());
        }
        log.info("[任务结束] 帖子分数刷新完毕！");
    }

    private void refresh(int discussPostId) {
        DiscussPost discussPost = discussPostService.findDiscussPostById(discussPostId);
        if (discussPost == null)
            return;

        boolean wonderful = discussPost.getStatus() == 1;
        int commentCount = discussPost.getCommentCount();
        long likeCount = likeService.findEntityLikeCount(ConstantUtil.ENTITY_TYPE_POST, discussPostId);

        double weight = (wonderful ? 75 : 0) + commentCount * 10 + likeCount * 2;
        double score = Math.log10(Math.max(weight, 1)) +
                (discussPost.getCreateTime().getTime() - epoch.getTime()) / (1000 * 3600 * 24);

        discussPostService.updateDiscussPostScoreById(discussPostId, score);
        elasticsearchService.saveDiscussPost(discussPost.setScore(score));
    }
}
