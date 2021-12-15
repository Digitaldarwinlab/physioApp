package com.darwin.physioai.coreapp.di

import android.content.Context
import com.example.physioai.data.network.RemoteDataSource
import com.example.taskmotl.data.Network.ApiCall.UserApi
import com.darwin.physioai.coreapp.utils.CShowProgress
import com.darwin.physioai.coreapp.utils.SessionManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    fun providesAuthApi(remoteDataSource: RemoteDataSource): UserApi {
        return remoteDataSource.buildApi(UserApi::class.java)
    }

    @Provides
    fun provideSharedPrefs(@ApplicationContext context: Context): SessionManager {
        return SessionManager(context)
    }

    @Provides
    fun provideProgressDialog(@ApplicationContext context: Context) : CShowProgress {
        return CShowProgress(context)
    }
}