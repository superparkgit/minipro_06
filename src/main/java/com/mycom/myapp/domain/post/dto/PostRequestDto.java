package com.mycom.myapp.domain.post.dto;

import com.mycom.myapp.domain.post.entity.Category;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PostRequestDto {
    private Category category;
    private String title;
    private String content;
}
