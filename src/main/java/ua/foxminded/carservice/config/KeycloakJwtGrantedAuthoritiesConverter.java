package ua.foxminded.carservice.config;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

public class KeycloakJwtGrantedAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

  @Override
  public Collection<GrantedAuthority> convert(Jwt jwt) {  
    Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
    
    Map<String, Object> clientAccess = (Map<String, Object>) resourceAccess.get("car-service-client");
    if (clientAccess == null) {
      return Collections.emptyList();
    }

    List<String> roles = (List<String>) clientAccess.get("roles");
    if (roles == null) {
      return Collections.emptyList();
    }

    return roles.stream()
        .map(role -> "ROLE_" + role.toUpperCase())
        .<GrantedAuthority>map(SimpleGrantedAuthority::new)
        .toList();
  }
}
