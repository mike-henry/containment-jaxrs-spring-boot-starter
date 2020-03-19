package com.spx.containment.general.jaxrs;


import com.spx.containment.general.jaxrs.mapping.ApplicationJsonProvider;
import com.spx.containment.general.jaxrs.security.filters.ApplicationRequestFilter;

import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.jaxrs.config.SwaggerConfigLocator;
import io.swagger.jaxrs.config.SwaggerContextService;
import io.swagger.jaxrs.listing.ApiListingResource;
import java.lang.annotation.Annotation;
import java.util.Set;
import javax.websocket.server.ServerEndpoint;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import org.glassfish.jersey.server.ResourceConfig;
import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@ApplicationPath(JerseyConfig.BASE_PATH)
public class JerseyConfig extends ResourceConfig {

  static final String BASE_PATH = "/api";
  private static final String BASE_PACKAGE = "com.spx.containment.core.api";

  @Autowired
  public JerseyConfig() {

    BeanConfig swaggerConfig = new BeanConfig();
    swaggerConfig.setBasePath(BASE_PATH);
    swaggerConfig.setResourcePackage(BASE_PACKAGE);
    SwaggerConfigLocator.getInstance()
        .putConfig(SwaggerContextService.CONFIG_ID_DEFAULT, swaggerConfig);

    register(JacksonFeature.class);

    findTypes(ApplicationRequestFilter.class).forEach(type -> register(type));
    findAnnotated(ServerEndpoint.class).forEach(ws->register(ws));
    packages(true, getClass().getPackage()
        .getName(), ApiListingResource.class.getPackage()
        .getName());
  }

  private <T> Set<Class<? extends T>> findTypes(Class<T> superType) {
    Reflections reflections = getReflections();
    return reflections.getSubTypesOf(superType);
  }


  private <T extends Annotation> Set<Class<?>> findAnnotated(Class<T> annotation) {
    Reflections reflections = getReflections();
    return reflections.getTypesAnnotatedWith(annotation);
  }

  private Reflections getReflections() {
    return new Reflections(new ConfigurationBuilder().setUrls(ClasspathHelper.forPackage(
        getClass().getPackage()
            .getName())));
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



