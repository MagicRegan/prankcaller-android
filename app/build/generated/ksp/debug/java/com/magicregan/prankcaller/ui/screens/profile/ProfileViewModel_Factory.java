package com.magicregan.prankcaller.ui.screens.profile;

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
public final class ProfileViewModel_Factory implements Factory<ProfileViewModel> {
  private final Provider<PrankRepository> repositoryProvider;

  public ProfileViewModel_Factory(Provider<PrankRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public ProfileViewModel get() {
    return newInstance(repositoryProvider.get());
  }

  public static ProfileViewModel_Factory create(Provider<PrankRepository> repositoryProvider) {
    return new ProfileViewModel_Factory(repositoryProvider);
  }

  public static ProfileViewModel newInstance(PrankRepository repository) {
    return new ProfileViewModel(repository);
  }
}
