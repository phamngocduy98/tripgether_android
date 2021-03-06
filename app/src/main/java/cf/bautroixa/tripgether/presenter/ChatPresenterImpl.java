package cf.bautroixa.tripgether.presenter;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.SortedList;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;

import java.util.ArrayList;

import cf.bautroixa.tripgether.model.firestore.ModelManager;
import cf.bautroixa.tripgether.model.firestore.core.DocumentsManager;
import cf.bautroixa.tripgether.model.firestore.objects.Discussion;
import cf.bautroixa.tripgether.model.firestore.objects.Message;
import cf.bautroixa.tripgether.model.firestore.objects.User;
import cf.bautroixa.tripgether.model.repo.RepositoryManager;
import cf.bautroixa.tripgether.model.repo.UserRepository;
import cf.bautroixa.tripgether.model.repo.objects.UserPublic;
import cf.bautroixa.tripgether.ui.adapter.ChatAdapter;
import cf.bautroixa.tripgether.ui.sortedlist.MessageSortedListAdapterCallback;

import static cf.bautroixa.tripgether.ui.chat.ChatActivity.ARG_DISCUSSION_ID;
import static cf.bautroixa.tripgether.ui.chat.ChatActivity.ARG_TO_USER_ID;

public class ChatPresenterImpl implements ChatPresenter {
    AppCompatActivity activity;
    Context context;
    View view;
    ModelManager manager;
    UserRepository userRepository;

    Discussion discussion;
    ChatAdapter adapter;
    SortedList<Message> messageSortedList;

    public ChatPresenterImpl(AppCompatActivity activity, Context context, View view) {
        this.activity = activity;
        this.context = context;
        this.view = view;
        this.manager = ModelManager.getInstance(context);
        this.userRepository = RepositoryManager.getInstance(context).getUserRepository();
    }


    @Override
    public boolean init(Bundle bundle) {
        if (bundle != null) {
            String discussionId = bundle.getString(ARG_DISCUSSION_ID, null);
            String toUserId = bundle.getString(ARG_TO_USER_ID, null);
            if (discussionId != null) {
                initDiscussion(discussionId);
                return true;
            } else if (toUserId != null && !toUserId.equals(manager.getCurrentUser().getId())) {
                view.onLoading();
                getOrCreateDiscussion(toUserId).addOnCompleteListener(activity, new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if (task.isSuccessful()) {
                            view.onSuccess();
                            DocumentReference discussionRef = task.getResult();
                            initDiscussion(discussionRef.getId());
                        } else {
                            view.onFailed(task.getException().getMessage());
                        }
                    }
                });
                return true;
            }
        }
        return false;
    }

    private void initDiscussion(String discussionId) {
        manager.getDiscussionsManagers().waitGet(discussionId).addOnCompleteListener(activity, new OnCompleteListener<Discussion>() {
            @Override
            public void onComplete(@NonNull Task<Discussion> task) {
                if (task.isSuccessful()){
                    discussion = task.getResult();
                    view.setupToolbar(discussion);
                    initAdapter();
                    initScroll();
                } else {
                    // TODO: finish activity here
                }
            }
        });
    }

    public Task<DocumentReference> getOrCreateDiscussion(final String toUserId) {
        final DocumentReference toUserRef = manager.getBaseUsersManager().getDocumentReference(toUserId);
        return manager.getDiscussionsManagers().getOrCreateDiscussion(manager.getCurrentUserRef(), toUserRef);
    }

    void initScroll() {
        discussion.getMessagesManager().attachListener(this.activity, new DocumentsManager.OnListChangedListener<Message>() {
            @Override
            public void onListSizeChanged(ArrayList<Message> list, int size) {
                view.scrollToEnd(size);
            }
        });
    }

    @Override
    public void initAdapter() {
        adapter = new ChatAdapter(this);
        messageSortedList = new SortedList<>(Message.class, new MessageSortedListAdapterCallback(adapter));
        adapter.setMessageSortedList(messageSortedList);
        discussion.getMessagesManager().attachSortedList(activity, messageSortedList);
        view.setUpAdapter(adapter);
        view.scrollToEnd(messageSortedList.size());
    }

    @Override
    public void sendMessage(String text) {
        discussion.getMessagesManager().create(new Message(manager.getCurrentUser().getRef(), text));
    }

    @Override
    public UserPublic getUser(String userId) {
        User user = discussion.getMembersManager().get(userId);
        if (user != null) return new UserPublic(user);
        return userRepository.get(userId);
    }

    @Override
    public boolean isMe(String userId) {
        return manager.getCurrentUser().getId().equals(userId);
    }
}
