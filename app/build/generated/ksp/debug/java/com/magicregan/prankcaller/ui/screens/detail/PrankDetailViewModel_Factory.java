package com.magicregan.prankcaller.ui.screens.detail;

import androidx.lifecycle.SavedStateHandle;
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
public final class PrankDetailViewModel_Factory implements Factory<PrankDetailViewModel> {
  private final Provider<SavedStateHandle> savedStateHandleProvider;

  private final Provider<PrankRepository> repositoryProvider;

  public PrankDetailViewModel_Factory(Provider<SavedStateHandle> savedStateHandleProvider,
      Provider<PrankRepository> repositoryProvider) {
    this.savedStateHandleProvider = savedStateHandleProvider;
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public PrankDetailViewModel get() {
    return newInstance(savedStateHandleProvider.get(), repositoryProvider.get());
  }

  public static PrankDetailViewModel_Factory create(
      Provider<SavedStateHandle> savedStateHandleProvider,
      Provider<PrankRepository> repositoryProvider) {
    return new PrankDetailViewModel_Factory(savedStateHandleProvider, repositoryProvider);
  }

  public static PrankDetailViewModel newInstance(SavedStateHandle savedStateHandle,
      PrankRepository repository) {
    return new PrankDetailViewModel(savedStateHandle, repository);
  }
}
