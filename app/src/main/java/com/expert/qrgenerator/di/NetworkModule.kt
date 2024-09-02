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
object NetworkModule {

    /**
     * Provides an instance of [ApiRepository] by using [ApiServices].
     * This is the implementation of the repository that communicates with the API.
     *
     * @param apiServices Instance of [ApiServices] to be used for network requests.
     * @return Instance of [ApiRepository].
     */
    @Provides
    fun provideApiRepository(apiServices: ApiServices): ApiRepository {
        return ApiRepositoryImpl(apiServices)
    }

    /**
     * Provides an instance of [ApiServices] for network operations.
     * This is used to create a Retrofit client instance for API communication.
     *
     * @return Instance of [ApiServices] for making API requests.
     */
    @Provides
    fun provideRequestsApi(): ApiServices {
        return RetrofitClientApi.getInstance().create(ApiServices::class.java)
    }
}
