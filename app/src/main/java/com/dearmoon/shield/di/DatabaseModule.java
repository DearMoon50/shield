package com.dearmoon.shield.di;

import android.content.Context;
import com.dearmoon.shield.alert.AlertManager;
import com.dearmoon.shield.database.ShieldDatabase;
import com.dearmoon.shield.database.SystemEventDao;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import javax.inject.Singleton;

@Module
@InstallIn(SingletonComponent.class)
public class DatabaseModule {
    
    @Provides
    @Singleton
    public ShieldDatabase provideDatabase(@ApplicationContext Context context) {
        return ShieldDatabase.getInstance(context);
    }
    
    @Provides
    public SystemEventDao provideSystemEventDao(ShieldDatabase database) {
        return database.systemEventDao();
    }
    
    @Provides
    @Singleton
    public AlertManager provideAlertManager(@ApplicationContext Context context) {
        return new AlertManager(context);
    }
}
