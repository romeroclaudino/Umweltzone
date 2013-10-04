package de.avpptr.umweltzone.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import de.avpptr.umweltzone.R;
import de.avpptr.umweltzone.utils.MapDrawer;
import de.avpptr.umweltzone.utils.PointsProvider;

public class MapFragment extends BaseFragment {

    private GoogleMap mMap;
    private MapDrawer mMapDrawer;
    private final GoogleMap.OnCameraChangeListener mOnCameraChangeListener;

    public MapFragment() {
        this.mOnCameraChangeListener = new OnCameraChangeListener();
    }

    @Override public int getLayoutResource() {
        return R.layout.fragment_map;
    }

    @Override public void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    private void setUpMapIfNeeded() {
        if (mMap == null) {
            FragmentActivity activity = getActivity();
            Context context = activity.getApplicationContext();
            int connectionResult = GooglePlayServicesUtil
                    .isGooglePlayServicesAvailable(context);
            if (connectionResult != ConnectionResult.SUCCESS) {
                showGooglePlayServicesErrorDialog(activity, connectionResult);
            } else {
                mMap = ((SupportMapFragment) activity.getSupportFragmentManager()
                        .findFragmentById(R.id.map)).getMap();
                if (mMap != null) {
                    onMapIsSetUp();
                }
            }
        }
    }

    private void showGooglePlayServicesErrorDialog(Activity activity, int errorCode) {
        final Dialog dialog = GooglePlayServicesUtil.getErrorDialog(errorCode, activity, 0);
        if (dialog == null) {
            Log.e(activity.getClass().getName(), "GooglePlayServicesErrorDialog is null.");
        } else {
            dialog.show();
        }
    }

    protected void onMapIsSetUp() {
        mMapDrawer = new MapDrawer(mMap);
        mMap.setOnCameraChangeListener(mOnCameraChangeListener);
        mMap.setMyLocationEnabled(true);
    }


    private void drawPolygonOverlay() {
        Iterable<LatLng> points = PointsProvider.getPoints(PointsProvider.Location.Berlin);
        Resources resources = getResources();
        int fillColor = resources.getColor(R.color.shape_fill_color);
        int strokeColor = resources.getColor(R.color.shape_stroke_color);
        int strokeWidth = resources.getInteger(R.integer.shape_stroke_width);
        mMapDrawer.drawPolygon(points, fillColor, strokeColor, strokeWidth);
    }

    class OnCameraChangeListener implements GoogleMap.OnCameraChangeListener {
        @Override public void onCameraChange(CameraPosition cameraPosition) {
            drawPolygonOverlay();
        }
    }

}