package com.dearmoon.shield;

import android.app.Application;
import com.dearmoon.shield.di.WorkManagerScheduler;
import dagger.hilt.android.HiltAndroidApp;
import javax.inject.Inject;

@HiltAndroidApp
public class ShieldApplication extends Application {
    @Inject WorkManagerScheduler workManagerScheduler;
}
