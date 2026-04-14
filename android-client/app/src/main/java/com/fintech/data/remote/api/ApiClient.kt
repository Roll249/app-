package com.fintech.data.remote.api

import com.fintech.BuildConfig
import com.fintech.data.local.datastore.PreferencesManager
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Network module for Hilt DI
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            .setLenient()
            .create()
    }

    @Provides
    @Singleton
    fun provideAuthInterceptor(preferencesManager: PreferencesManager): Interceptor {
        return Interceptor { chain ->
            val token = runBlocking { preferencesManager.accessToken.first() }
            val request = chain.request().newBuilder().apply {
                if (!token.isNullOrEmpty()) {
                    addHeader("Authorization", "Bearer $token")
                }
                addHeader("Content-Type", "application/json")
                addHeader("Accept", "application/json")
            }.build()
            chain.proceed(request)
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: Interceptor): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, gson: Gson): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    // Auth API
    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): com.fintech.data.remote.api.services.AuthApi {
        return retrofit.create(com.fintech.data.remote.api.services.AuthApi::class.java)
    }

    // User API
    @Provides
    @Singleton
    fun provideUserApi(retrofit: Retrofit): com.fintech.data.remote.api.services.UserApi {
        return retrofit.create(com.fintech.data.remote.api.services.UserApi::class.java)
    }

    // Account API
    @Provides
    @Singleton
    fun provideAccountApi(retrofit: Retrofit): com.fintech.data.remote.api.services.AccountApi {
        return retrofit.create(com.fintech.data.remote.api.services.AccountApi::class.java)
    }

    // Transaction API
    @Provides
    @Singleton
    fun provideTransactionApi(retrofit: Retrofit): com.fintech.data.remote.api.services.TransactionApi {
        return retrofit.create(com.fintech.data.remote.api.services.TransactionApi::class.java)
    }

    // Category API
    @Provides
    @Singleton
    fun provideCategoryApi(retrofit: Retrofit): com.fintech.data.remote.api.services.CategoryApi {
        return retrofit.create(com.fintech.data.remote.api.services.CategoryApi::class.java)
    }

    // Fund API
    @Provides
    @Singleton
    fun provideFundApi(retrofit: Retrofit): com.fintech.data.remote.api.services.FundApi {
        return retrofit.create(com.fintech.data.remote.api.services.FundApi::class.java)
    }

    // Budget API
    @Provides
    @Singleton
    fun provideBudgetApi(retrofit: Retrofit): com.fintech.data.remote.api.services.BudgetApi {
        return retrofit.create(com.fintech.data.remote.api.services.BudgetApi::class.java)
    }

    // Bank API
    @Provides
    @Singleton
    fun provideBankApi(retrofit: Retrofit): com.fintech.data.remote.api.services.BankApi {
        return retrofit.create(com.fintech.data.remote.api.services.BankApi::class.java)
    }

    // QR Code API
    @Provides
    @Singleton
    fun provideQRCodeApi(retrofit: Retrofit): com.fintech.data.remote.api.services.QRCodeApi {
        return retrofit.create(com.fintech.data.remote.api.services.QRCodeApi::class.java)
    }

    // Report API
    @Provides
    @Singleton
    fun provideReportApi(retrofit: Retrofit): com.fintech.data.remote.api.services.ReportApi {
        return retrofit.create(com.fintech.data.remote.api.services.ReportApi::class.java)
    }

    // AI API
    @Provides
    @Singleton
    fun provideAIApi(retrofit: Retrofit): com.fintech.data.remote.api.services.AIApi {
        return retrofit.create(com.fintech.data.remote.api.services.AIApi::class.java)
    }

    // Services API
    @Provides
    @Singleton
    fun provideServicesApi(retrofit: Retrofit): com.fintech.data.remote.services.ServicesApi {
        return retrofit.create(com.fintech.data.remote.services.ServicesApi::class.java)
    }

    // Service Manager
    @Provides
    @Singleton
    fun provideServiceManager(
        servicesApi: com.fintech.data.remote.services.ServicesApi,
        preferencesManager: PreferencesManager
    ): com.fintech.data.remote.services.ServiceManager {
        return com.fintech.data.remote.services.ServiceManager(servicesApi, preferencesManager)
    }

    // Market API
    @Provides
    @Singleton
    fun provideMarketApi(retrofit: Retrofit): com.fintech.data.remote.market.MarketApi {
        return retrofit.create(com.fintech.data.remote.market.MarketApi::class.java)
    }

    // Market Manager
    @Provides
    @Singleton
    fun provideMarketManager(
        marketApi: com.fintech.data.remote.market.MarketApi
    ): com.fintech.data.remote.market.MarketManager {
        return com.fintech.data.remote.market.MarketManager(marketApi)
    }

    // Savings Goal API
    @Provides
    @Singleton
    fun provideSavingsGoalApi(retrofit: Retrofit): com.fintech.data.remote.api.services.SavingsGoalApi {
        return retrofit.create(com.fintech.data.remote.api.services.SavingsGoalApi::class.java)
    }
}
