package com.spx.containment.general.jaxrs;


import com.spx.containment.general.jaxrs.mapping.ApplicationJsonProvider;
import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.jaxrs.config.SwaggerConfigLocator;
import io.swagger.jaxrs.config.SwaggerContextService;
import io.swagger.jaxrs.listing.ApiListingResource;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.websocket.server.ServerEndpoint;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.ReaderInterceptor;
import javax.ws.rs.ext.WriterInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.glassfish.jersey.server.ResourceConfig;
import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@ApplicationPath(JerseyConfig.BASE_PATH)
@Slf4j
public class JerseyConfig extends ResourceConfig {

  static final String BASE_PATH = "/api";
  private static final String BASE_PACKAGE = "com.spx";
  private final Set<String> allPackages;

  @Autowired
  public JerseyConfig(
      @Value("${application.webservices.base-packages:com.spx}") Set<String> basePackages) {
    this.allPackages = getAllPackages(basePackages);
    BeanConfig swaggerConfig = new BeanConfig();
    swaggerConfig.setBasePath(BASE_PATH);
    swaggerConfig.setResourcePackage(BASE_PACKAGE);
    SwaggerConfigLocator.getInstance()
        .putConfig(SwaggerContextService.CONFIG_ID_DEFAULT, swaggerConfig);

    register(JacksonFeature.class);

    findTypes(ContainerRequestFilter.class).forEach(type -> register(type));
    findTypes(ReaderInterceptor.class).forEach(type -> register(type));
    findTypes(WriterInterceptor.class).forEach(type -> register(type));
    findTypes(ContainerResponseFilter.class).forEach(type -> register(type));
    findTypes(ExceptionMapper.class).forEach(type -> register(type));

    findAnnotated(ServerEndpoint.class).forEach(ws -> register(ws));
    findAnnotated(Provider.class).forEach(type -> register(type));

    applicationPackages();

  }

  private <T> Set<Class<? extends T>> findTypes(Class<T> superType) {
    Reflections reflections = getReflections();
    return reflections.getSubTypesOf(superType);
  }


  private Set<String> getAllPackages(Set<String> basePackages) {
    Set<String> allPackages = new HashSet<String>(basePackages);
    allPackages.add("com.spx.containment.general");
    return allPackages;
  }

  private void applicationPackages() {
    allPackages.forEach(pkg -> {
      log.debug("Scanning Package: {}", pkg);
      packages(true, pkg, ApiListingResource.class.getPackage()
          .getName());
    });
  }

  private <T extends Annotation> Set<Class<?>> findAnnotated(Class<T> annotation) {
    Reflections reflections = getReflections();
    return reflections.getTypesAnnotatedWith(annotation);
  }

  private Reflections getReflections() {
    return new Reflections(new ConfigurationBuilder().setUrls(getUrlsForPackages()));
  }

  private Collection<URL> getUrlsForPackages() {
    final Set<URL> result = new HashSet<>();
    allPackages.forEach(pkg -> result.addAll(ClasspathHelper.forPackage(pkg)));
    return result;
  }

  static class JacksonFeature implements Feature {

    @Override
    public boolean configure(FeatureContext context) {
      context.register(new ApplicationJsonProvider(), MessageBodyReader.class,
          MessageBodyWriter.class);
      return true;
    }
  }


}



