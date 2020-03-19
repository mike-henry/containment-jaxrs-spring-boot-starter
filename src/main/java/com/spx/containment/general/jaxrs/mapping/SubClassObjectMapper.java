package com.spx.containment.general.jaxrs.mapping;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

@Slf4j
public class SubClassObjectMapper extends ObjectMapper {

  private final static String PACKAGE = "com.spx";
  private final String rootPackage;

  public SubClassObjectMapper() {
    super();
    rootPackage = PACKAGE;
    registerSubtypes();
  }

  SubClassObjectMapper(String rootPackage) {
    super();
    this.rootPackage = rootPackage;
    registerSubtypes();
  }

  public Set<Class<?>> findClassesToRegister() {
    Reflections reflections = new Reflections(
        new ConfigurationBuilder().setUrls(ClasspathHelper.forPackage(PACKAGE)));
    return reflections.getTypesAnnotatedWith(JsonTypeInfo.class);
  }

  private void registerSubtypes() {
    findClassesToRegister().stream()
        .forEach(type -> {
          JsonTypeInfo jsonTypeInfo = type.getAnnotation(JsonTypeInfo.class);
          log.debug("registering type {} as {} in json mapper", type.getName(),
              type.getSimpleName());
          registerSubtypes(new NamedType(type, type.getSimpleName()));
        });
  }
}
