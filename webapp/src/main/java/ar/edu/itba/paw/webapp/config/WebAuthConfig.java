package ar.edu.itba.paw.webapp.config;

import ar.edu.itba.paw.webapp.auth.AccessValidator;
import ar.edu.itba.paw.webapp.auth.filters.AuthAnywhereFilter;
import ar.edu.itba.paw.webapp.auth.filters.JwtTokenFilter;
import ar.edu.itba.paw.webapp.auth.JwtTokenUtil;
import ar.edu.itba.paw.webapp.utils.UriUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@ComponentScan("ar.edu.itba.paw.webapp.auth")
@Configuration
public class WebAuthConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private AccessValidator accessValidator;

    @Autowired
    private JwtTokenFilter jwtTokenFilter;

    @Autowired
    private AuthAnywhereFilter authAnywhereFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
    }

    @Override
    public void configure(final HttpSecurity http) throws Exception {
        http.sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)

                .and().authorizeRequests()
                .antMatchers(HttpMethod.GET, UriUtils.IMAGES_URL + "/{imageId:\\d+}").permitAll()
                .antMatchers(HttpMethod.POST, UriUtils.IMAGES_URL + "/{imageId:\\d+}").authenticated()

                .antMatchers(HttpMethod.GET, UriUtils.RESTAURANTS_URL).permitAll() // Checked with @PreAuthorize
                .antMatchers(HttpMethod.POST, UriUtils.RESTAURANTS_URL).authenticated()
                .antMatchers(HttpMethod.GET, UriUtils.RESTAURANTS_URL + "/{restaurantId:\\d+}").permitAll()
                .antMatchers(HttpMethod.PATCH, UriUtils.RESTAURANTS_URL + "/{restaurantId:\\d+}").permitAll() // Checked with @PreAuthorize
                .antMatchers(HttpMethod.DELETE, UriUtils.RESTAURANTS_URL + "/{restaurantId:\\d+}").access("@accessValidator.checkRestaurantOwner(#restaurantId) or hasRole('MODERATOR')")

                .antMatchers(HttpMethod.GET, UriUtils.RESTAURANTS_URL + "/{restaurantId:\\d+}/categories").permitAll()
                .antMatchers(HttpMethod.POST, UriUtils.RESTAURANTS_URL + "/{restaurantId:\\d+}/categories").access("@accessValidator.checkRestaurantAdmin(#restaurantId)")
                .antMatchers(HttpMethod.GET, UriUtils.RESTAURANTS_URL + "/{restaurantId:\\d+}/categories/{categoryId:\\d+}").permitAll()
                .antMatchers(HttpMethod.PATCH, UriUtils.RESTAURANTS_URL + "/{restaurantId:\\d+}/categories/{categoryId:\\d+}").access("@accessValidator.checkRestaurantAdmin(#restaurantId)")
                .antMatchers(HttpMethod.DELETE, UriUtils.RESTAURANTS_URL + "/{restaurantId:\\d+}/categories/{categoryId:\\d+}").access("@accessValidator.checkRestaurantAdmin(#restaurantId)")
                .antMatchers(HttpMethod.GET, UriUtils.RESTAURANTS_URL + "/{restaurantId:\\d+}/categories/{categoryId:\\d+}/products").permitAll()
                .antMatchers(HttpMethod.POST, UriUtils.RESTAURANTS_URL + "/{restaurantId:\\d+}/categories/{categoryId:\\d+}/products").access("@accessValidator.checkRestaurantAdmin(#restaurantId)")
                .antMatchers(HttpMethod.GET, UriUtils.RESTAURANTS_URL + "/{restaurantId:\\d+}/categories/{categoryId:\\d+}/products/{productId:\\d+}").permitAll()
                .antMatchers(HttpMethod.PATCH, UriUtils.RESTAURANTS_URL + "/{restaurantId:\\d+}/categories/{categoryId:\\d+}/products/{productId:\\d+}").access("@accessValidator.checkRestaurantAdmin(#restaurantId)")
                .antMatchers(HttpMethod.DELETE, UriUtils.RESTAURANTS_URL + "/{restaurantId:\\d+}/categories/{categoryId:\\d+}/products/{productId:\\d+}").access("@accessValidator.checkRestaurantAdmin(#restaurantId)")

                .antMatchers(HttpMethod.GET, UriUtils.RESTAURANTS_URL + "/{restaurantId:\\d+}/promotions").permitAll() // Checked with @PreAuthorize
                .antMatchers(HttpMethod.POST, UriUtils.RESTAURANTS_URL + "/{restaurantId:\\d+}/promotions").access("@accessValidator.checkRestaurantAdmin(#restaurantId)")
                .antMatchers(HttpMethod.GET, UriUtils.RESTAURANTS_URL + "/{restaurantId:\\d+}/promotions/{promotionId:\\d+}").permitAll()
                .antMatchers(HttpMethod.DELETE, UriUtils.RESTAURANTS_URL + "/{restaurantId:\\d+}/promotions/{promotionId:\\d+}").access("@accessValidator.checkRestaurantAdmin(#restaurantId)")

                .antMatchers(HttpMethod.GET, UriUtils.RESTAURANTS_URL + "/{restaurantId:\\d+}/employees").access("hasRole('MODERATOR') or @accessValidator.checkRestaurantOwner(#restaurantId)")
                .antMatchers(HttpMethod.POST, UriUtils.RESTAURANTS_URL + "/{restaurantId:\\d+}/employees").access("@accessValidator.checkRestaurantOwner(#restaurantId)")
                .antMatchers(HttpMethod.GET, UriUtils.RESTAURANTS_URL + "/{restaurantId:\\d+}/employees/{userId:\\d+}").access("hasRole('MODERATOR') or @accessValidator.checkRestaurantOwner(#restaurantId) or @accessValidator.checkIsUser(#userId)")
                .antMatchers(HttpMethod.PUT, UriUtils.RESTAURANTS_URL + "/{restaurantId:\\d+}/employees/{userId:\\d+}").access("@accessValidator.checkRestaurantOwner(#restaurantId)")
                .antMatchers(HttpMethod.DELETE, UriUtils.RESTAURANTS_URL + "/{restaurantId:\\d+}/employees/{userId:\\d+}").access("hasRole('MODERATOR') or @accessValidator.checkRestaurantOwner(#restaurantId)")

                .antMatchers(HttpMethod.GET, UriUtils.RESTAURANTS_URL + "/{restaurantId:\\d+}/reports").access("hasRole('MODERATOR')")
                .antMatchers(HttpMethod.POST, UriUtils.RESTAURANTS_URL + "/{restaurantId:\\d+}/reports").permitAll()
                .antMatchers(HttpMethod.GET, UriUtils.RESTAURANTS_URL + "/{restaurantId:\\d+}/reports/{reportId:\\d+}").access("hasRole('MODERATOR')")
                .antMatchers(HttpMethod.PATCH, UriUtils.RESTAURANTS_URL + "/{restaurantId:\\d+}/reports/{reportId:\\d+}").access("hasRole('MODERATOR')")
                .antMatchers(HttpMethod.DELETE, UriUtils.RESTAURANTS_URL + "/{restaurantId:\\d+}/reports/{reportId:\\d+}").access("hasRole('MODERATOR')")

                .antMatchers(HttpMethod.GET, UriUtils.USERS_URL + "/{userId:\\d+}").authenticated() // Checked with @PreAuthorize
                .antMatchers(HttpMethod.POST, UriUtils.USERS_URL).permitAll()
                .antMatchers(HttpMethod.DELETE, UriUtils.USERS_URL + "/{userId:\\d+}").access("hasRole('MODERATOR')")
                .antMatchers(UriUtils.USERS_URL + "/{userId:\\d+}/**").access("@accessValidator.checkIsUser(#userId)")
                .antMatchers(HttpMethod.GET, UriUtils.USERS_URL + "/{userId:\\d+}/addresses").access("@accessValidator.checkIsUser(#userId)")
                .antMatchers(HttpMethod.POST, UriUtils.USERS_URL + "/{userId:\\d+}/addresses").access("@accessValidator.checkIsUser(#userId)")
                .antMatchers(HttpMethod.GET, UriUtils.USERS_URL + "/{userId:\\d+}/addresses/{addressId:\\d+}").access("@accessValidator.checkIsUser(#userId)")
                .antMatchers(HttpMethod.PATCH, UriUtils.USERS_URL + "/{userId:\\d+}/addresses/{addressId:\\d+}").access("@accessValidator.checkIsUser(#userId)")
                .antMatchers(HttpMethod.DELETE, UriUtils.USERS_URL + "/{userId:\\d+}/addresses/{addressId:\\d+}").access("@accessValidator.checkIsUser(#userId)")

                .antMatchers(HttpMethod.GET, UriUtils.ORDERS_URL).authenticated() // Checked with @PreAuthorize
                .antMatchers(HttpMethod.POST, UriUtils.ORDERS_URL).permitAll()
                .antMatchers(HttpMethod.GET, UriUtils.ORDERS_URL + "/{orderId:\\d+}").access("@accessValidator.checkOrderOwnerOrHandler(#orderId)")
                .antMatchers(HttpMethod.GET, UriUtils.ORDERS_URL + "/{orderId:\\d+}/items").access("@accessValidator.checkOrderOwnerOrHandler(#orderId)")
                .antMatchers(HttpMethod.PATCH, UriUtils.ORDERS_URL + "/{orderId:\\d+}").access("@accessValidator.checkOrderHandler(#orderId) ")

                .antMatchers(HttpMethod.GET, UriUtils.REVIEWS_URL).permitAll()
                .antMatchers(HttpMethod.GET, UriUtils.REVIEWS_URL + "/{orderId:\\d+}").permitAll()
                .antMatchers(HttpMethod.POST, UriUtils.REVIEWS_URL).authenticated() // Checked with @PreAuthorize
                .antMatchers(HttpMethod.PUT, UriUtils.REVIEWS_URL + "/{orderId:\\d+}").access("@accessValidator.checkOrderOwner(#orderId)")
                .antMatchers(HttpMethod.PATCH, UriUtils.REVIEWS_URL + "/{orderId:\\d+}").access("@accessValidator.checkOrderHandler(#orderId)")
                .antMatchers(HttpMethod.DELETE, UriUtils.REVIEWS_URL + "/{orderId:\\d+}").access("@accessValidator.checkOrderOwner(#orderId)")

                .antMatchers("/**").permitAll()

                .and()
                .exceptionHandling()
                .authenticationEntryPoint((request, response, ex) -> {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, ex.getMessage());
                })

                // Disable client-side cache handling
                .and().headers().cacheControl().disable()

                .and()
                .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(authAnywhereFilter, UsernamePasswordAuthenticationFilter.class)

                // Enable CORS and disable csrf rules
                .cors().and().csrf().disable();
    }

    @Override
    public void configure(final WebSecurity web) {
        web.ignoring().antMatchers("/static/**");
    }

    @Bean
    public CorsFilter corsFilter() {
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        final CorsConfiguration config = new CorsConfiguration();
        config.setAllowedHeaders(Collections.singletonList("*"));
        config.setAllowedMethods(Arrays.asList("GET", "HEAD", "POST", "OPTIONS", "DELETE", "PUT", "PATCH"));
        config.setExposedHeaders(Arrays.asList("X-MenuMate-AuthToken", "X-MenuMate-RefreshToken", "X-MenuMate-EmployeeUserCreated", "Content-Disposition", "Content-Language", "Location", "ETag", "Last-Modified", "Link", "WWW-Authenticate"));
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

    @Override
    @Bean
    public AuthenticationManager authenticationManager() throws Exception {
        return super.authenticationManager();
    }

    @Bean
    public JwtTokenUtil jwtTokenUtil(@Value("classpath:jwtSecret.key") Resource jwtKeyRes) throws IOException {
        return new JwtTokenUtil(jwtKeyRes);
    }
}
