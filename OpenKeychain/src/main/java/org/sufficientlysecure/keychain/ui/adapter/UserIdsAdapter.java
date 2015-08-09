/*
 * Copyright (C) 2014-2015 Dominik Schürmann <dominik@dominikschuermann.de>
 * Copyright (C) 2015 Vincent Breitmoser <v.breitmoser@mugenguild.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.sufficientlysecure.keychain.ui.adapter;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.v4.content.CursorLoader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.sufficientlysecure.keychain.R;
import org.sufficientlysecure.keychain.pgp.KeyRing;
import org.sufficientlysecure.keychain.provider.KeychainContract.Certs;
import org.sufficientlysecure.keychain.provider.KeychainContract.UserPackets;
import org.sufficientlysecure.keychain.service.SaveKeyringParcel;
import org.sufficientlysecure.keychain.ui.util.KeyFormattingUtils;
import org.sufficientlysecure.keychain.ui.util.KeyFormattingUtils.State;

import snippet.CheatSheet;

public class UserIdsAdapter extends UserAttributesAdapter {
    protected LayoutInflater mInflater;
    private SaveKeyringParcel mSaveKeyringParcel;
    private boolean mShowStatusImages;

    public UserIdsAdapter(Context context, Cursor c, int flags,
                          boolean showStatusImages, SaveKeyringParcel saveKeyringParcel) {
        super(context, c, flags);
        mInflater = LayoutInflater.from(context);

        mSaveKeyringParcel = saveKeyringParcel;
        mShowStatusImages = showStatusImages;
    }

    public UserIdsAdapter(Context context, Cursor c, int flags, SaveKeyringParcel saveKeyringParcel) {
        this(context, c, flags, true, saveKeyringParcel);
    }

    public UserIdsAdapter(Context context, Cursor c, int flags) {
        this(context, c, flags, true, null);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView vName = (TextView) view.findViewById(R.id.user_id_item_name);
        TextView vAddress = (TextView) view.findViewById(R.id.user_id_item_address);
        TextView vComment = (TextView) view.findViewById(R.id.user_id_item_comment);
            CheatSheet.setup(vName,getContentDescription(vName));
            CheatSheet.setup(vAddress,getContentDescription(vAddress));
            CheatSheet.setup(vComment,getContentDescription(vComment));
        ImageView vVerified = (ImageView) view.findViewById(R.id.user_id_item_certified);
        View vVerifiedLayout = view.findViewById(R.id.user_id_item_certified_layout);
        ImageView vEditImage = (ImageView) view.findViewById(R.id.user_id_item_edit_image);
        ImageView vDeleteButton = (ImageView) view.findViewById(R.id.user_id_item_delete_button);
        vDeleteButton.setVisibility(View.GONE); // not used

        String userId = cursor.getString(INDEX_USER_ID);
        KeyRing.UserId splitUserId = KeyRing.splitUserId(userId);
        if (splitUserId.name != null) {
            vName.setText(splitUserId.name);
        } else {
            vName.setText(R.string.user_id_no_name);
        }
        if (splitUserId.email != null) {
            vAddress.setText(splitUserId.email);
            vAddress.setVisibility(View.VISIBLE);
        } else {
            vAddress.setVisibility(View.GONE);
        }
        if (splitUserId.comment != null) {
            vComment.setText(splitUserId.comment);
            vComment.setVisibility(View.VISIBLE);
        } else {
            vComment.setVisibility(View.GONE);
        }

        boolean isPrimary = cursor.getInt(INDEX_IS_PRIMARY) != 0;
        boolean isRevoked = cursor.getInt(INDEX_IS_REVOKED) > 0;

        // for edit key
        if (mSaveKeyringParcel != null) {
            boolean changeAnyPrimaryUserId = (mSaveKeyringParcel.mChangePrimaryUserId != null);
            boolean changeThisPrimaryUserId = (mSaveKeyringParcel.mChangePrimaryUserId != null
                    && mSaveKeyringParcel.mChangePrimaryUserId.equals(userId));
            boolean revokeThisUserId = (mSaveKeyringParcel.mRevokeUserIds.contains(userId));

            // only if primary user id will be changed
            // (this is not triggered if the user id is currently the primary one)
            if (changeAnyPrimaryUserId) {
                // change _all_ primary user ids and set new one to true
                isPrimary = changeThisPrimaryUserId;
            }

            if (revokeThisUserId) {
                if (!isRevoked) {
                    isRevoked = true;
                }
            }

            vEditImage.setVisibility(View.VISIBLE);
            vVerifiedLayout.setVisibility(View.GONE);
        } else {
            vEditImage.setVisibility(View.GONE);

            if (mShowStatusImages) {
                vVerifiedLayout.setVisibility(View.VISIBLE);
            } else {
                vVerifiedLayout.setVisibility(View.GONE);
            }
        }

        if (isRevoked) {
            // set revocation icon (can this even be primary?)
            KeyFormattingUtils.setStatusImage(mContext, vVerified, null, State.REVOKED, R.color.bg_gray);

            // disable revoked user ids
            vName.setEnabled(false);
            vAddress.setEnabled(false);
            vComment.setEnabled(false);
        } else {
            vName.setEnabled(true);
            vAddress.setEnabled(true);
            vComment.setEnabled(true);

            if (isPrimary) {
                vName.setTypeface(null, Typeface.BOLD);
                vAddress.setTypeface(null, Typeface.BOLD);
            } else {
                vName.setTypeface(null, Typeface.NORMAL);
                vAddress.setTypeface(null, Typeface.NORMAL);
            }

            int isVerified = cursor.getInt(INDEX_VERIFIED);
            switch (isVerified) {
                case Certs.VERIFIED_SECRET:
                    KeyFormattingUtils.setStatusImage(mContext, vVerified, null, State.VERIFIED, KeyFormattingUtils.DEFAULT_COLOR);
                    break;
                case Certs.VERIFIED_SELF:
                    KeyFormattingUtils.setStatusImage(mContext, vVerified, null, State.UNVERIFIED, KeyFormattingUtils.DEFAULT_COLOR);
                    break;
                default:
                    KeyFormattingUtils.setStatusImage(mContext, vVerified, null, State.INVALID, KeyFormattingUtils.DEFAULT_COLOR);
                    break;
            }
        }
    }

    public boolean getIsRevokedPending(int position) {
        mCursor.moveToPosition(position);
        String userId = mCursor.getString(INDEX_USER_ID);

        boolean isRevokedPending = false;
        if (mSaveKeyringParcel != null) {
            if (mSaveKeyringParcel.mRevokeUserIds.contains(userId)) {
                isRevokedPending = true;
            }

        }

        return isRevokedPending;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return mInflater.inflate(R.layout.view_key_adv_user_id_item, null);
    }

    // don't show revoked user ids, irrelevant for average users
    public static final String USER_IDS_WHERE = UserPackets.IS_REVOKED + " = 0";

    public static CursorLoader createLoader(Activity activity, Uri dataUri) {
        Uri baseUri = UserPackets.buildUserIdsUri(dataUri);
        return new CursorLoader(activity, baseUri,
                UserIdsAdapter.USER_IDS_PROJECTION, USER_IDS_WHERE, null, null);
    }

}
