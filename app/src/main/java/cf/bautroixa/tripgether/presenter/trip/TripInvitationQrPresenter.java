package cf.bautroixa.tripgether.presenter.trip;

import androidx.annotation.Nullable;

public interface TripInvitationQrPresenter {
    void requestNewQR();

    interface View {
        void updateQR(String tripId, @Nullable String joinCode);

        void onLoading();

        void onFailed(String reason);
    }
}
