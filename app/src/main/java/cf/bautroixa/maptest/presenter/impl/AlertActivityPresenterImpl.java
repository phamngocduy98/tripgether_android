package cf.bautroixa.maptest.presenter.impl;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import cf.bautroixa.maptest.model.firestore.ModelManager;
import cf.bautroixa.maptest.model.firestore.Notification;
import cf.bautroixa.maptest.model.firestore.TripNotification;
import cf.bautroixa.maptest.model.firestore.User;
import cf.bautroixa.maptest.model.firestore.UserNotification;
import cf.bautroixa.maptest.model.types.FcmNotification;
import cf.bautroixa.maptest.presenter.AlertActivityPresenter;

public class AlertActivityPresenterImpl implements AlertActivityPresenter {
    private Context context;
    private View view;
    private ModelManager manager;

    public AlertActivityPresenterImpl(Context context, View view) {
        this.manager = ModelManager.getInstance();
        this.context = context;
        this.view = view;
    }

    @Override
    public void handleIntent(@Nullable Bundle bundle) {
        if (bundle != null) {
            String notificationId = bundle.getString(FcmNotification.NOTI_REF_ID, null);
            String type = bundle.getString(FcmNotification.NOTI_TYPE, "null event type");
            if (notificationId != null) {
                if (Notification.TripType.tripTypes.contains(type)) {
                    manager.getCurrentTrip().getTripNotificationsManager().requestGet(notificationId).addOnCompleteListener(new OnCompleteListener<TripNotification>() {
                        @Override
                        public void onComplete(@NonNull Task<TripNotification> task) {
                            if (task.isSuccessful()) {
                                TripNotification tripNotification = task.getResult();
                                view.setUpView(tripNotification);
                            }
                        }
                    });
                } else if (Notification.UserType.userTypes.contains(type)) {
                    manager.getCurrentUser().getUserNotificationsManager().requestGet(notificationId).addOnCompleteListener(new OnCompleteListener<UserNotification>() {
                        @Override
                        public void onComplete(@NonNull Task<UserNotification> task) {
                            if (task.isSuccessful()) {
                                UserNotification userNotification = task.getResult();
                                view.setUpView(userNotification);
                            }
                        }
                    });
                }
            } else {
                String priority = bundle.getString(FcmNotification.NOTI_PRIORITY, "low");
                String messageParams = bundle.getString(FcmNotification.NOTI_MESSAGE_PARAMS, "");
                List<String> messageParamsList = Arrays.asList(messageParams.split(","));
                Notification notification = new Notification(type, User.DEFAULT_AVATAR, messageParamsList, new Timestamp(new Date()));
                view.setUpView(notification);
            }
        }
    }
}