package com.zhangyw.community.dao.elasticsearch;

import com.zhangyw.community.entity.DiscussPost;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Highlight;
import org.springframework.data.elasticsearch.annotations.HighlightField;
import org.springframework.data.elasticsearch.core.SearchPage;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiscussPostRepository extends PagingAndSortingRepository<DiscussPost, Integer> {

    @Highlight(fields = {
            @HighlightField(name = "title"),
            @HighlightField(name = "content")
    })
    SearchPage<DiscussPost> findByTitleOrContentOrderByTypeDescStatusDescCreateTimeDesc(String title, String content, Pageable pageable);
}
