package cf.bautroixa.tripgether.ui.adapter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;

import cf.bautroixa.tripgether.R;
import cf.bautroixa.tripgether.model.http.UserHttpService;
import cf.bautroixa.tripgether.model.repo.objects.UserPublic;
import cf.bautroixa.tripgether.ui.dialogs.LoadingDialogHelper;
import cf.bautroixa.tripgether.ui.friends.ProfileActivity;
import cf.bautroixa.tripgether.ui.theme.RoundedImageView;
import cf.bautroixa.tripgether.utils.ContactHelper;
import cf.bautroixa.tripgether.utils.IntentHelper;
import cf.bautroixa.tripgether.utils.ui_utils.ImageHelper;

public class SyncContactAdapter extends RecyclerView.Adapter<SyncContactAdapter.ContactVH> {
    ArrayList<ContactHelper.Contact> contacts;
    Context context;
    Activity activity;

    public SyncContactAdapter(ArrayList<ContactHelper.Contact> contacts, Context context, Activity activity) {
        this.contacts = contacts;
        this.context = context;
        this.activity = activity;
    }

    @NonNull
    @Override
    public SyncContactAdapter.ContactVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SyncContactAdapter.ContactVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend_with_action_btn, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull SyncContactAdapter.ContactVH holder, int position) {
        holder.bind(contacts.get(position));
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    class ContactVH extends RecyclerView.ViewHolder {
        TextView tvNameInAvatar, tvOnlineIndicator;
        RoundedImageView imgAvatar;
        TextView tvName, tvInfo;
        Button btnInvite;


        public ContactVH(@NonNull View itemView) {
            super(itemView);
            tvNameInAvatar = itemView.findViewById(R.id.tv_name_item_avatar);
            imgAvatar = itemView.findViewById(R.id.img_avatar_item_avatar);
            tvOnlineIndicator = itemView.findViewById(R.id.tv_online_indicator_item_avatar);

            tvName = itemView.findViewById(R.id.tv_name_status_item_friend);
            tvInfo = itemView.findViewById(R.id.tv_info_item_friend);

            btnInvite = itemView.findViewById(R.id.btn_action_item_friend);
        }

        void bind(final ContactHelper.Contact contact) {
            // online indicator
            tvOnlineIndicator.setVisibility(View.GONE);
            ImageHelper.loadUserAvatar(imgAvatar, tvNameInAvatar, contact);

            tvName.setText(contact.getName());
            tvInfo.setText(contact.getPhoneNumber());

            btnInvite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final ProgressDialog loadingDialog = LoadingDialogHelper.create(context, "Vui lòng đợi");
                    UserHttpService.findUser(null, contact.getPhoneNumber()).addOnCompleteListener(activity, new OnCompleteListener<UserPublic>() {
                        @Override
                        public void onComplete(@NonNull Task<UserPublic> task) {
                            loadingDialog.dismiss();
                            if (task.isSuccessful()) {
                                UserPublic userPublic = task.getResult();
                                Intent intent = new Intent(activity, ProfileActivity.class);
                                intent.putExtra(ProfileActivity.ARG_USER_PUBLIC_DATA, (Parcelable) userPublic);
                                activity.startActivity(intent);
                            } else {
                                IntentHelper.sendSms(context, contact.getPhoneNumber(), "Tải ngay Tripgether tại " + context.getString(R.string.server_host));
                            }
                        }
                    });
                }
            });
        }
    }
}
