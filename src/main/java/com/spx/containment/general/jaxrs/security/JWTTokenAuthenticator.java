package com.spx.containment.general.jaxrs.security;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.stereotype.Component;


@Component
public class JWTTokenAuthenticator {

  private final ClientRegistrationRepository clientRegistrationRepository;
  private final ConfigurableJWTProcessor<SecurityContext> jwtProcessor;
  // The expected JWS algorithm of the access tokens
  private final String jwsAlgorithm;

  public JWTTokenAuthenticator(@Autowired ClientRegistrationRepository clientRegistrationRepository,
      @Value("${application.security.client}") String clientId,
      @Value("${application.security.jws-algorithm:RS256}") String jwsAlgorithm) {
    this.clientRegistrationRepository = clientRegistrationRepository;
    this.jwsAlgorithm = jwsAlgorithm;
    jwtProcessor = getSecurityContextConfigurableJWTProcessor(clientId);
  }

  public JWTClaimsSet process(String accessToken) {
    try {
      return jwtProcessor.process(accessToken, null);
    } catch (JOSEException | BadJOSEException | ParseException e) {
      throw new SecurityException(e);
    }
  }

  private ConfigurableJWTProcessor<SecurityContext> getSecurityContextConfigurableJWTProcessor(
      String clientId) {
    ConfigurableJWTProcessor<SecurityContext> jwtProcessor = new DefaultJWTProcessor<>();
    jwtProcessor.setJWTClaimsSetVerifier(new DefaultJWTClaimsVerifier<>());
    JWKSource<SecurityContext> keySource = getSecurityContextJWKSource(clientId);

    JWSAlgorithm expectedJWSAlg = JWSAlgorithm.parse(jwsAlgorithm);

// Configure the JWT processor with a key selector to feed matching public
// RSA keys sourced from the JWK set URL
    JWSKeySelector<SecurityContext> keySelector = new JWSVerificationKeySelector<>(expectedJWSAlg,
        keySource);

    jwtProcessor.setJWSKeySelector(keySelector);

// Set the required JWT claims for access tokens issued by the Connect2id
// server, may differ with other servers
    jwtProcessor.setJWTClaimsSetVerifier(new DefaultJWTClaimsVerifier());

    return jwtProcessor;
  }


  /**
   * The public RSA keys to validate the signatures will be sourced from the OAuth 2.0 server's JWK
   * set, published at a well-known URL. The RemoteJWKSet object caches the retrieved keys to speed
   * up subsequent look-ups and can also handle key-rollover
   *
   * @param clientId
   * @return JWKSource according to selected client id (property application.security.client)
   */
  private JWKSource<SecurityContext> getSecurityContextJWKSource(String clientId) {
    ClientRegistration sc = clientRegistrationRepository.findByRegistrationId(clientId);
    return getSecurityContextJWKSource(sc);
  }

  private JWKSource<SecurityContext> getSecurityContextJWKSource(ClientRegistration sc) {
    try {
      URL jwkSetURL = new URL(sc.getProviderDetails()
          .getJwkSetUri());
      return new RemoteJWKSet<>(jwkSetURL);
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }

}
