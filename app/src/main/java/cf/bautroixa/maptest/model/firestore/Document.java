package cf.bautroixa.maptest.model.firestore;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.WriteBatch;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;

public abstract class Document implements Serializable {
    @Exclude
    public static final String ID = "id";
    @Exclude
    protected static final String TAG = "DataClass";
    @Exclude
    protected String id;
    @Exclude
    @Nullable
    protected OnValueChangedListener initListener = null;
    @Exclude
    DocumentReference ref;
    @Exclude
    private ListenerRegistration listenerRegistration;
    @Exclude
    private Class klass;
    @Exclude
    private ArrayList<OnValueChangedListener> onNewValueListeners = new ArrayList<>();
    @Exclude
    private boolean isAvailable = false;

    public Document() {
    }

    @Exclude
    public static <T extends Document> T newInstance(Class<T> klass, DocumentSnapshot documentSnapshot) {
        T data = (T) documentSnapshot.toObject(klass);
        data.withRef(documentSnapshot.getReference()).withClass(klass);
        data.setAvailable(true);
        return data;
    }

    @Exclude
    public <T extends Document> T withClass(Class<T> klass) {
        this.klass = klass;
        return (T) this;
    }

    @Exclude
    public Class getKlass() {
        return klass;
    }

    @Exclude
    public String getId() {
        return id;
    }

    @Exclude
    public DocumentReference getRef() {
        return ref;
    }

    @Exclude
    public <T extends Document> T withId(String id) {
        this.id = id;
        return (T) this;
    }

    @Exclude
    public <T extends Document> T withRef(@NonNull DocumentReference ref) {
        this.ref = ref;
        this.id = ref.getId();
        return (T) this;
    }

    @Exclude
    public boolean isAvailable() {
        return isAvailable;
    }

    @Exclude
    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    @Exclude
    protected abstract void update(Document document);

    @Exclude
    public void cancelListenerRegistration() {
        if (listenerRegistration != null) listenerRegistration.remove();
    }

    @Exclude
    public void addOnNewValueListener(OnValueChangedListener listener) {
        this.onNewValueListeners.add(listener);
        if (this.listenerRegistration == null)
            Log.e(TAG, "addOnNewValueListener before setListenerRegistration (it show that an activity or fragment attach listener before setListenerRegistration)");
        listener.onValueChanged(this);
    }

    @Exclude
    public ArrayList<OnValueChangedListener> getListeners() {
        return this.onNewValueListeners;
    }

    @Exclude
    public void attachListener(LifecycleOwner lifecycleOwner, final OnValueChangedListener listener) {
        lifecycleOwner.getLifecycle().addObserver(new LifecycleObserver() {
            @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
            public void connectListener() {
                addOnNewValueListener(listener);
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
            public void disconnectListener() {
                removeOnNewValueListener(listener);
            }
        });
    }

    @Exclude
    public void restoreListeners(ArrayList<OnValueChangedListener> backupListeners) {
        this.onNewValueListeners.addAll(backupListeners);
        for (int i = 0; i < backupListeners.size(); i++) {
            backupListeners.get(i).onValueChanged(this);
        }
    }

    @Exclude
    public void removeOnNewValueListener(OnValueChangedListener listener) {
        this.onNewValueListeners.remove(listener);
    }


    @Exclude
    public void setListenerRegistration(@Nullable final DocumentsManager dataManager, @Nullable final OnValueChangedListener initListener) {
        this.initListener = initListener;
        final Document thisDocument = this;
        this.listenerRegistration = this.ref.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.e(TAG, "Listen failed.", e);
                }
                Log.d(TAG + klass.getName(), "onDocumentSnapshot");
                if (documentSnapshot != null) {
                    if (documentSnapshot.exists()) {
                        isAvailable = true;
                        update(newInstance(klass, documentSnapshot));
                        if (dataManager != null) dataManager.put(thisDocument);
                    } else {
                        if (dataManager != null) {
                            dataManager.remove(thisDocument.getId());
                        } else {
                            onRemove();
                        }
                    }
                    if (initListener != null) {
                        initListener.onValueChanged(thisDocument);
                    }
                    for (OnValueChangedListener listener : onNewValueListeners) {
                        listener.onValueChanged(thisDocument);
                    }
                }
            }
        });
    }

    @Exclude
    public void onRemove() {
        this.isAvailable = false;
        for (OnValueChangedListener listener : onNewValueListeners) {
            listener.onValueChanged(this);
        }
        cancelListenerRegistration();
    }

    @Exclude
    public Task<Void> sendDelete(@Nullable WriteBatch batch) {
        if (batch != null) {
            batch.delete(this.ref);
            TaskCompletionSource<Void> source = new TaskCompletionSource<Void>();
            return source.getTask();
        }
        return this.ref.delete();
    }

    @Exclude
    public Task<Void> sendUpdate(@Nullable WriteBatch batch, @NonNull String field, @Nullable Object value, Object... moreFieldsAndValues) {
        if (batch != null) {
            batch.update(this.ref, field, value, moreFieldsAndValues);
            TaskCompletionSource<Void> source = new TaskCompletionSource<Void>();
            return source.getTask();
        }
        return this.ref.update(field, value, moreFieldsAndValues);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        Document document = (Document) obj;
        return Objects.equals(id, document.getId()) && Objects.equals(ref, document.getRef());
    }

    public interface OnValueChangedListener<T extends Document> {
        /**
         * onNewData
         *
         * @param data new value
         */
        void onValueChanged(@NonNull T data);
    }
}