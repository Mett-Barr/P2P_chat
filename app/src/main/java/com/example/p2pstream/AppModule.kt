package com.example.p2pstream

import android.app.Application
import android.content.Context
import com.example.p2pstream.chatlogic.MessageHandler
import com.example.p2pstream.network.MyConnectionInfoListener
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent


@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    fun provideMyConnectionInfoListener(): MyConnectionInfoListener {
        return MyConnectionInfoListener
    }

    @Provides
    fun provideMessageHandler(): MessageHandler {
        return MessageHandler
    }


    @Provides
    fun provideContext(application: Application): Context {
        return application.applicationContext
    }
}