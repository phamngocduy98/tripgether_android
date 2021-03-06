package cf.bautroixa.tripgether.presenter.bottomspace;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.recyclerview.widget.SortedList;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Objects;

import cf.bautroixa.tripgether.interfaces.NavigationInterface;
import cf.bautroixa.tripgether.model.firestore.ModelManager;
import cf.bautroixa.tripgether.model.firestore.core.CollectionManager;
import cf.bautroixa.tripgether.model.firestore.core.Document;
import cf.bautroixa.tripgether.model.firestore.core.DocumentsManager;
import cf.bautroixa.tripgether.model.firestore.objects.Checkpoint;
import cf.bautroixa.tripgether.model.firestore.objects.Notification;
import cf.bautroixa.tripgether.model.firestore.objects.Trip;
import cf.bautroixa.tripgether.model.firestore.objects.TripNotification;
import cf.bautroixa.tripgether.model.firestore.objects.Visit;
import cf.bautroixa.tripgether.presenter.bottomspace.BottomCheckpointsPresenter;
import cf.bautroixa.tripgether.ui.adapter.BottomCheckpointsAdapter;
import cf.bautroixa.tripgether.ui.sortedlist.CheckpointSortedListAdapterCallback;

import static cf.bautroixa.tripgether.presenter.bottomspace.BottomCheckpointsPresenter.SavedStateKeys.SAVED_ACTIVE_POS;

public class BottomCheckpointsPresenterImpl implements BottomCheckpointsPresenter, BottomCheckpointsPresenter.CallableMask {
    private static final String TAG = "BottomCheckpointsPresenterImpl";
    Checkpoint activeCheckpoint;
    ModelManager manager;
    BottomCheckpointsAdapter adapter;
    private Context context;
    private View view;
    private int activePos = 0;
    private SortedList<Checkpoint> checkpoints;

    public BottomCheckpointsPresenterImpl(Context context, LifecycleOwner lifecycleOwner, View view) {
        this.context = context;
        this.view = view;
        this.manager = ModelManager.getInstance(context);
        lifecycleOwner.getLifecycle().addObserver(new LifecycleObserver() {
            @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
            public void connectListener() {
                onScrollNewPosition(activePos);
            }
        });
    }

    @Override
    public void initAdapter(LifecycleOwner lifecycleOwner, NavigationInterface navigationInterface) {
        this.adapter = new BottomCheckpointsAdapter(this, navigationInterface);
        this.checkpoints = new SortedList<>(Checkpoint.class, new CheckpointSortedListAdapterCallback(adapter));
        adapter.setCheckpoints(checkpoints);
        manager.getCurrentTrip().getCheckpointsManager().attachSortedList(lifecycleOwner, checkpoints);
        checkpoints.addAll(manager.getCurrentTrip().getCheckpointsManager().getList());
        view.setupAdapter(adapter);
        if (checkpoints.size() == 0) {
            view.setUpTimeLineString(null, null);
        } else if (checkpoints.size() > 1) {
            view.setUpTimeLineString(checkpoints.get(0), checkpoints.get(1));
        } else {
            view.setUpTimeLineString(checkpoints.get(0), null);
        }
        initActiveCheckpointListener(lifecycleOwner);
    }

