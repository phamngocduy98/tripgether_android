package cf.bautroixa.maptest.ui.adapter;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.SortedList;

import com.google.firebase.firestore.FieldValue;

import java.util.Objects;

import cf.bautroixa.maptest.R;
import cf.bautroixa.maptest.model.firestore.Checkpoint;
import cf.bautroixa.maptest.model.firestore.ModelManager;
import cf.bautroixa.maptest.model.firestore.Notification;
import cf.bautroixa.maptest.model.firestore.TripNotification;
import cf.bautroixa.maptest.model.firestore.User;
import cf.bautroixa.maptest.model.firestore.UserNotification;
import cf.bautroixa.maptest.ui.map.TabMapFragment;
import cf.bautroixa.maptest.ui.notifications.NotificationActivity;
import cf.bautroixa.maptest.ui.theme.OneRecyclerView;
import cf.bautroixa.maptest.ui.theme.RoundedImageView;
import cf.bautroixa.maptest.utils.DateFormatter;
import cf.bautroixa.maptest.utils.ImageHelper;

public class TripNotificationAdapter extends OneRecyclerView.Adapter<TripNotificationAdapter.NotificationVH> {
    ModelManager manager;
    Context context;
    NotificationActivity.ActivityNavigationInterface navigationInterface;
    private SortedList<TripNotification> tripNotifications;

    public TripNotificationAdapter(Context context, NotificationActivity.ActivityNavigationInterface navigationInterface) {
        this.manager = ModelManager.getInstance();
        this.context = context;
        this.navigationInterface = navigationInterface;
    }

    public void setTripNotifications(SortedList<TripNotification> tripNotifications) {
        this.tripNotifications = tripNotifications;
    }

    @NonNull
    @Override
    public NotificationVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.fragment_tab_notification_item, parent, false);
        return new NotificationVH(v, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationVH holder, final int position) {
        final TripNotification tripNotification = tripNotifications.get(position);
        holder.bind(tripNotification);
    }

    @Override
    public int getItemCount() {
        return tripNotifications.size();
    }

    public class NotificationVH extends OneRecyclerView.ViewHolder {
        View badgeSeen;
        RoundedImageView imgAvatar, imgType;
        TextView tvContent, tvTime;

        public NotificationVH(@NonNull View itemView, int viewType) {
            super(itemView, viewType);
            badgeSeen = itemView.findViewById(R.id.badge_seen_item_noti);
            imgAvatar = itemView.findViewById(R.id.img_avatar_item_noti);
            imgType = itemView.findViewById(R.id.img_type_item_noti);
            tvContent = itemView.findViewById(R.id.tv_content_item_noti);
            tvTime = itemView.findViewById(R.id.tv_time_item_noti);
        }

        public void updateSeen(TripNotification tripNotification) {
            if (!tripNotification.getSeenList().contains(manager.getCurrentUserRef())) {
                tripNotification.sendUpdate(null, TripNotification.SEEN_LIST, FieldValue.arrayUnion(manager.getCurrentUserRef()));
            }
        }

        public void bind(final TripNotification tripNotification) {
            if (tripNotification == null) return;
//            boolean isSeen = tripNotification.getSeenList().contains(manager.getCurrentUserRef());
            boolean isSeen = tripNotification.isSeen();
            tvContent.setText(Html.fromHtml(tripNotification.getRenderedMessage(context, !isSeen)));
            tvTime.setText(DateFormatter.format(tripNotification.getTime()));
            ImageHelper.loadCircleImage(tripNotification.getAvatar(), imgAvatar);
            int typeIndex = Notification.TripType.tripTypes.indexOf(tripNotification.getType());
            imgType.setImageResource(Notification.TripIcon.tripIcons.get(typeIndex));
            badgeSeen.setVisibility(isSeen ? View.INVISIBLE : View.VISIBLE);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateSeen(tripNotification);
                    switch (tripNotification.getType()) {
                        case UserNotification.TripType.USER_SOS_ADDED:
                        case UserNotification.TripType.USER_SOS_RESOLVED:
                        case UserNotification.TripType.USER_ADDED:
                        case UserNotification.TripType.USER_REMOVED:
                            if (!tripNotification.getType().equals(UserNotification.TripType.USER_REMOVED)) {
                                User user = manager.getCurrentTrip().getMembersManager().get(Objects.requireNonNull(tripNotification.getUserRef()).getId());
                                navigationInterface.navigate(MainActivityPagerAdapter.Tabs.TAB_MAP, TabMapFragment.STATE_MEMBER_STATUS, user);
                            }
                            break;
                        case UserNotification.TripType.CHECKPOINT_GATHER_REQUEST:
                        case UserNotification.TripType.CHECKPOINT_ADDED:
                        case UserNotification.TripType.CHECKPOINT_REMOVED:
                            if (!tripNotification.getType().equals(UserNotification.TripType.CHECKPOINT_REMOVED)) {
                                Checkpoint checkpoint = manager.getCurrentTrip().getCheckpointsManager().get(Objects.requireNonNull(tripNotification.getCheckpointRef()).getId());
                                navigationInterface.navigate(MainActivityPagerAdapter.Tabs.TAB_MAP, TabMapFragment.STATE_CHECKPOINT, checkpoint);
                            }
                            break;
                        case UserNotification.TripType.TRIP_JOIN_REQUEST:
                            // TODO: handle here
                            break;
                    }
                }
            });
        }
    }
}