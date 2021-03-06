package org.ligi.passandroid.ui;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import javax.annotation.Nonnull;
import org.ligi.passandroid.R;
import org.ligi.passandroid.model.pass.Pass;
import org.ligi.passandroid.model.pass.PassLocation;

public class NavigateToLocationsDialog {

    public static void perform(@Nonnull final Activity activity, @Nonnull final Pass pass, final boolean finishOnDone) {
        final List<PassLocation> locations = pass.getLocations();

        if (locations.size() == 0) {
            done(activity, finishOnDone);
        } else if (locations.size() == 1) {
            startIntentForLocation(activity, locations.get(0), pass);
            done(activity, finishOnDone);
        } else if (locations.size() > 1) {
            final String[] locationDescriptions = new String[locations.size()];

            int i = 0;
            for (PassLocation loc : locations) {
                locationDescriptions[i++] = loc.getNameWithFallback(pass);
            }
            new AlertDialog.Builder(activity).setTitle(activity.getString(R.string.choose_location))
                    .setItems(locationDescriptions, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startIntentForLocation(activity, locations.get(which), pass);
                            done(activity, finishOnDone);
                        }
                    })
                    .show();

        }
    }

    private static void done(Activity activity, boolean finishOnDone) {
        if (finishOnDone) {
            activity.finish();
        }
    }

    private static void startIntentForLocation(Activity activity, PassLocation location, @Nonnull final Pass pass) {
        final Intent i = new Intent(Intent.ACTION_VIEW);

        final String description = getEncodedDescription(location, pass);

        final String latAndLonStr = location.getLat() + "," + location.getLon();
        i.setData(Uri.parse("geo:" + latAndLonStr + "?q=" + latAndLonStr + "(" + description + ")"));
        try {
            activity.startActivity(i);
        } catch (ActivityNotFoundException e) {
            i.setData(Uri.parse("http://maps.google.com/?q=" + description + "@" + location.getLat() + "," + location.getLon()));
            activity.startActivity(i);
            // TODO also the browser could not be found -> handle
        }
    }

    private static String getEncodedDescription(final PassLocation location, @Nonnull final Pass pass) {
        try {
            return URLEncoder.encode(location.getNameWithFallback(pass), "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            // OK - no description
            return "";
        }
    }

}
