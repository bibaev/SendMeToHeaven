package com.se_au.stars;

import com.google.android.gms.common.api.GoogleApiClient;

public class AchievementsProvider {
    private boolean mAchievementsAvailable;
    private GoogleApiClient mGoogleApiClient;

    public AchievementsProvider(GoogleApiClient googleApiClient){
        mGoogleApiClient = googleApiClient;
    }
}
