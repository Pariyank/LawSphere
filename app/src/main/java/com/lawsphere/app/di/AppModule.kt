package com.lawsphere.app.di

import com.google.ai.client.generativeai.GenerativeModel
import com.lawsphere.app.data.api.LawApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

//    private const val BASE_URL = "http://10.0.2.2:3000/"
//     private const val BASE_URL = "http://10.165.41.156:3000/"
     private const val BASE_URL = "https://lawsphere-backend-xvr1.onrender.com/"

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideLawApi(retrofit: Retrofit): LawApi {
        return retrofit.create(LawApi::class.java)
    }

    @Provides
    @Singleton
    fun provideGeminiModel(): GenerativeModel {
        return GenerativeModel(
            modelName = "gemini-2.5-flash",
            apiKey = "AIzaSyCBeph2v44ASUf6ivIt7sn07zbwRcl1VFA"
        )
    }
}