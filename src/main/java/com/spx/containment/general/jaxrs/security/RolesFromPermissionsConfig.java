package com.spx.containment.general.jaxrs.security;


import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ConfigurationProperties(prefix = "application.security")
@PropertySource("application.yml")
public class RolesFromPermissionsConfig {

  private Map<String, String> roles;

  public Map<String, String> getRoles() {
    return roles;
  }

  public void setRoles(Map<String, String> roles) {
    this.roles = roles;
  }

  String getRoleFor(String role) {
    return roles.get(role);
  }
}

