package com.se_au.stars;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;

import static android.support.v4.app.ActivityCompat.startActivityForResult;


public class LeaderboardsProvider {
    private String mGlobalLeaderboardId;
    private GoogleApiClient mGoogleApiClient;

    private int REQUEST_LEADERBOARD = 0;
    public LeaderboardsProvider(GoogleApiClient googleApiClient){
        mGoogleApiClient = googleApiClient;

    }
}
