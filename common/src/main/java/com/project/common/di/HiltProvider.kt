package com.project.common.di

import android.content.Context
import com.project.common.remote_config.RemoteConfigRepo
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class HiltProvider {

    @Singleton
    @Provides
    fun provideConfigRepo(
        @ApplicationContext context: Context,
    ): RemoteConfigRepo = RemoteConfigRepo(
        context
    )

}