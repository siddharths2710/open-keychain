package org.sufficientlysecure.keychain.ui.base;


import android.os.Bundle;
import android.os.Parcelable;

import org.sufficientlysecure.keychain.operations.results.OperationResult;


public abstract class CachingCryptoOperationFragment <T extends Parcelable, S extends OperationResult>
        extends QueueingCryptoOperationFragment<T, S> {

    public static final String ARG_CACHED_ACTIONS = "cached_actions";

    private T mCachedActionsParcel;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(ARG_CACHED_ACTIONS, mCachedActionsParcel);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mCachedActionsParcel = savedInstanceState.getParcelable(ARG_CACHED_ACTIONS);
        }
    }

    @Override
    public void onQueuedOperationSuccess(S result) {
        mCachedActionsParcel = null;
    }

    @Override
    public void onQueuedOperationError(S result) {
        mCachedActionsParcel = null;
    }

    @Override
    public abstract T createOperationInput();

    protected T getCachedActionsParcel() {
        return mCachedActionsParcel;
    }

    protected void cacheActionsParcel(T cachedActionsParcel) {
        mCachedActionsParcel = cachedActionsParcel;
    }

    public void onCryptoOperationCancelled() {
        mCachedActionsParcel = null;
    }

}
