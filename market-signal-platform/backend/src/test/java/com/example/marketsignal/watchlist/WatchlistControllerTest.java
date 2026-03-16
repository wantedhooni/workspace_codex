package com.example.marketsignal.watchlist;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.marketsignal.auth.JwtAuthenticationFilter;
import com.example.marketsignal.config.WebMvcConfig;
import com.example.marketsignal.user.CurrentUser;
import com.example.marketsignal.user.CurrentUserArgumentResolver;
import com.example.marketsignal.user.CustomUserDetails;
import com.example.marketsignal.user.UserRole;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(WatchlistController.class)
@Import(WebMvcConfig.class)
@AutoConfigureMockMvc(addFilters = false)
class WatchlistControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WatchlistService watchlistService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private CurrentUserArgumentResolver currentUserArgumentResolver;

    @Test
    @DisplayName("관심 종목 생성 API는 생성된 항목을 반환한다")
    void createReturnsCreatedItem() throws Exception {
        when(watchlistService.create(any(), any()))
                .thenReturn(new WatchlistResponse(1L, "AAPL", "Apple", LocalDateTime.now()));
        when(currentUserArgumentResolver.supportsParameter(any())).thenReturn(true);
        when(currentUserArgumentResolver.resolveArgument(any(), any(), any(), any()))
                .thenReturn(new CurrentUser(1L, "demo@marketsignal.dev"));

        CustomUserDetails principal = new CustomUserDetails(1L, "demo@marketsignal.dev", "pw", UserRole.ROLE_USER);

        mockMvc.perform(post("/api/watchlists")
                        .with(authentication(new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ticker": "AAPL",
                                  "companyName": "Apple"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ticker").value("AAPL"));
    }

    @Test
    @DisplayName("관심 종목 조회 API는 검색어를 서비스에 전달한다")
    void getAllWithQueryReturnsFilteredItems() throws Exception {
        when(watchlistService.getAll(any(), any()))
                .thenReturn(List.of(new WatchlistResponse(1L, "NVDA", "NVIDIA", LocalDateTime.now())));
        when(currentUserArgumentResolver.supportsParameter(any())).thenReturn(true);
        when(currentUserArgumentResolver.resolveArgument(any(), any(), any(), any()))
                .thenReturn(new CurrentUser(1L, "demo@marketsignal.dev"));

        CustomUserDetails principal = new CustomUserDetails(1L, "demo@marketsignal.dev", "pw", UserRole.ROLE_USER);

        mockMvc.perform(get("/api/watchlists")
                        .param("query", "nvd")
                        .with(authentication(new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].ticker").value("NVDA"));
    }

    @Test
    @DisplayName("관심 종목 커버리지 API는 최신 시그널 요약을 반환한다")
    void getCoverageReturnsSignalSummary() throws Exception {
        when(watchlistService.getCoverage(any(), any()))
                .thenReturn(List.of(new WatchlistCoverageResponse(
                        1L,
                        "NVDA",
                        "NVIDIA",
                        LocalDateTime.now(),
                        true,
                        LocalDate.of(2026, 3, 16),
                        7,
                        com.example.marketsignal.signal.SignalAction.BUY,
                        List.of("20DMA 상회", "상대 강도 우위")
                )));
        when(currentUserArgumentResolver.supportsParameter(any())).thenReturn(true);
        when(currentUserArgumentResolver.resolveArgument(any(), any(), any(), any()))
                .thenReturn(new CurrentUser(1L, "demo@marketsignal.dev"));

        CustomUserDetails principal = new CustomUserDetails(1L, "demo@marketsignal.dev", "pw", UserRole.ROLE_USER);

        mockMvc.perform(get("/api/watchlists/coverage")
                        .with(authentication(new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].ticker").value("NVDA"))
                .andExpect(jsonPath("$[0].action").value("BUY"))
                .andExpect(jsonPath("$[0].reasons[0]").value("20DMA 상회"));
    }
}
