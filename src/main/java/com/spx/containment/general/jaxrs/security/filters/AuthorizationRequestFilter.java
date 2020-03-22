package com.spx.containment.general.jaxrs.security.filters;

import com.spx.containment.general.jaxrs.security.JWTTokenAuthenticator;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import javax.annotation.Priority;
import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;


@Priority(Priorities.AUTHORIZATION)
public class AuthorizationRequestFilter implements ContainerRequestFilter {

  private static final Logger logger = LoggerFactory.getLogger(AuthorizationRequestFilter.class);

  private final ResourceInfo resourceInfo;

  public AuthorizationRequestFilter(@Autowired JWTTokenAuthenticator authenticator,
      @Context ResourceInfo resourceInfo) {
    this.resourceInfo = resourceInfo;
    logger.debug("constructing oauth 2.0 JWT authorization token filter");
  }

  @SneakyThrows
  @Override
  public void filter(ContainerRequestContext context) {
    try {
      if (isPermitAllMethod()) {
        return;
      }
      checkDenyAllOnMethodOrClass();
      checkRolesAllowedOnMethodOrClass(SecurityContextHolder.getContext()
          .getAuthentication()
          .getAuthorities());
    } catch (Exception e) {
      logger.error("Error occurred during authorization {}", e);
      context.abortWith(Response.status(Response.Status.UNAUTHORIZED)
          .build());
    }

  }

  private boolean isPermitAllMethod() {
    return resourceInfo.getResourceMethod()
        .getAnnotation(PermitAll.class) != null;
  }

  private void checkRolesAllowedOnMethodOrClass(
      Collection<? extends GrantedAuthority> authorities) {
    RolesAllowed allowed = resourceInfo.getResourceMethod()
        .getAnnotation(RolesAllowed.class);
    if (allowed == null) {
      allowed = resourceInfo.getResourceClass()
          .getAnnotation(RolesAllowed.class);
    }
    if (allowed == null) {
      return;
    }

    List<String> rolesAllowed = Arrays.asList(allowed.value());
    if (authorities.stream()
        .map(GrantedAuthority::getAuthority)
        .noneMatch(rolesAllowed::contains)) {
      throw new SecurityException();
    }


  }

  private void checkDenyAllOnMethodOrClass() {
    if (resourceInfo.getResourceMethod()
        .getAnnotation(DenyAll.class) != null) {
      throw new SecurityException();
    }
    if (resourceInfo.getResourceClass()
        .getAnnotation(DenyAll.class) != null && resourceInfo.getResourceMethod()
        .getAnnotation(RolesAllowed.class) == null && resourceInfo.getResourceMethod()
        .getAnnotation(PermitAll.class) == null) {
      throw new SecurityException();
    }


  }


}