package com.chonkcheck.android.di

import com.chonkcheck.android.BuildConfig
import com.chonkcheck.android.data.api.DiaryApi
import com.chonkcheck.android.data.api.ExerciseApi
import com.chonkcheck.android.data.api.FoodApi
import com.chonkcheck.android.data.api.NutritionLabelApi
import com.chonkcheck.android.data.api.RecipeApi
import com.chonkcheck.android.data.api.SavedMealApi
import com.chonkcheck.android.data.api.UserApi
import com.chonkcheck.android.data.api.WeightApi
import com.chonkcheck.android.data.api.interceptor.AuthInterceptor
import com.chonkcheck.android.data.api.interceptor.TokenAuthenticator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
        coerceInputValues = true
    }

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        tokenAuthenticator: TokenAuthenticator,
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .authenticator(tokenAuthenticator)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        json: Json
    ): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.API_URL)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    @Provides
    @Singleton
    fun provideFoodApi(retrofit: Retrofit): FoodApi =
        retrofit.create(FoodApi::class.java)

    @Provides
    @Singleton
    fun provideDiaryApi(retrofit: Retrofit): DiaryApi =
        retrofit.create(DiaryApi::class.java)

    @Provides
    @Singleton
    fun provideWeightApi(retrofit: Retrofit): WeightApi =
        retrofit.create(WeightApi::class.java)

    @Provides
    @Singleton
    fun provideRecipeApi(retrofit: Retrofit): RecipeApi =
        retrofit.create(RecipeApi::class.java)

    @Provides
    @Singleton
    fun provideUserApi(retrofit: Retrofit): UserApi =
        retrofit.create(UserApi::class.java)

    @Provides
    @Singleton
    fun provideSavedMealApi(retrofit: Retrofit): SavedMealApi =
        retrofit.create(SavedMealApi::class.java)

    @Provides
    @Singleton
    fun provideExerciseApi(retrofit: Retrofit): ExerciseApi =
        retrofit.create(ExerciseApi::class.java)

    @Provides
    @Singleton
    fun provideNutritionLabelApi(retrofit: Retrofit): NutritionLabelApi =
        retrofit.create(NutritionLabelApi::class.java)
}
