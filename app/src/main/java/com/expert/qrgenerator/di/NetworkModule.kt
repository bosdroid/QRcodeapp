package com.expert.qrgenerator.di

import com.expert.qrgenerator.retrofit.ApiRepository
import com.expert.qrgenerator.retrofit.ApiRepositoryImpl
import com.expert.qrgenerator.retrofit.ApiServices
import com.expert.qrgenerator.retrofit.RetrofitClientApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {

    @Provides
    fun provideApiRepository(apiServices: ApiServices): ApiRepository {
        return ApiRepositoryImpl(apiServices)
    }

    @Provides
    fun provideRequestsApi(): ApiServices {
        return RetrofitClientApi.getInstance().create(ApiServices::class.java)
    }

}