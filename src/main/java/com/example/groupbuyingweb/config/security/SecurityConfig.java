package com.example.groupbuyingweb.config.security;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // @PreAuthorize 어노테이션을 컨트롤러에서 사용하기 위해 필수!
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 보호 비활성화
                // REST API 서버로 개발 중이고 테스트하기 편하도록 일단 껐음
                .csrf(csrf -> csrf.disable())

                // 폼 로그인 및 기본 HTTP Basic 인증 비활성화 (세션/토큰 방식을 직접 제어하기 위함)
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())

                // HTTP 요청에 대한 전역 권한 설정
                .authorizeHttpRequests(auth -> auth
                        // 세부 권한은 컨트롤러의 @PreAuthorize로 제어할 예정이므로 여기서는 모두 허용
                        .anyRequest().permitAll()
                )

                // !!!!팀원이 로그인 구현하기 전까지 사용할 임시 필터 등록!!!!
                // 스프링 시큐리티의 기본 인증 필터가 실행되기 전에 임시 유저를 컨텍스트에 강제 주입함
                .addFilterBefore(new DummyAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}