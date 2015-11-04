package com.se_au.stars;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;


public class LeaderboardsProvider {
    private GoogleApiClient mGoogleApiClient;
    public String mGlobalLeaderboardId;

    public LeaderboardsProvider(GoogleApiClient googleApiClient){
        mGoogleApiClient = googleApiClient;

        // TODO: Доставать из ресурсов
        mGlobalLeaderboardId = "CgkI2ajnr7MaEAIQAQ";
    }

    public String GetGlobalLeaderboardId(){
        return mGlobalLeaderboardId;
    }

    public void Submit(double result){
        if(!mGoogleApiClient.isConnected()){
            return;
        }

        Games.Leaderboards.submitScore(mGoogleApiClient,
                mGlobalLeaderboardId,
                (long) (100 * result));
    }
}
