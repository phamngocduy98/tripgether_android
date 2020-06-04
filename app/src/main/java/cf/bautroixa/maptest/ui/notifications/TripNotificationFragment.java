package cf.bautroixa.maptest.ui.notifications;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SortedList;
import androidx.recyclerview.widget.SortedListAdapterCallback;

import java.util.Objects;

import cf.bautroixa.maptest.R;
import cf.bautroixa.maptest.model.firestore.ModelManager;
import cf.bautroixa.maptest.model.firestore.TripNotification;
import cf.bautroixa.maptest.ui.adapter.TripNotificationAdapter;

/**
 * A simple {@link Fragment} subclass.
 */
public class TripNotificationFragment extends Fragment implements NotificationActivity.ActivityNavigationInterfaceOwner {
    private ModelManager manager;
    private RecyclerView rvNotifications;
    private TripNotificationAdapter adapter;
    private SortedList<TripNotification> tripNotificationSortedList;
    private NotificationActivity.ActivityNavigationInterface activityNavigationInterface;

    public TripNotificationFragment() {
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        manager = ModelManager.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_trip_notification, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rvNotifications = view.findViewById(R.id.rv_notifications);
        rvNotifications.setAdapter(adapter);
        rvNotifications.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false));
    }

    // Called On Attach
    @Override
    public void setActivityNavigationInterface(NotificationActivity.ActivityNavigationInterface activityNavigationInterface) {
        this.activityNavigationInterface = activityNavigationInterface;
        adapter = new TripNotificationAdapter(requireContext(), activityNavigationInterface);
        tripNotificationSortedList = new SortedList<>(TripNotification.class, new SortedListAdapterCallback<TripNotification>(adapter) {
            @Override
            public int compare(TripNotification o1, TripNotification o2) {
                return -o1.getTime().compareTo(o2.getTime());
            }

            @Override
            public boolean areContentsTheSame(TripNotification oldItem, TripNotification newItem) {
                // UserNotifications are not changed once created;
                return Objects.equals(oldItem.getId(), newItem.getId()) && Objects.equals(oldItem.isSeen(), newItem.isSeen());
            }

            @Override
            public boolean areItemsTheSame(TripNotification item1, TripNotification item2) {
                return item1.getId().equals(item2.getId());
            }
        });
        adapter.setTripNotifications(tripNotificationSortedList);
        manager.getCurrentTrip().getTripNotificationsManager().attachSortedList(this, tripNotificationSortedList);
    }
}
