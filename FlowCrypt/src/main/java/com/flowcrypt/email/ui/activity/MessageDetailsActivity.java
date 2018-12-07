/*
 * © 2016-2018 FlowCrypt Limited. Limitations apply. Contact human@flowcrypt.com
 * Contributors: DenBond7
 */

package com.flowcrypt.email.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.flowcrypt.email.R;
import com.flowcrypt.email.api.email.FoldersManager;
import com.flowcrypt.email.api.email.JavaEmailConstants;
import com.flowcrypt.email.api.email.LocalFolder;
import com.flowcrypt.email.api.email.model.AttachmentInfo;
import com.flowcrypt.email.api.email.model.GeneralMessageDetails;
import com.flowcrypt.email.api.email.model.IncomingMessageInfo;
import com.flowcrypt.email.api.email.sync.SyncErrorTypes;
import com.flowcrypt.email.database.MessageState;
import com.flowcrypt.email.database.dao.source.imap.AttachmentDaoSource;
import com.flowcrypt.email.database.dao.source.imap.MessageDaoSource;
import com.flowcrypt.email.service.EmailSyncService;
import com.flowcrypt.email.ui.activity.base.BaseBackStackSyncActivity;
import com.flowcrypt.email.ui.activity.fragment.MessageDetailsFragment;
import com.flowcrypt.email.util.GeneralUtil;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

/**
 * This activity describe details of some message.
 *
 * @author DenBond7
 * Date: 03.05.2017
 * Time: 16:29
 * E-mail: DenBond7@gmail.com
 */
