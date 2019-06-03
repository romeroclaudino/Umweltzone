/*
 *  Copyright (C) 2019  Tobias Preuss
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.avpptr.umweltzone.map;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import org.parceler.Parcels;

import java.util.List;

import de.avpptr.umweltzone.BuildConfig;
import de.avpptr.umweltzone.R;
import de.avpptr.umweltzone.Umweltzone;
import de.avpptr.umweltzone.analytics.Tracking;
import de.avpptr.umweltzone.analytics.TrackingPoint;
import de.avpptr.umweltzone.models.AdministrativeZone;
import de.avpptr.umweltzone.utils.IntentHelper;
import de.avpptr.umweltzone.utils.SnackBarHelper;

public class ZoneNotDrawableDialog extends DialogFragment {

    private final Tracking mTracking;

    public static final String FRAGMENT_TAG = BuildConfig.APPLICATION_ID + "." +
            ZoneNotDrawableDialog.class.getSimpleName();

    private static final String BUNDLE_KEY_ADMINISTRATIVE_ZONE =
            BuildConfig.APPLICATION_ID + ".ADMINISTRATIVE_ZONE";

    public static ZoneNotDrawableDialog newInstance(@NonNull AdministrativeZone administrativeZone) {
        ZoneNotDrawableDialog dialog = new ZoneNotDrawableDialog();
        Bundle extras = new Bundle();
        extras.putParcelable(BUNDLE_KEY_ADMINISTRATIVE_ZONE, Parcels.wrap(administrativeZone));
        dialog.setArguments(extras);
        return dialog;
    }

    public ZoneNotDrawableDialog() {
        mTracking = Umweltzone.getTracker();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final FragmentActivity activity = requireActivity();
        Bundle extras = getArguments();
        Parcelable parcelable = extras.getParcelable(BUNDLE_KEY_ADMINISTRATIVE_ZONE);
        final AdministrativeZone administrativeZone = Parcels.unwrap(parcelable);
        if (administrativeZone == null) {
            throw new NullPointerException("Recent low emission zone is null.");
        }
        final String zoneDisplayName = administrativeZone.displayName;

        // Prepare layout
        LayoutInflater inflater = activity.getLayoutInflater();
        View zoneNotDrawableView = inflater.inflate(R.layout.fragment_zone_not_drawable, null);
        TextView noticeTextView = zoneNotDrawableView
                .findViewById(R.id.zone_not_drawable_notice);
        Spanned noticeSpanned = Html.fromHtml(getString(
                R.string.zone_not_drawable_notice,
                zoneDisplayName,
                zoneDisplayName));
        noticeTextView.setText(noticeSpanned, TextView.BufferType.SPANNABLE);

        final String[] toRecipients = getToRecipients(administrativeZone.contactEmails);

        // Launch dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(zoneNotDrawableView)
                .setTitle(R.string.zone_not_drawable_title)
                .setPositiveButton(R.string.zone_not_drawable_open_email, (dialog, whichButton) -> {
                    Intent intent = IntentHelper.getSendEmailIntent(
                            activity,
                            toRecipients,
                            getBccRecipients(),
                            getEmailSubject(zoneDisplayName),
                            getEmailMessage(zoneDisplayName)
                    );
                    mTracking.track(TrackingPoint.ZoneNotDrawableOpenEmailClick,
                            administrativeZone.name);
                    if (intent.resolveActivity(activity.getPackageManager()) != null) {
                        startActivity(Intent.createChooser(intent, getString(
                                R.string.zone_not_drawable_app_chooser_title)));
                    } else {
                        SnackBarHelper.showError(activity, R.id.map_view,
                                R.string.zone_not_drawable_no_email_app);
                    }
                })
                .setNegativeButton(R.string.zone_not_drawable_later, (dialog, whichButton) -> {
                    // Nothing to do here
                    mTracking.track(TrackingPoint.ZoneNotDrawableLaterClick,
                            administrativeZone.name);
                });
        return builder.create();
    }

    @NonNull
    private String[] getToRecipients(@Nullable List<String> contactEmails) {
        if (contactEmails == null || contactEmails.isEmpty()) {
            throw new AssertionError("Contact emails cannot be null or empty.");
        }
        String toRecipients = TextUtils.join(", ", contactEmails);
        return new String[]{toRecipients};
    }

    @NonNull
    private String[] getBccRecipients() {
        return new String[]{getString(R.string.config_contact_email_address)};
    }

    @NonNull
    private String getEmailSubject(String zoneName) {
        return getString(R.string.zone_not_drawable_email_subject, zoneName);
    }

    @NonNull
    private String getEmailMessage(String zoneName) {
        return getString(R.string.zone_not_drawable_email_html_message, zoneName);
    }

}
