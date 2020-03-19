package com.spx.containment.general.web.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;


@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

  private static final String[] ALLOWED_PATHS = {"/css/**", "/webjars/**"};
  private static final String[] API_PATHS = {"/api/**"};


  @Override
  protected void configure(HttpSecurity http) throws Exception {
    // @formatter:off
    http.csrf()
        .disable();
    http.authorizeRequests()
        .antMatchers(API_PATHS)
        .permitAll();
//        http
//
//            .oauth2Login()
//
//            .successHandler(oauthSuccessHandler)
////		  .defaultSuccessUrl("/")
//            .userInfoEndpoint()
//            .oidcUserService(new CryptoOidcUserService());

    // @formatter:on
  }


  @Override
  public void configure(WebSecurity web) {
    web.ignoring()
        .antMatchers(ALLOWED_PATHS);
  }


}
