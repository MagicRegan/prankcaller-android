package com.magicregan.prankcaller.data.repository;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast"
})
public final class PrankRepository_Factory implements Factory<PrankRepository> {
  @Override
  public PrankRepository get() {
    return newInstance();
  }

  public static PrankRepository_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static PrankRepository newInstance() {
    return new PrankRepository();
  }

  private static final class InstanceHolder {
    private static final PrankRepository_Factory INSTANCE = new PrankRepository_Factory();
  }
}
