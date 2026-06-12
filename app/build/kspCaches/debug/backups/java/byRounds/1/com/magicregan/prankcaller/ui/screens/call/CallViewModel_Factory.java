package com.magicregan.prankcaller.ui.screens.call;

import androidx.lifecycle.SavedStateHandle;
import androidx.media3.exoplayer.ExoPlayer;
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
public final class CallViewModel_Factory implements Factory<CallViewModel> {
  private final Provider<SavedStateHandle> savedStateHandleProvider;

  private final Provider<PrankRepository> repositoryProvider;

  private final Provider<ExoPlayer> exoPlayerProvider;

  public CallViewModel_Factory(Provider<SavedStateHandle> savedStateHandleProvider,
      Provider<PrankRepository> repositoryProvider, Provider<ExoPlayer> exoPlayerProvider) {
    this.savedStateHandleProvider = savedStateHandleProvider;
    this.repositoryProvider = repositoryProvider;
    this.exoPlayerProvider = exoPlayerProvider;
  }

  @Override
  public CallViewModel get() {
    return newInstance(savedStateHandleProvider.get(), repositoryProvider.get(), exoPlayerProvider.get());
  }

  public static CallViewModel_Factory create(Provider<SavedStateHandle> savedStateHandleProvider,
      Provider<PrankRepository> repositoryProvider, Provider<ExoPlayer> exoPlayerProvider) {
    return new CallViewModel_Factory(savedStateHandleProvider, repositoryProvider, exoPlayerProvider);
  }

  public static CallViewModel newInstance(SavedStateHandle savedStateHandle,
      PrankRepository repository, ExoPlayer exoPlayer) {
    return new CallViewModel(savedStateHandle, repository, exoPlayer);
  }
}
