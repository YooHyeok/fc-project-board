package com.fastcampus.projectboard.controller;

import com.fastcampus.projectboard.config.SecurityConfig;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("View 컨트롤러 - 게시글")
@Import(SecurityConfig.class) // Security 설정 전체 허용 적용
@WebMvcTest(ArticleController.class) //정석적인 WebMvcTest - 별도의 어노테이션 설정 없이 MockMvc 사용 가능
class ArticleControllerTest {
    private final MockMvc mvc;

    /**
     * 테스트 패키지에 있는 생성자는 @Autowired 생략이 불가능하다.
     * @param mvc
     */
    ArticleControllerTest(@Autowired MockMvc mvc) {
        this.mvc = mvc;
    }

    @DisplayName("[view][GET] 게시글 리스트 (게시판) 페이지 - 정상 호출")
    @Test
    public void givenNothing_whenRequestingArticlesView_thenReturnsArticlesView() throws Exception {
        mvc.perform(get("/articles"))
                .andExpect(status().isOk()) //status 200 여부
//                .andExpect(content().contentType(MediaType.TEXT_HTML)) //내용의 타입 view이므로 TEXT-HTML인지 여부
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML)) //호환되는 타입까지 적용시켜주는 옵션이 있다 ex) 스프링부트에서 추가시켜주는 charset=UTF-8
                .andExpect(view().name("articles/index")) //해당 경로에 view가 있어야함
                /* 해당 뷰는 데이터가 있어야한다. 게시글 목록이 출력된다는것은 서버에서 게시글 목록을 ModelAttribute로 view에 넘겨줬다는 의미 */
                .andExpect(model().attributeExists("articles")); //모델 애트리뷰트맵에 해당이름의 키가 있는지 여부
    }

    @DisplayName("[view][GET] 게시글 상세 페이지 - 정상 호출")
    @Test
    public void givenNothing_whenRequestingArticleView_thenReturnsArticleView() throws Exception {
        mvc.perform(get("/articles/1"))
                .andExpect(status().isOk()) //status 200 여부
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(view().name("articles/detail"))
                .andExpect(model().attributeExists("article"))
                .andExpect(model().attributeExists("articleComments")); // 댓글 ModelAttribute 야부 확인
    }

    @Disabled("구현중")
    @DisplayName("[view][GET] 게시글 검색 전용 페이지 - 정상 호출")
    @Test
    public void givenNothing_whenRequestingArticleSearchView_thenReturnsArticleSearchView() throws Exception {
        mvc.perform(get("/articles/search"))
                .andExpect(status().isOk()) //status 200 여부
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(view().name("articles/search"));

    }

    @Disabled("구현중")
    @DisplayName("[view][GET] 게시글 해시태그 검색 페이지 - 정상 호출")
    @Test
    public void givenNothing_whenRequestingArticleHashtagSearchView_thenReturnsArticleHashtagSearchView() throws Exception {
        mvc.perform(get("/articles/search-hashtag"))
                .andExpect(status().isOk()) //status 200 여부
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(view().name("articles/search-hashtag"));
    }
}
