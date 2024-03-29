package com.fastcampus.projectboard.controller;

import com.fastcampus.projectboard.config.SecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;

@DisplayName("View 컨트롤러 - 인증 부분")
@Import(SecurityConfig.class) // Security 설정 전체 허용 적용
@WebMvcTest(Void.class)
public class AuthControllerTest {

    private final MockMvc mvc;

    public AuthControllerTest(@Autowired MockMvc mvc) {this.mvc = mvc;}

    @DisplayName("[view][GET] 게시글 리스트 (게시판) 페이지 - 정상 호출")
    @Test
    public void givenNothing_whenTryingLogIn_thenReturnsLogInView() throws Exception {
        mvc.perform(get("/login"))
                .andExpect(status().isOk()) //status 200 여부
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andDo(MockMvcResultHandlers.print()); // 결과물 HTML 출력
    }
}
