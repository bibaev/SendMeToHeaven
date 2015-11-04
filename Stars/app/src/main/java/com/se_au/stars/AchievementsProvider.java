package com.se_au.stars;

import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;

public class AchievementsProvider {
    private GoogleApiClient mGoogleApiClient;
    private String WelcomeToTheClubId;
    private String StartSmall;
    private String AreYouCrazy;
    private String UsainBolt;
    private String BrakePhone;

    public AchievementsProvider(GoogleApiClient googleApiClient){
        mGoogleApiClient = googleApiClient;

        // TODO: Доставать из ресурсов.
        AreYouCrazy = "CgkI2ajnr7MaEAIQBA";
        StartSmall = "CgkI2ajnr7MaEAIQBQ";
        WelcomeToTheClubId = "CgkI2ajnr7MaEAIQBg";
        UsainBolt = "CgkI2ajnr7MaEAIQAw";
        BrakePhone = "CgkI2ajnr7MaEAIQAg";
    }

    public void Submit(float result){
        if(!mGoogleApiClient.isConnected()){
            return;
        }

        try {
            Games.Achievements.increment(mGoogleApiClient, UsainBolt, 1);
            Games.Achievements.unlock(mGoogleApiClient, StartSmall);

            if (result >= 1.0) {
                Games.Achievements.unlock(mGoogleApiClient, WelcomeToTheClubId);
                if (result >= 2.) {
                    Games.Achievements.unlock(mGoogleApiClient, BrakePhone);
                    if (result >= 20.) {
                        Games.Achievements.unlock(mGoogleApiClient, AreYouCrazy);
                    }
                }
            }
        }
        catch (Exception e)
        {
            Log.d("ERROR", "Achievement send failed.");
            Log.d("ERROR", e.toString());
        }
    }
}
