package cf.bautroixa.tripgether.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SortedList;

import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import java.util.List;

import cf.bautroixa.tripgether.R;
import cf.bautroixa.tripgether.interfaces.NavigationInterface;
import cf.bautroixa.tripgether.model.firestore.objects.Checkpoint;
import cf.bautroixa.tripgether.model.firestore.objects.SosRequest;
import cf.bautroixa.tripgether.model.firestore.objects.User;
import cf.bautroixa.tripgether.model.firestore.objects.Visit;
import cf.bautroixa.tripgether.model.repo.objects.UserPublic;
import cf.bautroixa.tripgether.presenter.bottomspace.BottomMembersPresenterImpl;
import cf.bautroixa.tripgether.ui.adapter.pager_adapter.MainActivityPagerAdapter;
import cf.bautroixa.tripgether.ui.chat.ChatActivity;
import cf.bautroixa.tripgether.ui.dialogs.SosRequestViewDialogFragment;
import cf.bautroixa.tripgether.ui.friends.ProfileActivity;
import cf.bautroixa.tripgether.ui.map.TabMapFragment;
import cf.bautroixa.tripgether.ui.theme.RoundedImageView;
import cf.bautroixa.tripgether.utils.ui_utils.DateFormatter;
import cf.bautroixa.tripgether.utils.ui_utils.ImageHelper;

public class BottomMembersAdapter extends RecyclerView.Adapter<BottomMembersAdapter.MemberViewHolder> {
    BottomMembersPresenterImpl bottomMembersPresenter;
    NavigationInterface navigationInterface;
    FragmentManager fragmentManager;

    SortedList<User> members;
    Checkpoint activeCheckpoint;

    public BottomMembersAdapter(BottomMembersPresenterImpl bottomMembersPresenter, NavigationInterface navigationInterface, FragmentManager fm) {
        this.bottomMembersPresenter = bottomMembersPresenter;
        this.navigationInterface = navigationInterface;
        this.fragmentManager = fm;
    }

    public void setMembers(SortedList<User> members) {
        this.members = members;
    }

