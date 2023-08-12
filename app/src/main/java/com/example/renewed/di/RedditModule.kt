package com.example.renewed.di

import android.content.Context
import android.content.SharedPreferences

import com.example.renewed.*
import com.example.renewed.Room.FavoritesDAO
import com.example.renewed.Room.T3DAO
import com.example.renewed.Room.T5DAO
import com.example.renewed.repos.BaseFavoritesRepo
import com.example.renewed.moshiadapters.DescriptionAdapter
import com.example.renewed.moshiadapters.MediaList
import com.example.renewed.moshiadapters.RedditHolderAdapter
import com.example.renewed.moshiadapters.RedditPostAdapter
import com.example.renewed.repos.BaseSubredditsAndPostsRepo
import com.example.renewed.repos.FavoritesRepo
import com.example.renewed.repos.SubredditsAndPostsRepo
import com.google.android.exoplayer2.ExoPlayer
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class SharedPreferencesModule {
    @Singleton
    @Provides
    fun provideSharedPreference(@ApplicationContext context: Context): SessionManager {
        return SessionManager(context)
    }
}

@InstallIn(SingletonComponent::class)
@Module
object RedditModule {
    private const val BASE_URL="https://oauth.reddit.com/"

    @Singleton
    @Provides
    fun provideAPICallInterface(retrofit: Retrofit): API =
        retrofit.create(API::class.java)


    @Singleton
    @Provides
    fun provideLoginInterface(retrofit: Retrofit): AuthAPI =
        retrofit.create(AuthAPI::class.java)

    @Singleton
    @Provides
    fun provideRetrofit(okHttpClient: OkHttpClient, mosh: Moshi): Retrofit =
        Retrofit.Builder()
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create(mosh))
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .baseUrl(BASE_URL)
                .build()

    @Singleton
    @Provides
    fun provideOkHttpClient(httpLoggingInterceptor: HttpLoggingInterceptor,
                            redirect: RedirectInterceptor,auth:AuthInterceptor):OkHttpClient =

            OkHttpClient.Builder()
                        .followRedirects(false)
                        .followSslRedirects(false)
                        .addInterceptor(redirect)
                        .addInterceptor(auth)
                        .addInterceptor(httpLoggingInterceptor)
                        .build()

    @Singleton
    @Provides
    fun provideHttpLoggingInterceptor() =
        HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.HEADERS }

    @Singleton
    @Provides
    fun provideRedirectInterceptor() = RedirectInterceptor()

    @Singleton
    @Provides
    fun provideAuthInterceptor(sm:SessionManager) = AuthInterceptor(sm)

    @Singleton
    @Provides
    fun provideMoshi(): Moshi = Moshi.Builder()
        .add(RedditPostAdapter())
        .add(RedditHolderAdapter())
        .add(DescriptionAdapter())
        .add(MediaList())
        .build()
}

@InstallIn(SingletonComponent::class)
@Module
object RepoModule {
    @Singleton
    @Provides
    fun providePostsRepository(t5Dao: T5DAO, t3Dao: T3DAO,api: API ):
            BaseSubredditsAndPostsRepo = SubredditsAndPostsRepo( t5Dao, t3Dao,api)
}

@InstallIn(SingletonComponent::class)
@Module
object ExoPlayerModule {
    @Singleton
    @Provides
    fun provideExoplayer(@ApplicationContext ctx: Context): ExoPlayer =
                                                                    ExoPlayer.Builder(ctx).build()
}

@InstallIn(SingletonComponent::class)
@Module
object FavsRepoModule {
    @Singleton
    @Provides
    fun provideFavoritesRepository(t5Dao: T5DAO, t3Dao: T3DAO, favs: FavoritesDAO, api:API)
                                : BaseFavoritesRepo = FavoritesRepo(t5Dao, t3Dao,favs, api)
}