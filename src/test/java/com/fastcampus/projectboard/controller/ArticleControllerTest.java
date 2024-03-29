package com.fastcampus.projectboard.controller;

import com.fastcampus.projectboard.config.SecurityConfig;
import com.fastcampus.projectboard.config.TestSecurityConfig;
import com.fastcampus.projectboard.domain.constant.FormStatus;
import com.fastcampus.projectboard.domain.constant.SearchType;
import com.fastcampus.projectboard.dto.ArticleDto;
import com.fastcampus.projectboard.dto.ArticleWithCommentsDto;
import com.fastcampus.projectboard.dto.UserAccountDto;
import com.fastcampus.projectboard.dto.security.BoardPrincipal;
import com.fastcampus.projectboard.request.ArticleRequest;
import com.fastcampus.projectboard.response.ArticleResponse;
import com.fastcampus.projectboard.service.ArticleService;
import com.fastcampus.projectboard.service.PaginationService;
import com.fastcampus.projectboard.util.FormDataEncoder;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("View 컨트롤러 - 게시글")
@Import({TestSecurityConfig.class, FormDataEncoder.class}) // Security 설정 전체 허용 적용
@WebMvcTest(ArticleController.class) //정석적인 WebMvcTest - 별도의 어노테이션 설정 없이 MockMvc 사용 가능
class ArticleControllerTest {

    /**
     * @WebMvcTest의 @ExtendWith 즉, SpringExtention 내부에 @Autowired를 인지해서
     * 생성자 주입을 할 수 있게끔 하는 코드가 들어있으나, @MockBean은 불가능하다.
     * @Autowired는 타겟이 Method의 parameter에 들어갈 수 있게 되어있으나, MockBean은 타입과 필드만 가능하다.
     */
    @MockBean private ArticleService articleService;
    @MockBean
    private PaginationService paginationService;

    private final MockMvc mvc;
    private final FormDataEncoder formDataEncoder;
    /**
     * @Autowired Field Injection + Constructor
     * (실제 필드 인젝션을 하면 되지만, 권장히지 않는다는 문구 때문에...)
     * 테스트 패키지에 있는 생성자(주입)은 @Autowired 생략이 불가능하다.
     * @See <a href="https://pinokio0702.tistory.com/189">참조 게시 글</a>
     * @WebMvcTest의 @ExtendWith 즉, SpringExtention 내부에 @Autowired를 인지해서
     * 생성자 주입을 할 수 있게끔 하는 코드가 들어있다.
     */
    ArticleControllerTest(
            @Autowired MockMvc mvc,
            @Autowired FormDataEncoder formDataEncoder
    ) {
        this.mvc = mvc;
        this.formDataEncoder = formDataEncoder;
    }

