package guru.sfg.brewery.config;

import guru.sfg.brewery.security.RestHeaderAuthFiler;
import guru.sfg.brewery.security.SfgPasswordEncoderFactories;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    public RestHeaderAuthFiler restHeaderAuthFiler(AuthenticationManager authenticationManager) {
        RestHeaderAuthFiler filer = new RestHeaderAuthFiler(new AntPathRequestMatcher("/api/**"));
        filer.setAuthenticationManager(authenticationManager);

        return filer;
    }



    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.addFilterBefore(restHeaderAuthFiler(authenticationManager()), UsernamePasswordAuthenticationFilter.class)
                .csrf().disable();

        http.authorizeRequests(authorize -> {
            authorize.antMatchers("/h2-console/**").permitAll()
                    .antMatchers("/", "/webjars/**", "/login", "/resources/**").permitAll()
                    .antMatchers("/beers/find", "/beers/**").permitAll()
                    .antMatchers(HttpMethod.GET, "/api/v1/beer/**").permitAll()
                    ./*antMatchers*/mvcMatchers(HttpMethod.GET, "/api/v1/beerUpc/{upc}").permitAll();

        })
                .authorizeRequests()
                .anyRequest().authenticated()
                .and()
                .formLogin().and()
                .httpBasic();

        // h2 console config
        http.headers().frameOptions().sameOrigin();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return SfgPasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication()
                .withUser("spring")
                .password("{bcrypt}$2a$10$hqRrBSEZ/3d7kbhPGk5jse6AJqEYVt1LqSIlpJb52vH.RowY0tOY2")
                .roles("ADMIN")
                .and()
                .withUser("user")
                .password("{sha256}ea8ce0cbbf3060ffd496546eb5b94b8ebadc8bdc837ac3e0d3cd2537cb0d8e764a80009e7e15e617")
                .roles("USER");

        auth.inMemoryAuthentication().withUser("scott").password("{bcrypt10}$2a$11$vfpbaZV0ASrlUo88Q7Kjj.8Rvm9qdBdlazAt0djqQjosWnzd9W4a2").roles("CUSTOMER");
    }

    //    @Override   // in memory
//    @Bean
//    protected UserDetailsService userDetailsService() {
//        UserDetails admin = User.withDefaultPasswordEncoder()
//                .username("spring")
//                .password("guru")
//                .roles("ADMIN")
//                .build();
//        UserDetails user = User.withDefaultPasswordEncoder()
//                .username("user")
//                .password("password")
//                .roles("USER")
//                .build();
//        return new InMemoryUserDetailsManager(admin,user);
//    }
}
