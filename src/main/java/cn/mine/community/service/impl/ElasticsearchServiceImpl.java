package cn.mine.community.service.impl;

import cn.mine.community.dao.es.DiscussPostRepository;
import cn.mine.community.entity.DiscussPost;
import cn.mine.community.service.ElasticsearchService;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class ElasticsearchServiceImpl implements ElasticsearchService {
    @Autowired
    private DiscussPostRepository discussPostRepository;

    @Autowired
    private ElasticsearchTemplate template;

    @Override
    public void saveDiscussPost(DiscussPost discussPost) {
        discussPostRepository.save(discussPost);
    }

    @Override
    public void deleteDiscussPostById(int discussPostId) {
        discussPostRepository.deleteById(discussPostId);
    }

    @Override
    public Page<DiscussPost> searchDiscussPostsByPage(String keyWord, int pageNum, int pageSize) {
        SearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery(keyWord, "title", "content"))
                .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                .withPageable(PageRequest.of(pageNum - 1, pageSize))
                .withHighlightFields(
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
                ).build();

        return template.queryForPage(searchQuery, DiscussPost.class, new SearchResultMapper() {
            @Override
            public <T> AggregatedPage<T> mapResults(SearchResponse searchResponse, Class<T> aClass, Pageable pageable) {
                SearchHits hits = searchResponse.getHits();
                if (hits == null || hits.getTotalHits() <= 0)
                    return null;

                List<DiscussPost> list = new ArrayList<>();
                for (SearchHit hit : hits) {
                    Map<String, Object> sourceAsMap = hit.getSourceAsMap();
                    String id = sourceAsMap.get("id").toString();
                    String userId = sourceAsMap.get("userId").toString();
                    String title = sourceAsMap.get("title").toString();
                    String content = sourceAsMap.get("content").toString();
                    String status = sourceAsMap.get("status").toString();
                    String createTime = sourceAsMap.get("createTime").toString();
                    String commentCount = sourceAsMap.get("commentCount").toString();

                    DiscussPost discussPost = new DiscussPost()
                            .setId(Integer.parseInt(id))
                            .setUserId(Integer.parseInt(userId))
                            .setTitle(title)
                            .setContent(content)
                            .setStatus(Integer.parseInt(status))
                            .setCommentCount(Integer.parseInt(commentCount))
                            .setCreateTime(new Date(Long.parseLong(createTime)));

                    HighlightField titleField = hit.getHighlightFields().get("title");
                    if (titleField != null) {
                        discussPost.setTitle(titleField.getFragments()[0].toString());
                    }
                    HighlightField contentField = hit.getHighlightFields().get("content");
                    if (contentField != null) {
                        discussPost.setContent(contentField.getFragments()[0].toString());
                    }

                    list.add(discussPost);
                }

                return new AggregatedPageImpl(list, pageable, hits.getTotalHits(), searchResponse.getAggregations(),
                        searchResponse.getScrollId(), hits.getMaxScore());
            }
        });
    }
}