public class MessageDetailsActivity extends BaseBackStackSyncActivity implements
    LoaderManager.LoaderCallbacks<Cursor>, MessageDetailsFragment.OnActionListener {
  public static final int RESULT_CODE_UPDATE_LIST = 100;

  public static final String EXTRA_KEY_FOLDER = GeneralUtil.generateUniqueExtraKey("EXTRA_KEY_FOLDER",
      MessageDetailsActivity.class);
  public static final String EXTRA_KEY_GENERAL_MESSAGE_DETAILS = GeneralUtil.generateUniqueExtraKey
      ("EXTRA_KEY_GENERAL_MESSAGE_DETAILS", MessageDetailsActivity.class);

  private GeneralMessageDetails details;
  private LocalFolder localFolder;
  private boolean isReceiveMsgBodyNeeded;
  private boolean isBackEnabled = true;
  private boolean isRequestMsgDetailsStarted;
  private boolean isRetrieveIncomingMsgNeeded = true;

  public static Intent getIntent(Context context, LocalFolder localFolder, GeneralMessageDetails details) {
    Intent intent = new Intent(context, MessageDetailsActivity.class);
    intent.putExtra(EXTRA_KEY_FOLDER, localFolder);
    intent.putExtra(EXTRA_KEY_GENERAL_MESSAGE_DETAILS, details);
    return intent;
  }

  @Override
  public View getRootView() {
    return null;
  }

  @Override
  public int getContentViewResourceId() {
    return R.layout.activity_message_details;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (getIntent() != null) {
      this.localFolder = getIntent().getParcelableExtra(EXTRA_KEY_FOLDER);
      this.details = getIntent().getParcelableExtra(EXTRA_KEY_GENERAL_MESSAGE_DETAILS);
    }

    updateViews();

    if (TextUtils.isEmpty(details.getRawMsgWithoutAttachments())) {
      LoaderManager.getInstance(this).initLoader(R.id.loader_id_load_message_info_from_database, null, this);
    }

    LoaderManager.getInstance(this).initLoader(R.id.loader_id_subscribe_to_message_changes, null, this);
  }

  @Override
  public void onBackPressed() {
    if (isBackEnabled) {
      super.onBackPressed();
    } else {
      Toast.makeText(this, R.string.please_wait_while_action_will_be_completed, Toast.LENGTH_SHORT).show();
    }
  }

  @NonNull
  @Override
  public Loader<Cursor> onCreateLoader(int id, Bundle args) {
    switch (id) {
      case R.id.loader_id_load_message_info_from_database:
      case R.id.loader_id_subscribe_to_message_changes:
        Uri uri = new MessageDaoSource().getBaseContentUri();
        String selection = MessageDaoSource.COL_EMAIL + "= ? AND " + MessageDaoSource.COL_FOLDER + " = ? AND "
            + MessageDaoSource.COL_UID + " = ? ";
        String[] selectionArgs = new String[]{details.getEmail(), localFolder.getFolderAlias(),
            String.valueOf(details.getUid())};
        return new CursorLoader(this, uri, null, selection, selectionArgs, null);

      case R.id.loader_id_load_attachments:
        Uri uriAtt = new AttachmentDaoSource().getBaseContentUri();
        String selectionAtt = AttachmentDaoSource.COL_EMAIL + " = ?" + " AND " + AttachmentDaoSource.COL_FOLDER
            + " = ?" + " AND " + AttachmentDaoSource.COL_UID + " = ?";
        String[] selectionArgsAtt = new String[]{details.getEmail(), localFolder.getFolderAlias(),
            String.valueOf(details.getUid())};
        return new CursorLoader(this, uriAtt, null, selectionAtt, selectionArgsAtt, null);

      default:
        return new Loader<>(this);
    }
  }

  @Override
  public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
    MessageDaoSource messageDaoSource = new MessageDaoSource();

    switch (loader.getId()) {
      case R.id.loader_id_load_message_info_from_database:
        if (cursor != null && cursor.moveToFirst()) {
          this.details = messageDaoSource.getMsgInfo(cursor);
          updateMsgDetails(details);

          if (TextUtils.isEmpty(details.getRawMsgWithoutAttachments())) {
            if (isSyncServiceBound && !isRequestMsgDetailsStarted) {
              this.isRequestMsgDetailsStarted = true;
              loadMsgDetails(R.id.syns_request_code_load_message_details, localFolder, details.getUid());
            } else {
              isReceiveMsgBodyNeeded = true;
            }
          } else if (isRetrieveIncomingMsgNeeded) {
            isRetrieveIncomingMsgNeeded = false;
            isReceiveMsgBodyNeeded = false;
            messageDaoSource.setSeenStatus(this, details.getEmail(),
                localFolder.getFolderAlias(), details.getUid());
            setResult(MessageDetailsActivity.RESULT_CODE_UPDATE_LIST, null);
            decryptMsg(R.id.js_decrypt_message, details.getRawMsgWithoutAttachments());
          }
        }
        break;

      case R.id.loader_id_subscribe_to_message_changes:
        if (cursor != null && cursor.moveToFirst()) {
          details = messageDaoSource.getMsgInfo(cursor);
          updateViews();
        }
        break;

      case R.id.loader_id_load_attachments:
        if (cursor != null) {
          ArrayList<AttachmentInfo> attInfolist = new ArrayList<>();
          while (cursor.moveToNext()) {
            attInfolist.add(AttachmentDaoSource.getAttachmentInfo(cursor));
          }

          updateAttachments(attInfolist);
        }
        break;
    }
  }

  @Override
  public void onLoaderReset(@NonNull Loader<Cursor> loader) {
    switch (loader.getId()) {
      case R.id.loader_id_load_message_info_from_database:
        break;

      case R.id.loader_id_subscribe_to_message_changes:
        details = null;
        updateViews();
        break;

      case R.id.loader_id_load_attachments:
        updateAttachments(new ArrayList<AttachmentInfo>());
        break;
    }
  }


  @Override
  public void onSyncServiceConnected() {
    super.onSyncServiceConnected();
    if (isReceiveMsgBodyNeeded) {
      loadMsgDetails(R.id.syns_request_code_load_message_details, localFolder, details.getUid());
    }
  }

  @Override
  public void onReplyReceived(int requestCode, int resultCode, Object obj) {
    switch (requestCode) {
      case R.id.syns_request_code_load_message_details:
        isRequestMsgDetailsStarted = false;
        switch (resultCode) {
          case EmailSyncService.REPLY_RESULT_CODE_ACTION_OK:
            String folderAlias = localFolder.getFolderAlias();
            new MessageDaoSource().setSeenStatus(this, details.getEmail(), folderAlias, details.getUid());
            setResult(MessageDetailsActivity.RESULT_CODE_UPDATE_LIST, null);
            LoaderManager.getInstance(this).restartLoader(R.id.loader_id_load_message_info_from_database, null, this);
            break;

          case EmailSyncService.REPLY_RESULT_CODE_ACTION_ERROR_MESSAGE_NOT_FOUND:
            messageNotAvailableInFolder();
            break;
        }
        break;

      case R.id.syns_request_archive_message:
      case R.id.syns_request_delete_message:
      case R.id.syns_request_move_message_to_inbox:
        isBackEnabled = true;
        switch (resultCode) {
          case EmailSyncService.REPLY_RESULT_CODE_ACTION_OK:
            int toastMsgResId = 0;

            switch (requestCode) {
              case R.id.syns_request_archive_message:
                toastMsgResId = R.string.message_was_archived;
                break;

              case R.id.syns_request_delete_message:
                toastMsgResId = R.string.message_was_deleted;
                break;

              case R.id.syns_request_move_message_to_inbox:
                toastMsgResId = R.string.message_was_moved_to_inbox;
                break;
            }

            Toast.makeText(this, toastMsgResId, Toast.LENGTH_SHORT).show();
            String folderAlias = localFolder.getFolderAlias();
            new MessageDaoSource().deleteMsg(this, details.getEmail(), folderAlias, details.getUid());
            new AttachmentDaoSource().deleteAttachments(this, details.getEmail(), folderAlias, details.getUid());
            setResult(MessageDetailsActivity.RESULT_CODE_UPDATE_LIST, null);
            finish();
            break;

          case EmailSyncService.REPLY_RESULT_CODE_ACTION_ERROR_MESSAGE_NOT_EXISTS:
            messageNotAvailableInFolder();
            break;
        }
        break;

      case R.id.js_decrypt_message:
        if (obj instanceof IncomingMessageInfo) {
          IncomingMessageInfo msgInfo = (IncomingMessageInfo) obj;
          MessageDetailsFragment fragment = (MessageDetailsFragment) getSupportFragmentManager()
              .findFragmentById(R.id.messageDetailsFragment);

          if (fragment != null) {
            fragment.showIncomingMsgInfo(msgInfo);
            LoaderManager.getInstance(this).initLoader(R.id.loader_id_load_attachments, null, this);
          }
        }
        break;
    }
  }

  @Override
  public void onJsServiceConnected() {
    super.onJsServiceConnected();
    if (!TextUtils.isEmpty(details.getRawMsgWithoutAttachments())) {
      decryptMsg(R.id.js_decrypt_message, details.getRawMsgWithoutAttachments());
    }
  }

  @Override
  public void onErrorHappened(int requestCode, int errorType, Exception e) {
    switch (requestCode) {
      case R.id.syns_request_code_load_message_details:
        isRequestMsgDetailsStarted = false;
        onErrorOccurred(requestCode, errorType, e);
        break;

      case R.id.syns_request_archive_message:
      case R.id.syns_request_delete_message:
      case R.id.syns_request_move_message_to_inbox:
        isBackEnabled = true;
        onErrorOccurred(requestCode, errorType, e);
        break;

      default:
        onErrorOccurred(requestCode, errorType, e);
        break;
    }
  }

  @Override
  public void onArchiveMsgClicked() {
    isBackEnabled = false;
    FoldersManager foldersManager = FoldersManager.fromDatabase(this, details.getEmail());
    moveMsg(R.id.syns_request_archive_message, localFolder, foldersManager.getFolderArchive(), details.getUid());
  }

  @Override
  public void onDeleteMsgClicked() {
    isBackEnabled = false;
    if (JavaEmailConstants.FOLDER_OUTBOX.equalsIgnoreCase(details.getLabel())) {
      MessageDaoSource msgDaoSource = new MessageDaoSource();
      GeneralMessageDetails details = msgDaoSource.getMsg(this, this.details.getEmail(),
          this.details.getLabel(), this.details.getUid());

      if (details == null || details.getMsgState() == MessageState.SENDING) {
        Toast.makeText(this, details == null ? R.string.can_not_delete_sent_message
            : R.string.can_not_delete_sending_message, Toast.LENGTH_LONG).show();
      } else {
        int deletedRows = new MessageDaoSource().deleteOutgoingMsg(this, details);
        if (deletedRows > 0) {
          Toast.makeText(this, R.string.message_was_deleted, Toast.LENGTH_SHORT).show();
        } else {
          Toast.makeText(this, R.string.can_not_delete_sent_message, Toast.LENGTH_LONG).show();
        }
      }

      setResult(MessageDetailsActivity.RESULT_CODE_UPDATE_LIST, null);
      finish();
    } else {
      FoldersManager foldersManager = FoldersManager.fromDatabase(this, details.getEmail());
      moveMsg(R.id.syns_request_delete_message, localFolder, foldersManager.getFolderTrash(), details.getUid());
    }
  }

  @Override
  public void onMoveMsgToInboxClicked() {
    isBackEnabled = false;
    FoldersManager foldersManager = FoldersManager.fromDatabase(this, details.getEmail());
    LocalFolder desFolder = foldersManager.getFolderInbox();
    moveMsg(R.id.syns_request_move_message_to_inbox, localFolder, desFolder, details.getUid());
  }

  private void messageNotAvailableInFolder() {
    String folderAlias = localFolder.getFolderAlias();
    new MessageDaoSource().deleteMsg(this, details.getEmail(), folderAlias, details.getUid());
    new AttachmentDaoSource().deleteAttachments(this, details.getEmail(), folderAlias, details.getUid());
    setResult(MessageDetailsActivity.RESULT_CODE_UPDATE_LIST, null);
    Toast.makeText(this, R.string.email_does_not_available_in_this_folder, Toast.LENGTH_LONG).show();
    finish();
  }

  /**
   * Handle an error from the sync service.
   *
   * @param requestCode The unique request code for the reply to {@link android.os.Messenger}.
   * @param errorType   The {@link SyncErrorTypes}
   * @param e           The exception which happened.
   */
  private void onErrorOccurred(int requestCode, int errorType, Exception e) {
    MessageDetailsFragment fragment = (MessageDetailsFragment)
        getSupportFragmentManager().findFragmentById(R.id.messageDetailsFragment);

    if (fragment != null) {
      fragment.onErrorOccurred(requestCode, errorType, e);
    }
  }

  private void updateMsgDetails(GeneralMessageDetails generalMsgDetails) {
    MessageDetailsFragment fragment = (MessageDetailsFragment) getSupportFragmentManager()
        .findFragmentById(R.id.messageDetailsFragment);

    if (fragment != null) {
      fragment.updateMsgDetails(generalMsgDetails);
    }
  }

  private void updateAttachments(ArrayList<AttachmentInfo> atts) {
    MessageDetailsFragment fragment = (MessageDetailsFragment) getSupportFragmentManager()
        .findFragmentById(R.id.messageDetailsFragment);

    if (fragment != null) {
      fragment.updateAttInfos(atts);
    }
  }

  private void updateViews() {
    if (getSupportActionBar() != null) {
      String actionBarTitle = null;
      String actionBarSubTitle = null;

      if (details != null) {
        if (JavaEmailConstants.FOLDER_OUTBOX.equalsIgnoreCase(details.getLabel())) {
          actionBarTitle = getString(R.string.outgoing);

          if (details.getMsgState() != null) {
            switch (details.getMsgState()) {
              case NEW:
              case NEW_FORWARDED:
                actionBarSubTitle = getString(R.string.preparing);
                break;

              case QUEUED:
                actionBarSubTitle = getString(R.string.queued);
                break;

              case SENDING:
                actionBarSubTitle = getString(R.string.sending);
                break;

              case SENT:
              case SENT_WITHOUT_LOCAL_COPY:
                actionBarSubTitle = getString(R.string.sent);
                break;

              case ERROR_CACHE_PROBLEM:
              case ERROR_DURING_CREATION:
              case ERROR_ORIGINAL_MESSAGE_MISSING:
              case ERROR_ORIGINAL_ATTACHMENT_NOT_FOUND:
              case ERROR_SENDING_FAILED:
              case ERROR_PRIVATE_KEY_NOT_FOUND:
                actionBarSubTitle = getString(R.string.an_error_has_occurred);
                break;
            }
          }
        }
      }

      getSupportActionBar().setTitle(actionBarTitle);
      getSupportActionBar().setSubtitle(actionBarSubTitle);
    }
  }
}
