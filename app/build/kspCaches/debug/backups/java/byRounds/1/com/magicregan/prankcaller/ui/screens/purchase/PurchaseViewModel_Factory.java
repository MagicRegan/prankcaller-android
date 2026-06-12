package com.magicregan.prankcaller.ui.screens.purchase;

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
public final class PurchaseViewModel_Factory implements Factory<PurchaseViewModel> {
  private final Provider<PrankRepository> repositoryProvider;

  public PurchaseViewModel_Factory(Provider<PrankRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public PurchaseViewModel get() {
    return newInstance(repositoryProvider.get());
  }

  public static PurchaseViewModel_Factory create(Provider<PrankRepository> repositoryProvider) {
    return new PurchaseViewModel_Factory(repositoryProvider);
  }

  public static PurchaseViewModel newInstance(PrankRepository repository) {
    return new PurchaseViewModel(repository);
  }
}
