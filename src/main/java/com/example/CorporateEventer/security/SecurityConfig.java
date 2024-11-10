// package com.example.CorporateEventer.security;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
// import org.springframework.security.config.annotation.web.builders.HttpSecurity;
// import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
// import org.springframework.security.core.userdetails.UserDetailsService;
// import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
// import org.springframework.security.crypto.password.PasswordEncoder;
// import org.springframework.security.web.SecurityFilterChain;
// import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;

// @Configuration
// @EnableWebSecurity
// public class SecurityConfig {

//     @Autowired
//     private UserDetailsService userDetailsService;

//     @Bean
//     public static PasswordEncoder passwordEncoder(){
//         return new BCryptPasswordEncoder();
//     }

//     @Bean
//     public SecurityFilterChain FilterChain(HttpSecurity http) throws Exception {
//         http

//                 .csrf(AbstractHttpConfigurer::disable)
//                 .cors(AbstractHttpConfigurer::disable)
//                 .authorizeHttpRequests((requests) -> requests
//                                 .requestMatchers("/login").anonymous()
//                                 .requestMatchers("/register").anonymous()
//                                 .requestMatchers("/user_page").authenticated()
//                                 .requestMatchers("/admin").hasRole("ADMIN")
//                                 .requestMatchers("/admin/**").hasRole("ADMIN")
//                                 .requestMatchers("/css/**", "/js/**").permitAll()
//                                 .requestMatchers("/**").permitAll()
//                                 .requestMatchers("/").permitAll()
//                 )
//                 .formLogin((form) -> form
//                         .loginPage("/login")
//                         .defaultSuccessUrl("/index")
//                         .permitAll()
//                 )
//                 .logout((logout) -> logout
//                         .logoutUrl("/logout")
//                         .logoutSuccessUrl("/")
//                         .invalidateHttpSession(true));
//         return http.build();
//     }

//     @Autowired
//     public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
//         auth
//                 .userDetailsService(userDetailsService)
//                 .passwordEncoder(passwordEncoder());
//     }

// }

