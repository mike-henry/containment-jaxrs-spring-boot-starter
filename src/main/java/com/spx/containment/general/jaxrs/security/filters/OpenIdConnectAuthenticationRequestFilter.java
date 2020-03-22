package com.spx.containment.general.jaxrs.security.filters;

import com.nimbusds.jwt.JWTClaimsSet;
import com.spx.containment.general.jaxrs.security.AuthenticationCreator;
import com.spx.containment.general.jaxrs.security.JWTTokenAuthenticator;
import java.io.IOException;
import java.util.Date;
import javax.annotation.Priority;
import javax.security.sasl.AuthenticationException;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

@Priority(Priorities.AUTHENTICATION)
@Slf4j
public class OpenIdConnectAuthenticationRequestFilter implements ContainerRequestFilter {

  private final JWTTokenAuthenticator authenticator;

  private final AuthenticationCreator authenticationCreator;

  public OpenIdConnectAuthenticationRequestFilter(@Autowired JWTTokenAuthenticator authenticator,
      @Autowired AuthenticationCreator authenticationCreator, @Context ResourceInfo info) {
    this.authenticator = authenticator;
    this.authenticationCreator = authenticationCreator;
    log.debug("constructing oauth 2.0 JWT  authentication token filter");
  }

  @SneakyThrows
  @Override
  public void filter(ContainerRequestContext context) throws IOException {
    log.debug("openid  connect authentication filtering");
    if (context.getMethod()
        .equals("OPTIONS")) {
      return;
    }
    try {
      String accessToken = getAccessToken(context);
      JWTClaimsSet claims = authenticator.process(accessToken);
      checkExpired(claims);
      checkScope(claims);
      authenticationCreator.addPrinciple(claims);
      log.debug("Passed authentication");
    } catch (Exception e) {
      log.warn("Failed authentication :{}", e.getMessage());
      context.abortWith(Response.status(Response.Status.FORBIDDEN)
          .build());
    }

  }

  @SneakyThrows
  private void checkScope(JWTClaimsSet claims) {
    String scope = claims.getStringClaim("scope");

    if (!(scope.contains("profile")) && scope.contains("openid")) {
      throw new AuthenticationException("invalid openid user");
    }
  }

  @SneakyThrows
  private void checkExpired(JWTClaimsSet claims) {
    if (claims.getExpirationTime()
        .compareTo(new Date()) < 0) {
      throw new AuthenticationException("Expired Token");
    }
  }

  private String getAccessToken(ContainerRequestContext context) {
    String result = StringUtils.replace(context.getHeaderString(HttpHeaders.AUTHORIZATION),
        "Bearer ", "");
    log.debug("Access Token: {}", result);
    return result;
  }
}