package com.magicregan.prankcaller.ui.screens.calls;

import com.magicregan.prankcaller.data.repository.PrankRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
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
public final class CallsViewModel_Factory implements Factory<CallsViewModel> {
  private final Provider<PrankRepository> repositoryProvider;

  public CallsViewModel_Factory(Provider<PrankRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public CallsViewModel get() {
    return newInstance(repositoryProvider.get());
  }

  public static CallsViewModel_Factory create(Provider<PrankRepository> repositoryProvider) {
    return new CallsViewModel_Factory(repositoryProvider);
  }

  public static CallsViewModel newInstance(PrankRepository repository) {
    return new CallsViewModel(repository);
  }
}