    @DisplayName("[view][GET] 게시글 리스트 (게시판) 페이지 - 정상 호출")
    @Test
    public void givenNothing_whenRequestingArticlesView_thenReturnsArticlesView() throws Exception {
        // Given
        /**
         * eq, any : 일종의 Matcher (Argument Matcher)
         * 정확한 null, 어떠한(아무) Pageable 클래스
         */
        given(articleService.searchArticles(eq(null), eq(null), any(Pageable.class))).willReturn(Page.empty());
        given(paginationService.getPaginationBarNumbers(anyInt(), anyInt())).willReturn(List.of(0, 1, 2, 3, 4)); //임의의 값들로 페이징 테스트

        // When & Then
        mvc.perform(get("/articles"))
                .andExpect(status().isOk()) //status 200 여부
//                .andExpect(content().contentType(MediaType.TEXT_HTML)) //내용의 타입 view이므로 TEXT-HTML인지 여부
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML)) //호환되는 타입까지 적용시켜주는 옵션이 있다 ex) 스프링부트에서 추가시켜주는 charset=UTF-8
                .andExpect(view().name("articles/index")) //해당 경로에 view가 있어야함
                /* 해당 뷰는 데이터가 있어야한다. 게시글 목록이 출력된다는것은 서버에서 게시글 목록을 ModelAttribute로 view에 넘겨줬다는 의미 */
                .andExpect(model().attributeExists("articles")) //모델 애트리뷰트맵에 해당이름의 키가 있는지 여부
                .andExpect(model().attributeExists("paginationBarNumbers"));
        then(articleService).should().searchArticles(eq(null), eq(null), any(Pageable.class)); // should는 1회 호출된다는 내용이 포함되어있음.
        then(paginationService).should().getPaginationBarNumbers(anyInt(), anyInt());
    }

    @DisplayName("[view][GET] 게시글 리스트 (게시판) 페이지 - 검색어와 함께 호출")
    @Test
    public void givenSearchKeyword_whenSearchingArticlesView_thenReturnsArticlesView() throws Exception {
        // Given

        SearchType searchType = SearchType.TITLE;
        String searchValue = "title";
        given(articleService.searchArticles(eq(searchType), eq(searchValue), any(Pageable.class))).willReturn(Page.empty());
        given(paginationService.getPaginationBarNumbers(anyInt(), anyInt())).willReturn(List.of(0, 1, 2, 3, 4)); //임의의 값들로 페이징 테스트

        // When & Then
        mvc.perform(get("/articles").queryParam("searchType", searchType.name()).queryParam("searchValue", searchValue))
                .andExpect(status().isOk()) //status 200 여부
//                .andExpect(content().contentType(MediaType.TEXT_HTML)) //내용의 타입 view이므로 TEXT-HTML인지 여부
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML)) //호환되는 타입까지 적용시켜주는 옵션이 있다 ex) 스프링부트에서 추가시켜주는 charset=UTF-8
                .andExpect(view().name("articles/index")) //해당 경로에 view가 있어야함
                /* 해당 뷰는 데이터가 있어야한다. 게시글 목록이 출력된다는것은 서버에서 게시글 목록을 ModelAttribute로 view에 넘겨줬다는 의미 */
                .andExpect(model().attributeExists("articles")) //모델 애트리뷰트맵에 해당이름의 키가 있는지 여부
                .andExpect(model().attributeExists("searchTypes"));
        then(articleService).should().searchArticles(eq(searchType), eq(searchValue), any(Pageable.class)); // should는 1회 호출된다는 내용이 포함되어있음.
        then(paginationService).should().getPaginationBarNumbers(anyInt(), anyInt());
    }

    @DisplayName("[view][GET] 게시글 리스트 (게시판) 페이지 - 페이징, 정렬 기능")
    @Test
    void givenPagingAndSortingParams_whenSearchingArticlesPage_thenReturnsArticlesPage() throws Exception {
        // Given
        String sortName = "title";
        String direction = "desc";
        int pageNumber = 0;
        int pageSize = 5;
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Order.desc(sortName)));
        List<Integer> barNumbers = List.of(1, 2, 3, 4, 5);
        given(articleService.searchArticles(null, null, pageable)).willReturn(Page.empty());
        given(paginationService.getPaginationBarNumbers(pageable.getPageNumber(), Page.empty().getTotalPages())).willReturn(barNumbers);

        // When & Then
        mvc.perform(
                        get("/articles")
                                .queryParam("page", String.valueOf(pageNumber))
                                .queryParam("size", String.valueOf(pageSize))
                                .queryParam("sort", sortName + "," + direction)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(view().name("articles/index"))
                .andExpect(model().attributeExists("articles"))
                .andExpect(model().attribute("paginationBarNumbers", barNumbers));
        then(articleService).should().searchArticles(null, null, pageable);
        then(paginationService).should().getPaginationBarNumbers(pageable.getPageNumber(), Page.empty().getTotalPages());
    }

    @DisplayName("[view][GET] 게시글 페이지 - 인증 없을 땐 로그인 페이지로 이동")
    @Test
    public void givenNothing_whenReqeustArticlePage_thenRedirectsToLoginPage() throws Exception{
        // Given
        long articleId = 1L;

        // When & Then
        mvc.perform(get("/articlee/" + articleId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));//앞쪽인 localhost를 포함한 경로를 **로 표현 redirectUrl을 쓴다면 full로 작성해야함
        then(articleService).shouldHaveNoInteractions();
        then(articleService).shouldHaveNoInteractions();
    }

    @WithMockUser // 유저정보 모킹 (BoardPrincipal)과 연관없을때 임의로 유저의 인증로직을 통과시킨다.
    @DisplayName("[view][GET] 게시글 페이지 - 정상 호출, 인증된 사용자")
    @Test
    public void givenNothing_whenRequestingArticleView_thenReturnsArticleView() throws Exception {
        // Given
        Long articleId = 1L;
        long totalCount = 1L; // 임의의 값 (0이어도 됨...)
        given(articleService.getArticleWithComments(articleId)).willReturn(createArticleWithCommentsDto());
        given(articleService.getArticleCount()).willReturn(totalCount);
        // When & Then
        mvc.perform(get("/articles/" + articleId))
                .andExpect(status().isOk()) //status 200 여부
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(view().name("articles/detail"))
                .andExpect(model().attributeExists("article"))
                .andExpect(model().attributeExists("articleComments")) // 댓글 ModelAttribute 야부 확인
                .andExpect(model().attribute("totalCount", totalCount));
        then(articleService).should().getArticleWithComments(articleId);
        then(articleService).should().getArticleCount();
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

    @DisplayName("[view][GET] 게시글 해시태그 검색 페이지 - 정상 호출")
    @Test
    public void givenNothing_whenRequestingArticleSearchHashtagView_thenReturnsArticleSearchHashtagView() throws Exception {
        //Given
        List<String> hashtags = List.of("#java", "#spring", "#boot");
        given(articleService.searchArticlesViaHashtag(eq(null), any(Pageable.class))).willReturn(Page.empty());
        given(articleService.getHashtags()).willReturn(hashtags);
        given(paginationService.getPaginationBarNumbers(anyInt(), anyInt())).willReturn(List.of(1, 2, 3, 4, 5));

        //When & Then
        mvc.perform(get("/articles/search-hashtag"))
                .andExpect(status().isOk()) //status 200 여부
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(view().name("articles/search-hashtag"))
                .andExpect(model().attribute("articles",Page.empty()))
                .andExpect(model().attribute("hashtags", hashtags))
                .andExpect(model().attributeExists("paginationBarNumbers"))
                .andExpect(model().attribute("searchType", SearchType.HASHTAG));

        then(articleService).should().searchArticlesViaHashtag(eq(null), any(Pageable.class));
        then(articleService).should().getHashtags();
        then(paginationService).should().getPaginationBarNumbers(anyInt(), anyInt());
    }

    @DisplayName("[view][GET] 게시글 해시태그 검색 페이지 - 정상 호출, 해시태그 입력")
    @Test
    public void givenHashtag_whenRequestingArticleSearchHashtagView_thenReturnsArticleSearchHashtagView() throws Exception {
        // Given
        String hashtag = "#java";
        List<String> hashtags = List.of("#java", "#spring", "#boot");
        given(articleService.searchArticlesViaHashtag(eq(hashtag), any(Pageable.class))).willReturn(Page.empty());
        given(articleService.getHashtags()).willReturn(hashtags);
        given(paginationService.getPaginationBarNumbers(anyInt(), anyInt())).willReturn(List.of(1, 2, 3, 4, 5));
        // When & Then
        mvc.perform(
                        get("/articles/search-hashtag")
                                .queryParam("searchValue", hashtag)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(view().name("articles/search-hashtag"))
                .andExpect(model().attribute("articles", Page.empty()))
                .andExpect(model().attribute("hashtags", hashtags))
                .andExpect(model().attributeExists("paginationBarNumbers"))
                .andExpect(model().attribute("searchType", SearchType.HASHTAG));
        then(articleService).should().searchArticlesViaHashtag(eq(hashtag), any(Pageable.class));
        then(articleService).should().getHashtags();
        then(paginationService).should().getPaginationBarNumbers(anyInt(), anyInt());
    }

    @WithMockUser
    @DisplayName("[view][GET] 새 게시글 작성 페이지")
    @Test
    void givenNothing_whenRequesting_thenReturnsNewArticlePage() throws Exception {
        // Given

        // When & Then
        mvc.perform(get("/articles/form"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(view().name("articles/form"))
                .andExpect(model().attribute("formStatus", FormStatus.CREATE));
    }

    @WithUserDetails(value="YooHyeokTest", setupBefore = TestExecutionEvent.TEST_EXECUTION) // TestSecurityConfig에 등록한 인증 회원 모킹
    @DisplayName("[view][POST] 새 게시글 등록 - 정상 호출")
    @Test
    void givenNewArticleInfo_whenRequesting_thenSavesNewArticle() throws Exception {
        // Given
        ArticleRequest articleRequest = ArticleRequest.of("new title", "new content", "#new");
        willDoNothing().given(articleService).saveArticle(any(ArticleDto.class));

        // When & Then
        mvc.perform(
                        post("/articles/form")
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                .content(formDataEncoder.encode(articleRequest))
                                .with(csrf())
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/articles"))
                .andExpect(redirectedUrl("/articles"));
        then(articleService).should().saveArticle(any(ArticleDto.class));
    }

    @WithMockUser
    @DisplayName("[view][GET] 게시글 수정 페이지")
    @Test
    void givenNothing_whenRequesting_thenReturnsUpdatedArticlePage() throws Exception {
        // Given
        long articleId = 1L;
        ArticleDto dto = createArticleDto();
        given(articleService.getArticle(articleId)).willReturn(dto);

        // When & Then
        mvc.perform(get("/articles/" + articleId + "/form"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(view().name("articles/form"))
                .andExpect(model().attribute("article", ArticleResponse.from(dto)))
                .andExpect(model().attribute("formStatus", FormStatus.UPDATE));
        then(articleService).should().getArticle(articleId);
    }

    @WithUserDetails(value="YooHyeokTest", setupBefore = TestExecutionEvent.TEST_EXECUTION) // TestSecurityConfig에 등록한 인증 회원 모킹
    @DisplayName("[view][POST] 게시글 수정 - 정상 호출")
    @Test
    void givenUpdatedArticleInfo_whenRequesting_thenUpdatesNewArticle() throws Exception {
        // Given
        long articleId = 1L;
        ArticleRequest articleRequest = ArticleRequest.of("new title", "new content", "#new");
        willDoNothing().given(articleService).updateArticle(eq(articleId), any(ArticleDto.class));

        // When & Then
        mvc.perform(
                        post("/articles/" + articleId + "/form")
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                .content(formDataEncoder.encode(articleRequest))
                                .with(csrf())
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/articles/" + articleId))
                .andExpect(redirectedUrl("/articles/" + articleId));
        then(articleService).should().updateArticle(eq(articleId), any(ArticleDto.class));
    }

    @WithUserDetails(value="YooHyeokTest", setupBefore = TestExecutionEvent.TEST_EXECUTION) // TestSecurityConfig에 등록한 인증 회원 모킹
    @DisplayName("[view][POST] 게시글 삭제 - 정상 호출")
    @Test
    void givenArticleIdToDelete_whenRequesting_thenDeletesArticle() throws Exception {
        // Given
        long articleId = 1L;
        String userId = "YooHyeokTest";
        willDoNothing().given(articleService).deleteArticle(articleId, userId);

        // When & Then
        mvc.perform(
                        post("/articles/" + articleId + "/delete")
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                .with(csrf())
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/articles"))
                .andExpect(redirectedUrl("/articles"));
        then(articleService).should().deleteArticle(articleId, userId);
    }


    private ArticleDto createArticleDto() {
        return ArticleDto.of(
                createUserAccountDto(),
                "title",
                "content",
                "#java"
        );
    }

    private ArticleWithCommentsDto createArticleWithCommentsDto() {
        return ArticleWithCommentsDto.of(
                1L,
                createUserAccountDto(),
                Set.of(),
                "title",
                "content",
                "#java",
                LocalDateTime.now(),
                "yooHyeok",
                LocalDateTime.now(),
                "yooHyeok"
        );
    }
    private UserAccountDto createUserAccountDto() {
        return UserAccountDto.of(
                "yooHyeok",
                "123qwe",
                "webdevyoo@gmail.com",
                "yooHyeok",
                "memo",
                LocalDateTime.now(),
                "yooHyeok",
                LocalDateTime.now(),
                "yooHyeok"
        );
    }
}