    public void initActiveCheckpointListener(final LifecycleOwner lifecycleOwner) {
        final DocumentsManager.OnListChangedListener visitListener = new DocumentsManager.OnListChangedListener<Visit>() {
            @Override
            public void onItemInserted(int position, Visit data) {
                adapter.notifyItemChanged(checkpoints.indexOf(activeCheckpoint), getUpdateVisitCountPayload(activeCheckpoint));
            }

            @Override
            public void onItemChanged(int position, Visit data) {

            }

            @Override
            public void onItemRemoved(int position, Visit data) {

            }

            @Override
            public void onDataSetChanged(ArrayList<Visit> datas) {
                adapter.notifyItemChanged(checkpoints.indexOf(activeCheckpoint), getUpdateVisitCountPayload(activeCheckpoint));
            }
        };
        manager.getCurrentTrip().attachListener(lifecycleOwner, new Document.OnValueChangedListener<Trip>() {
            @Override
            public void onValueChanged(@NonNull Trip trip) {
                if (trip.isAvailable()) {
                    manager.getCurrentTrip().getActiveCheckpoint().addOnCompleteListener(new OnCompleteListener<Checkpoint>() {
                        @Override
                        public void onComplete(@NonNull Task<Checkpoint> task) {
                            Checkpoint newActiveCheckpoint = task.getResult();
                            if (Objects.equals(activeCheckpoint, newActiveCheckpoint)) return;
                            if (activeCheckpoint != null)
                                activeCheckpoint.getVisitsManager().removeOnListChangedListener(visitListener);
                            activeCheckpoint = newActiveCheckpoint;
                            if (activeCheckpoint != null) {
                                activeCheckpoint.getVisitsManager().attachListener(lifecycleOwner, visitListener);
                            }
                            adapter.notifyDataSetChanged();
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onScrollNewPosition(int position) {
        Checkpoint checkpoint = checkpoints.get(position);
        int size = checkpoints.size();
        Log.d(TAG, "new pos = " + position + "/" + size + " " + checkpoint.getId());
        if (position + 1 < size) {
            view.setUpTimeLineString(checkpoint, checkpoints.get(position + 1));
        } else {
            view.setUpTimeLineString(checkpoint, null);
        }
        view.onTargetCheckpoint(checkpoint);
    }

    @Override
    public boolean isActiveCheckpoint(Checkpoint checkpoint) {
        return activeCheckpoint != null && activeCheckpoint.getId().equals(checkpoint.getId());
    }

    @Override
    public boolean isReadyToCheckIn(Checkpoint checkpoint) {
        return manager.isReadyToCheckIn(activeCheckpoint);
    }

    @Override
    public boolean isTripLeader() {
        return manager.isTripLeader();
    }

    @Override
    public Task<Void> setActiveCheckpoint(Context context, @Nullable Checkpoint checkpoint) {
        if (checkpoint != null) {
            WriteBatch batch = manager.newWriteBatch();
            manager.getCurrentTrip().sendUpdate(batch, Trip.ACTIVE_CHECKPOINT_REF, checkpoint.getRef());
            manager.getCurrentTrip().getTripNotificationsManager().create(batch, new TripNotification(context, Notification.TripType.CHECKPOINT_GATHER_REQUEST, manager.getCurrentUser(), checkpoint, Notification.Priority.HIGH));
            return batch.commit();
        } else {
            WriteBatch batch = manager.newWriteBatch();
            manager.getCurrentTrip().sendUpdate(batch, Trip.ACTIVE_CHECKPOINT_REF, null);
//            manager.getCurrentTrip().getTripNotificationsManager().create(batch, new TripNotification(context, Notification.TripType.CHECKPOINT_GATHER_REQUEST_END, manager.getCurrentUser(), checkpoint, Notification.Priority.NORMAL));
            return batch.commit();
        }
    }

    @Nullable
    @Override
    public BottomCheckpointsAdapter.UpdateVisitCountPayload getUpdateVisitCountPayload(Checkpoint checkpoint) {
        if (!isActiveCheckpoint(checkpoint)) return null;
        CollectionManager<Visit> visitManager = activeCheckpoint.getVisitsManager();
        boolean isUserCheckedIn = visitManager.contains(manager.getCurrentUser().getId());
        return new BottomCheckpointsAdapter.UpdateVisitCountPayload(isUserCheckedIn, visitManager.getList().size(), manager.getCurrentTrip().getMembers().size());
    }

    @Override
    public Task<DocumentReference> sendCheckIn() {
        return manager.getCurrentTrip().getActiveCheckpoint().continueWithTask(new Continuation<Checkpoint, Task<DocumentReference>>() {
            @Override
            public Task<DocumentReference> then(@NonNull Task<Checkpoint> task) throws Exception {
                if (task.isSuccessful()) {
                    Checkpoint activeCheckpoint = task.getResult();
                    if (isReadyToCheckIn(activeCheckpoint)) {
                        return activeCheckpoint.getVisitsManager().create(new Visit().withRef(manager.getCurrentUser().getRef()));
                    }
                    throw new Exception("User not ready to check in");
                }
                throw task.getException();
            }
        });
    }

    @Override
    public void selectCheckpoint(String checkpointId) {
        manager.getCurrentTrip().getCheckpointsManager().requestGet(checkpointId).addOnCompleteListener(new OnCompleteListener<Checkpoint>() {
            @Override
            public void onComplete(@NonNull Task<Checkpoint> task) {
                if (task.isSuccessful()) {
                    Checkpoint checkpoint = task.getResult();
                    if (checkpoint != null) {
                        activePos = checkpoints.indexOf(checkpoint);
                        if (activePos != -1) view.scrollToPosition(activePos);
                    }
                }
            }
        });
    }

    public Context getContext() {
        return context;
    }

    public Bundle getSavedState() {
        Bundle bundle = new Bundle();
        bundle.putInt(SAVED_ACTIVE_POS, activePos);
        return bundle;
    }
}
