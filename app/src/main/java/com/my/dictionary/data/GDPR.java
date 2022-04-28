package com.my.dictionary.data;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.google.ads.consent.ConsentForm;
import com.google.ads.consent.ConsentFormListener;
import com.google.ads.consent.ConsentInfoUpdateListener;
import com.google.ads.consent.ConsentInformation;
import com.google.ads.consent.ConsentStatus;
import com.google.ads.consent.DebugGeography;
import com.my.dictionary.BuildConfig;
import com.my.dictionary.R;

import java.net.MalformedURLException;
import java.net.URL;

public class GDPR {

    public static Bundle getBundleAd(Activity act) {
        Bundle extras = new Bundle();
        ConsentInformation consentInformation = ConsentInformation.getInstance(act);
        if (consentInformation.getConsentStatus().equals(ConsentStatus.NON_PERSONALIZED)) {
            extras.putString("npa", "1");
        }
        return extras;
    }

    public static void updateConsentStatus(final Activity act) {
        ConsentInformation consentInformation = ConsentInformation.getInstance(act);
        // for debug needed
        if(BuildConfig.DEBUG){
            ConsentInformation.getInstance(act).addTestDevice("5417CD40436EA9EEDB1801BAF429F990");
            consentInformation.setDebugGeography(DebugGeography.DEBUG_GEOGRAPHY_EEA);
        }
        consentInformation.requestConsentInfoUpdate(new String[]{act.getString(R.string.publisher_id)}, new ConsentInfoUpdateListener() {
            @Override
            public void onConsentInfoUpdated(ConsentStatus consentStatus) {
                // User's consent status successfully updated. Display the consent form if Consent Status is UNKNOWN
                if (consentStatus == ConsentStatus.UNKNOWN) {
                    new GDPRForm(act).displayConsentForm();
                }
            }

            @Override
            public void onFailedToUpdateConsentInfo(String errorDescription) {
                // Consent form error.
                Log.e("GDPR", errorDescription);

            }
        });
    }

    private static class GDPRForm {

        private ConsentForm form;

        private Activity activity;

        private GDPRForm(Activity act) {
            activity = act;
        }

        private void displayConsentForm() {
            ConsentForm.Builder builder = new ConsentForm.Builder(activity, getUrlPrivacyPolicy(activity));
            builder.withPersonalizedAdsOption();
            builder.withNonPersonalizedAdsOption();
            builder.withListener(new ConsentFormListener() {
                @Override
                public void onConsentFormLoaded() {
                    // Consent form loaded successfully.
                    form.show();
                }

                @Override
                public void onConsentFormOpened() {
                    // Consent form was displayed.
                }

                @Override
                public void onConsentFormClosed(ConsentStatus consentStatus, Boolean userPrefersAdFree) {
                    // Consent form was closed.
                    Log.e("GDPR", "Status : " + consentStatus);
                }

                @Override
                public void onConsentFormError(String errorDescription) {
                    // Consent form error.
                    Log.e("GDPR", errorDescription);
                }
            });
            form = builder.build();
            form.load();
        }

        private URL getUrlPrivacyPolicy(Activity act) {
            URL mUrl = null;
            try {
                mUrl = new URL(act.getString(R.string.privacy_policy_url));
            } catch (MalformedURLException e) {
                Log.e("GDPR", e.getMessage());
            }
            return mUrl;
        }
    }


}
