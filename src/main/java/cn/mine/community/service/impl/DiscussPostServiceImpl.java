package cn.mine.community.service.impl;

import cn.mine.community.dao.DiscussPostMapper;
import cn.mine.community.entity.DiscussPost;
import cn.mine.community.service.DiscussPostService;
import cn.mine.community.util.RedisKeyUtil;
import cn.mine.community.util.SensitiveWordsFilter;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class DiscussPostServiceImpl implements DiscussPostService {
    @Value("${community.caffeine.posts.first-max-size}")
    private int firstMaxSize;

    @Value("${community.caffeine.posts.second-max-size}")
    private int secondMaxSize;

    @Value("${community.caffeine.posts.first-expire-seconds}")
    private int firstExpireSeconds;

    @Value("${community.caffeine.posts.second-expire-seconds}")
    private int secondExpireSeconds;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private SensitiveWordsFilter filter;

    @Autowired
    private RedisTemplate redisTemplate;

    //Caffeine的核心接口: cache, LoadingCache（同步缓存）, AysnLoadingCache（异步缓存）
    private LoadingCache<String, PageInfo<DiscussPost>> postsPageInfoCache;

    @PostConstruct
    public void init() {
        postsPageInfoCache = Caffeine.newBuilder()
                .maximumSize(firstMaxSize)
                .expireAfterWrite(firstExpireSeconds, TimeUnit.SECONDS)
                .build(new CacheLoader<String, PageInfo<DiscussPost>>() {
                    @Override
                    public PageInfo<DiscussPost> load(String s) throws Exception {
                        if (s == null || s.length() == 0)
                            throw new IllegalArgumentException("参数错误！");

                        String[] params = s.split(":");
                        if (params == null || params.length != 2)
                            throw new IllegalArgumentException("参数错误！");

                        int pageNum = Integer.parseInt(params[0]);
                        int pageSize = Integer.parseInt(params[1]);

                        //也可以在这里设置二级缓存即Redis缓存，先从Redis缓存中查询数据，如果没有再从数据库中查
                        String key = RedisKeyUtil.getPostsPageInfoCacheKey(pageNum, pageSize);
                        PageInfo<DiscussPost> pageInfo = (PageInfo<DiscussPost>) redisTemplate.opsForValue().get(key);
                        if (pageInfo != null) {
                            log.debug("Load DiscussPostsPageInfo From Redis!");
                            return pageInfo;
                        }

                        log.debug("Load DiscussPostsPageInfo From DB!");
                        QueryWrapper<DiscussPost> queryWrapper = new QueryWrapper<>();
                        queryWrapper.orderByDesc("type").orderByDesc("score");
                        PageHelper.startPage(pageNum, pageSize);
                        List<DiscussPost> discussPosts = discussPostMapper.selectList(queryWrapper);
                        pageInfo = new PageInfo<>(discussPosts);
                        redisTemplate.opsForValue().set(key, pageInfo, secondExpireSeconds, TimeUnit.SECONDS);
                        return pageInfo;
                    }
                });
    }

    @Override
    public PageInfo<DiscussPost> selectDiscussPostsByPage(int userId, int pageNum, int pageSize, int orderMode) {
        if (userId == 0 && orderMode == 1)
            return postsPageInfoCache.get(pageNum + ":" + pageSize);

        log.debug("Load DiscussPostsPageInfo From DB!");

        QueryWrapper<DiscussPost> queryWrapper = new QueryWrapper<>();
        if (orderMode == 0)
            queryWrapper.orderByDesc("type").orderByDesc("id");
        else
            queryWrapper.orderByDesc("type").orderByDesc("score");
        if (userId > 0)
            queryWrapper.eq("user_id", userId);

        PageHelper.startPage(pageNum, pageSize);
        List<DiscussPost> discussPosts = discussPostMapper.selectList(queryWrapper);
        return new PageInfo<>(discussPosts);
    }

    @Override
    public int displayDiscussPost(DiscussPost discussPost) {
        if (discussPost == null) {
            throw new IllegalArgumentException("参数不能为空！");
        }

        discussPost.setTitle(HtmlUtils.htmlEscape(discussPost.getTitle()));
        discussPost.setContent(HtmlUtils.htmlEscape(discussPost.getContent()));
        discussPost.setTitle(filter.filter(discussPost.getTitle()));
        discussPost.setContent(filter.filter(discussPost.getContent()));

        return discussPostMapper.insert(discussPost);
    }

    @Override
    public DiscussPost findDiscussPostById(int discussPostId) {
        return discussPostMapper.selectById(discussPostId);
    }

    @Override
    public int updateDiscussPostTypeById(int discussPostId, int type) {
        return discussPostMapper.updateById(new DiscussPost().setId(discussPostId).setType(type));
    }

    @Override
    public int updateDiscussPostStatusById(int discussPostId, int status) {
        return discussPostMapper.updateById(new DiscussPost().setId(discussPostId).setStatus(status));
    }

    @Override
    public int deleteDiscussPostById(int discussPostId) {
        return discussPostMapper.deleteById(discussPostId);
    }

    @Override
    public int updateDiscussPostScoreById(int discussPostId, double score) {
        return discussPostMapper.updateById(new DiscussPost().setId(discussPostId).setScore(score));
    }
}
