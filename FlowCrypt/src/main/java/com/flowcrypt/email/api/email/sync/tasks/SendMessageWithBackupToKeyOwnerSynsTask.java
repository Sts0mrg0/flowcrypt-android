/*
 * © 2016-2018 FlowCrypt Limited. Limitations apply. Contact human@flowcrypt.com
 * Contributors: DenBond7
 */

package com.flowcrypt.email.api.email.sync.tasks;

import android.os.Messenger;

import com.flowcrypt.email.api.email.EmailUtil;
import com.flowcrypt.email.api.email.sync.SyncListener;
import com.flowcrypt.email.database.dao.source.AccountDao;
import com.flowcrypt.email.js.Js;
import com.flowcrypt.email.security.SecurityStorageConnector;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;

/**
 * This task send a message with backup to the key owner.
 *
 * @author DenBond7
 * Date: 05.07.2017
 * Time: 14:08
 * E-mail: DenBond7@gmail.com
 */

public class SendMessageWithBackupToKeyOwnerSynsTask extends BaseSyncTask {
  /**
   * The base constructor.
   *
   * @param ownerKey    The name of the reply to {@link Messenger}.
   * @param requestCode The unique request code for the reply to {@link Messenger}.
   */
  public SendMessageWithBackupToKeyOwnerSynsTask(String ownerKey, int requestCode) {
    super(ownerKey, requestCode);
  }

  @Override
  public boolean isUseSMTP() {
    return true;
  }

  @Override
  public void runSMTPAction(AccountDao accountDao, Session session, Store store, SyncListener syncListener) throws
      Exception {
    super.runSMTPAction(accountDao, session, store, syncListener);

    if (syncListener != null && accountDao != null) {
      Transport transport = prepareTransportForSmtp(syncListener.getContext(), session, accountDao);

      Message message = EmailUtil.generateMessageWithAllPrivateKeysBackups(syncListener.getContext(),
          accountDao, session, new Js(syncListener.getContext(),
              new SecurityStorageConnector(syncListener.getContext())));
      transport.sendMessage(message, message.getAllRecipients());

      syncListener.onMessageWithBackupToKeyOwnerSent(accountDao, ownerKey, requestCode, true);
    }
  }
}
