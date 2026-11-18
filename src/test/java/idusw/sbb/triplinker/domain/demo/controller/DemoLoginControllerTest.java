package idusw.sbb.triplinker.domain.demo.controller;

import idusw.sbb.triplinker.domain.auth.dto.TokenResponseDto;
import idusw.sbb.triplinker.domain.demo.DemoLoginProperties;
import idusw.sbb.triplinker.domain.demo.service.DemoLoginService;
import idusw.sbb.triplinker.domain.demo.service.DemoUsageLimiter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DemoLoginControllerTest {

    private MockMvc mvc(boolean enabled, int perMinute, DemoLoginService service) {
        DemoLoginProperties props = new DemoLoginProperties(enabled, 10, perMinute);
        return MockMvcBuilders.standaloneSetup(
                new DemoLoginController(props, service, new DemoUsageLimiter(props))).build();
    }

    private DemoLoginService tokenService() {
        DemoLoginService s = mock(DemoLoginService.class);
        when(s.login(any())).thenReturn(new TokenResponseDto("access-x", "refresh-y", false));
        return s;
    }

    @Test
    @DisplayName("켜져 있으면 단추 길이 일반 로그인과 같은 모양으로 토큰을 준다. 비밀번호는 오가지 않는다")
    void issuesTokens() throws Exception {
        DemoLoginService service = tokenService();
        MvcResult r = mvc(true, 10, service).perform(post("/api/auth/demo")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"role\":\"user\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("로그인 성공"))
                .andExpect(jsonPath("$.data.accessToken").value("access-x"))
                .andExpect(jsonPath("$.data.refreshToken").value("refresh-y"))
                .andReturn();
        assertThat(r.getResponse().getContentAsString().toLowerCase()).doesNotContain("password\":\"");
        verify(service).login(DemoLoginService.Kind.USER);

        mvc(true, 10, service).perform(post("/api/auth/demo")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"role\":\"admin\"}"))
                .andExpect(status().isOk());
        verify(service).login(DemoLoginService.Kind.ADMIN);
    }

    @Test
    @DisplayName("끄면(APP_DEMO_LOGIN_ENABLED=false) 단추 확인 길과 로그인 길 모두 404")
    void disabledIs404() throws Exception {
        DemoLoginService service = tokenService();
        MockMvc mvc = mvc(false, 10, service);
        mvc.perform(get("/api/auth/demo")).andExpect(status().isNotFound());
        mvc.perform(post("/api/auth/demo").contentType(MediaType.APPLICATION_JSON)
                .content("{\"role\":\"user\"}")).andExpect(status().isNotFound());
        verifyNoInteractions(service);
    }

    @Test
    @DisplayName("켜져 있으면 단추 확인 길은 200")
    void enabledStatus() throws Exception {
        mvc(true, 10, tokenService()).perform(get("/api/auth/demo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.enabled").value(true));
    }

    @Test
    @DisplayName("role 이 user/admin 이 아니면 400")
    void badRole() throws Exception {
        mvc(true, 10, tokenService()).perform(post("/api/auth/demo")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"role\":\"root\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("한 IP가 1분 한도를 넘기면 429")
    void rateLimited() throws Exception {
        MockMvc mvc = mvc(true, 2, tokenService());
        for (int i = 0; i < 2; i++) {
            mvc.perform(post("/api/auth/demo").contentType(MediaType.APPLICATION_JSON)
                    .content("{\"role\":\"user\"}")).andExpect(status().isOk());
        }
        mvc.perform(post("/api/auth/demo").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"user\"}"))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.success").value(false));
    }
}
