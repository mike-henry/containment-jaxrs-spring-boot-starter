package com.spx.containment.general.jaxrs.security;

import com.nimbusds.jwt.JWTClaimsSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class AuthenticationCreator {

  private static final String ROLES = "roles";

  private final RolesFromPermissionsConfig rolesFromPermissionsConfig;
  private final String tokenRoles;

  public AuthenticationCreator(@Autowired RolesFromPermissionsConfig rolesFromPermissionsConfig,
      @Value("${application.security.token-roles}") String tokenRoles) {
    this.rolesFromPermissionsConfig = rolesFromPermissionsConfig;
    this.tokenRoles = tokenRoles;
  }

  public void addPrinciple(JWTClaimsSet claims) {
    SecurityContextHolder.getContext()
        .setAuthentication(createAuthentication(claims));
  }


  @SneakyThrows
  private Set<GrantedAuthority> getGrantedAuthorities(JWTClaimsSet claims) {

    return ((JSONArray) claims.getJSONObjectClaim(tokenRoles)
        .get(ROLES)).stream()
        .map(Object::toString)
        .map(this::getRoleOrPermission)
        .peek(r -> log.warn("Role Added: {} ", r))
        .map(SimpleGrantedAuthority::new)
        .collect(Collectors.toSet());
  }

  private String getRoleOrPermission(String permission) {
    return Optional.ofNullable(rolesFromPermissionsConfig.getRoleFor(permission))
        .orElse(StringUtils.trim(permission) + "_permission_user");
  }

  private Authentication createAuthentication(JWTClaimsSet claims) {
    AuthenticationUser result = new AuthenticationUser(getGrantedAuthorities(claims),
        createPrinciple(claims));
    return result;
  }

  private AuthenticatedPrincipal createPrinciple(JWTClaimsSet claims) {

    return new AuthenticatedPrincipal() {

      @Override
      public String getName() {
        return Optional.ofNullable((String) claims.getClaim("name"))
            .orElse(claims.getSubject());
      }
    };

  }

}