    public void setActiveCheckpoint(Checkpoint activeCheckpoint) {
        this.activeCheckpoint = activeCheckpoint;
    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MemberViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_member_full, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        holder.bind(members.get(position));
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (payloads.size() > 0) { // Payload.UPDATE_HEIGHT
            int itemMeasuredHeight = holder.itemView.getMeasuredHeight();
//            if (itemMeasuredHeight > rv.getMeasuredHeight()) {
            // try to set minHeight to extend view height
            holder.itemView.setMinimumHeight(itemMeasuredHeight);
//            }
        } else {
            super.onBindViewHolder(holder, position, payloads);
        }
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    public interface Payload {
        int UPDATE_HEIGHT = 0;
    }

    /**
     * ViewHoldder
     */
    public class MemberViewHolder extends RecyclerView.ViewHolder {
        View itemView;
        TextView tvName, tvLocation, tvStatus, tvSos, tvBattery;
        CircularProgressBar progressBattery;
        RoundedImageView imgSos;
        ImageView imgAvatar, imgCheckedIn, imgMoving, imgLocation, imgInAccurateLocation;
        LinearLayout linearSos, linearLocation;
        String currentAvatar = "";

        private Button btnDirection;
        private ImageButton btnCall, btnMessage;

        public MemberViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            imgAvatar = itemView.findViewById(R.id.img_avatar_item_member);
            tvName = itemView.findViewById(R.id.tv_name_bottom_item_member);
            tvLocation = itemView.findViewById(R.id.tv_location_item_member);
            tvSos = itemView.findViewById(R.id.tv_sos_message_item_member);
            tvBattery = itemView.findViewById(R.id.tv_battery_item_member);
            progressBattery = itemView.findViewById(R.id.progress_battery_item_member);
            imgCheckedIn = itemView.findViewById(R.id.img_checked_in_item_member);
            imgSos = itemView.findViewById(R.id.img_sos_item_member);
            imgMoving = itemView.findViewById(R.id.img_moving_item_member);
            imgLocation = itemView.findViewById(R.id.img_location_item_member);
            imgInAccurateLocation = itemView.findViewById(R.id.img_inaccurate_location_item_member);

            linearSos = itemView.findViewById(R.id.linear_sos_message_item_member);
            linearLocation = itemView.findViewById(R.id.linear_location_item_member);

            btnCall = itemView.findViewById(R.id.btn_call_frag_friend);
            btnDirection = itemView.findViewById(R.id.btn_direction_frag_friend);
            btnMessage = itemView.findViewById(R.id.btn_message_frag_friend);
        }

        public void bind(final User user) {
            final Context context = itemView.getContext();
            final UserPublic userPublic = new UserPublic(user);
            if (!currentAvatar.equals(user.getAvatar())) {
                ImageHelper.loadCircleImage(user.getAvatar(), imgAvatar, 100, 100);
                currentAvatar = user.getAvatar();
            }
            tvName.setText(user.getName());

            View.OnClickListener avatarNameOnClick = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, ProfileActivity.class);
                    intent.putExtra(ProfileActivity.ARG_USER_PUBLIC_DATA, (Parcelable) userPublic);
                    context.startActivity(intent);
                }
            };
            imgAvatar.setOnClickListener(avatarNameOnClick);
            tvName.setOnClickListener(avatarNameOnClick);

            // Location
            if (user.getLocationAccuracy() > 50f) { // > accuracy > 50m
                imgLocation.setImageResource(R.drawable.ic_no_gps);
                tvLocation.setText(String.format("Vị trí ước tính gần %s", user.getCurrentLocation()));
                imgInAccurateLocation.setVisibility(View.VISIBLE);
                imgMoving.setVisibility(View.GONE);
            } else if (user.getLastUpdate() != null && System.currentTimeMillis() - user.getLastUpdate().toDate().getTime() > 10L * 60 * 1000) { // lastUpdate > 10 min ago
                imgLocation.setImageResource(R.drawable.ic_no_gps);
                tvLocation.setText(String.format("Vị trí cuối cùng %s gần %s", DateFormatter.format(user.getLastUpdate()), user.getCurrentLocation()));
                imgInAccurateLocation.setVisibility(View.VISIBLE);
                imgMoving.setVisibility(View.GONE);
            } else {
                imgInAccurateLocation.setVisibility(View.GONE);
                if (user.getSpeed() > 0) {
                    imgLocation.setImageResource(R.drawable.ic_directions_bike_black_24dp);
                    tvLocation.setText(String.format("Đang di chuyển %d m/s gần %s", user.getSpeed(), user.getCurrentLocation()));
                    imgMoving.setVisibility(View.VISIBLE);
                } else {
                    imgLocation.setImageResource(R.drawable.ic_place_black_24dp);
                    tvLocation.setText(user.getCurrentLocation());
                    imgMoving.setVisibility(View.GONE);
                }
            }

            tvBattery.setText(String.format("%d%%", user.getBattery()));
            progressBattery.setProgress(user.getBattery());
            if (user.getBattery() <= 20) {
                progressBattery.setProgressBarColor(Color.RED);
                tvBattery.setTextColor(Color.RED);
            } else {
                progressBattery.setProgressBarColor(context.getResources().getColor(R.color.colorText));
                tvBattery.setTextColor(context.getResources().getColor(R.color.colorText));
            }
            // SOS
            SosRequest userSosRequest = user.getSosRequest();
            if (userSosRequest != null && !userSosRequest.isResolved()) {
                imgSos.setVisibility(View.VISIBLE);
                linearSos.setVisibility(View.VISIBLE);
                tvSos.setText(userSosRequest.getDescription());
                linearSos.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SosRequestViewDialogFragment.newInstance(user.getId()).show(fragmentManager, "sos viewer");
                    }
                });
            } else {
                imgSos.setVisibility(View.GONE);
                linearSos.setVisibility(View.GONE);
            }
            // Current location / Diem danh
            if (activeCheckpoint != null) {
                Visit userVisit = activeCheckpoint.getVisitsManager().get(user.getId());
                if (userVisit != null) {
                    imgCheckedIn.setVisibility(View.VISIBLE);
                    if (bottomMembersPresenter.isReadyToCheckIn()) {
                        imgLocation.setImageResource(R.drawable.ic_ticker);
                        tvLocation.setText(String.format("Đang có mặt tại %s từ %s", activeCheckpoint.getName(), DateFormatter.format(userVisit.getTime())));
                    }
                } else {
                    imgCheckedIn.setVisibility(View.GONE);
                }
            }
            // buttons
            bindButtons(user);
        }

        public void bindButtons(final User user) {
            final Context context = itemView.getContext();
            btnCall.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + user.getPhoneNumber()));
                    context.startActivity(intent);
                }
            });
            btnDirection.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    navigationInterface.navigate(MainActivityPagerAdapter.Tabs.TAB_MAP, TabMapFragment.STATE_ROUTE, user.getLatLng());
                }
            });
            btnMessage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, ChatActivity.class);
                    intent.putExtra(ChatActivity.ARG_TO_USER_ID, user.getId());
                    context.startActivity(intent);
                }
            });
        }
    }
}