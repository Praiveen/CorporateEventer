package com.example.CorporateEventer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {
    private final AuthenticationProvider authenticationProvider;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfiguration(
        JwtAuthenticationFilter jwtAuthenticationFilter,
        AuthenticationProvider authenticationProvider
    ) {
        this.authenticationProvider = authenticationProvider;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests((requests) -> requests
                                .requestMatchers("/login").anonymous()
                                .requestMatchers("/register").anonymous()
                                .requestMatchers("/user_page").authenticated()
                                // .requestMatchers("/admin").hasRole("ADMIN")
                                // .requestMatchers("/admin/**").hasRole("ADMIN")
                                .requestMatchers("/css/**", "/js/**").permitAll()
                                .requestMatchers("/**").permitAll()
                                .requestMatchers("/").permitAll()
                )
                .formLogin((form) -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/dashboard")
                        .permitAll()
                )
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
                // .logout()
                // .logoutSuccessHandler((req, res, auth) -> tokenBlacklistService.addToBlacklist(req.getHeader("Authorization")))
                // .permitAll();


        return http.build();
    }

    // @Bean
    // public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    //     http.csrf()
    //             .disable()
    //             .authorizeHttpRequests()
    //             .requestMatchers("/auth/**")
    //             .permitAll()
    //             .anyRequest()
    //             .authenticated()
    //             .and()
    //             .sessionManagement()
    //             .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
    //             .and()
    //             .authenticationProvider(authenticationProvider)
    //             .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    //     return http.build();
    // }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // configuration.setAllowedOrigins(List.of("http://localhost:8005"));
        configuration.setAllowedMethods(List.of("GET","POST"));
        configuration.setAllowedHeaders(List.of("Authorization","Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**",configuration);

        return source;
    }
}
