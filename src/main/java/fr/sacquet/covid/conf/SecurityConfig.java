package fr.sacquet.covid.conf;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain auth0FilterChain(HttpSecurity httpSecurity) throws Exception {
        // We don't need CSRF for this example
        httpSecurity.cors(corsCustomizer -> corsCustomizer.configure(httpSecurity));
        httpSecurity
                // dont authenticate this particular request
                .authorizeHttpRequests(autorizeRequests -> autorizeRequests.requestMatchers("/authenticate", "/register", "/open/api/**").permitAll()
                // all other requests need to be authenticated
                .anyRequest().authenticated());
        httpSecurity.sessionManagement(sessionAuthenticationStrategy -> sessionAuthenticationStrategy.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        return httpSecurity.build();
    }
}
