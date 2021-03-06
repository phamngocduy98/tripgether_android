package cf.bautroixa.tripgether.presenter.bottomspace;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;

import java.util.List;

import cf.bautroixa.tripgether.model.firestore.objects.Checkpoint;
import cf.bautroixa.tripgether.model.firestore.objects.User;
import cf.bautroixa.tripgether.ui.map.MapBackgroundFragment;

public interface MapPresenter {
    void initOnMapLoaded(GoogleMap googleMap);

    void initLocationService();

    interface View {
        MapBackgroundFragment.UserMarkerViewHolder createUserMarker(final User user);

        MapBackgroundFragment.CheckpointMarkerViewHolder createCheckpointMarker(final Checkpoint checkpoint, final int checkpointIndex);

        Marker createTempMarker(LatLng latLng, String title, String snippet);

        void setMapNightMode(GoogleMap googleMap);

        boolean isMapLoaded();

        boolean isMyLocationMarker(Marker marker);

        void targetPoint(LatLng latLng, int bottomSpaceHeight);

        void targetCamera(LatLngBounds bounds, int bottomSpaceHeight);

        void targetMarker(@NonNull Marker marker);

        void drawRoute(List<LatLng> latLngs, int bottomSpaceHeight);

        void cleanUpTempMarkerAndRoute();

        void onMyLocationChanged(LatLng latLng);

        void onCompassRotate(float azimuth);

        void lockFocusMyLocation(boolean lock);

        @Nullable
        MapBackgroundFragment.UserMarkerViewHolder getUserMarkerView(User user);

        @Nullable
        MapBackgroundFragment.CheckpointMarkerViewHolder getCheckpointMarkerView(Checkpoint checkpoint);

        void clearAllUserMarker();

        void clearAllCheckpointMarker();
    }

    interface CallableMask {
        void targetMyLocation();

        void target(Object data);

        void createTempMarker(LatLng latLng, String title, String snippet);

        void cleanUpTempMarkerAndRoute();

        void drawRoute(@Nullable LatLng fromN, final LatLng to);

        void drawLine(List<LatLng> latlngs);
    }
}
