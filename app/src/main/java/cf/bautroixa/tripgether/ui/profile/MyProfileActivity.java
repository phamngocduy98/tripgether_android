package cf.bautroixa.tripgether.ui.profile;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import cf.bautroixa.tripgether.R;
import cf.bautroixa.tripgether.model.firestore.ModelManager;
import cf.bautroixa.tripgether.model.sharedpref.SharedPrefKeys;
import cf.bautroixa.tripgether.ui.friends.FriendListActivity;
import cf.bautroixa.tripgether.ui.settings.SettingActivity;
import cf.bautroixa.tripgether.ui.theme.OneAppbarActivity;
import cf.bautroixa.tripgether.ui.theme.OneDialog;
import cf.bautroixa.tripgether.utils.ui_utils.ImageHelper;

public class MyProfileActivity extends OneAppbarActivity implements Toolbar.OnMenuItemClickListener {
    GoogleSignInClient mGoogleSignInClient;
    private ModelManager manager;
    private SharedPreferences sharedPref;

    private TextView tvUserName;
    private ImageView imgAvatar;

    private Switch switchService;

    public MyProfileActivity() {
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_tab_profile);
        setToolbarMenu(R.menu.fragment_profile);
        getToolbar().setOnMenuItemClickListener(this);

        manager = ModelManager.getInstance(this);

        imgAvatar = findViewById(R.id.appbar_img_avatar);
        switchService = findViewById(R.id.switch_toggle_service);
        LinearLayout mPersonalInformationLinear = findViewById(R.id.ln_personal_information);
        LinearLayout mFriendListLinear = findViewById(R.id.ln_friend_list);
        LinearLayout mChangePasswordLinear = findViewById(R.id.ln_change_password);
        LinearLayout mLogoutLinear = findViewById(R.id.ln_logout);

        imgAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MyProfileActivity.this, DetailProfileActivity.class);
                startActivity(intent);
            }
        });
        mPersonalInformationLinear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MyProfileActivity.this, DetailProfileActivity.class);
                startActivity(intent);
            }
        });
        mFriendListLinear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MyProfileActivity.this, FriendListActivity.class));
            }
        });
        mChangePasswordLinear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MyProfileActivity.this, ChangePasswordActivity.class);
                startActivity(intent);
            }
        });
        mLogoutLinear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new OneDialog.Builder().title(R.string.dialog_title_confirm_logout).message(R.string.dialog_message_logout)
                        .enableNegativeButton(true)
                        .buttonClickListener(new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == DialogInterface.BUTTON_POSITIVE) {
                                    logout();
                                }
                                dialog.dismiss();
                            }
                        }).show(getSupportFragmentManager(), "logout dialog");
            }
        });

        sharedPref = this.getSharedPreferences(getString(R.string.shared_preference_name), Context.MODE_PRIVATE);
        switchService.setChecked(sharedPref.getBoolean("SERVICE_ON", false));
        switchService.setText(switchService.isChecked() ? R.string.switch_toggle_service_on : R.string.switch_toggle_service_off);
        switchService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (switchService.isChecked()) {
//                    AlarmHelper.turnOn(MyProfileActivity.this, sharedPref);
                    sharedPref.edit().putBoolean(SharedPrefKeys.SETTING_SERVICE_ON, true).commit();
                    switchService.setText(R.string.switch_toggle_service_on);
                } else {
//                    AlarmHelper.turnOff(MyProfileActivity.this, sharedPref);
                    sharedPref.edit().putBoolean(SharedPrefKeys.SETTING_SERVICE_ON, false).commit();
                    switchService.setText(R.string.switch_toggle_service_off);
                }
            }
        });

        String googleClientId = "703604566706-upp9g9rtcdh3adrflqcgddt4p712jh27.apps.googleusercontent.com";
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(googleClientId)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    @Override
    public void onResume() {
        super.onResume();
        String avatarUrl = "";
        if (manager.isLoggedIn()) {
            if (!avatarUrl.equals(manager.getCurrentUser().getAvatar())) {
                ImageHelper.loadCircleImage(manager.getCurrentUser().getAvatar(), imgAvatar, 100, 100);
            }
            setTitle(manager.getCurrentUser().getName());
        }
    }

    private void logout() {
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(MyProfileActivity.this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        manager.logout();
                    }
                });

    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_setup_frag_profile:
                startActivity(new Intent(MyProfileActivity.this, SettingActivity.class));
                break;
        }
        return false;
    }
}
