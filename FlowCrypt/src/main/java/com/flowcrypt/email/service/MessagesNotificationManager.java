/*
 * © 2016-2018 FlowCrypt Limited. Limitations apply. Contact human@flowcrypt.com
 * Contributors: DenBond7
 */

package com.flowcrypt.email.service;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.service.notification.StatusBarNotification;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;

import com.flowcrypt.email.BuildConfig;
import com.flowcrypt.email.R;
import com.flowcrypt.email.api.email.EmailUtil;
import com.flowcrypt.email.api.email.Folder;
import com.flowcrypt.email.api.email.FoldersManager;
import com.flowcrypt.email.api.email.model.GeneralMessageDetails;
import com.flowcrypt.email.database.dao.source.AccountDao;
import com.flowcrypt.email.database.dao.source.imap.MessageDaoSource;
import com.flowcrypt.email.ui.NotificationChannelManager;
import com.flowcrypt.email.ui.activity.EmailManagerActivity;
import com.flowcrypt.email.ui.activity.SplashActivity;
import com.flowcrypt.email.ui.notifications.CustomNotificationManager;

import java.util.List;

/**
 * This manager is responsible for displaying messages notifications.
 *
 * @author Denis Bondarenko
 *         Date: 23.06.2018
 *         Time: 12:10
 *         E-mail: DenBond7@gmail.com
 */
public class MessagesNotificationManager extends CustomNotificationManager {
    public static final String GROUP_NAME_FLOWCRYPT_MESSAGES = BuildConfig.APPLICATION_ID + ".MESSAGES";
    private NotificationManagerCompat notificationManagerCompat;

    public MessagesNotificationManager(Context context) {
        this.notificationManagerCompat = NotificationManagerCompat.from(context);
    }

    /**
     * Show a {@link Notification} of an incoming message.
     *
     * @param context                   Interface to global information about an application environment.
     * @param accountDao                An {@link AccountDao} object which contains information about an email account.
     * @param generalMessageDetailsList A list of models which consists information about some messages.
     * @param uidListOfUnseenMessages       A list of UID of unseen messages.
     */
    public void notify(Context context, AccountDao accountDao, List<GeneralMessageDetails> generalMessageDetailsList,
                       List<Integer> uidListOfUnseenMessages) {

        if (accountDao == null || generalMessageDetailsList == null || generalMessageDetailsList.isEmpty()) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            notifyWithGroupSupport(context, accountDao, generalMessageDetailsList);
        } else {
            notifyWithSingleNotification(context, accountDao, generalMessageDetailsList, uidListOfUnseenMessages);
        }
    }

    public void cancel(int messageUID) {
        notificationManagerCompat.cancel(messageUID);
    }

    public void cancelAll(Context context, AccountDao accountDao) {
        notificationManagerCompat.cancel(NOTIFICATIONS_GROUP_MESSAGES);

        FoldersManager foldersManager = FoldersManager.fromDatabase(context, accountDao.getEmail());
        Folder folder = foldersManager.findInboxFolder();

        if (folder != null) {
            new MessageDaoSource().setOldStatusForLocalMessages(context,
                    accountDao.getEmail(), folder.getFolderAlias());
        }
    }

    private void notifyWithSingleNotification(Context context, AccountDao accountDao,
                                              List<GeneralMessageDetails> generalMessageDetailsList,
                                              List<Integer> uidOfUnseenMessages) {
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, NotificationChannelManager.CHANNEL_ID_MESSAGES)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setCategory(NotificationCompat.CATEGORY_EMAIL)
                        .setSmallIcon(R.drawable.ic_email_encrypted)
                        .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))
                        .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                        .setAutoCancel(true)
                        .setGroup(GROUP_NAME_FLOWCRYPT_MESSAGES)
                        .setSubText(accountDao.getEmail());

        if (uidOfUnseenMessages.size() > 1) {
            builder.setNumber(uidOfUnseenMessages.size());
        }

        if (generalMessageDetailsList.size() > 1) {
            NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
            for (GeneralMessageDetails generalMessageDetails : generalMessageDetailsList) {
                inboxStyle.addLine(formatInboxStyleLine(context,
                        EmailUtil.getFirstAddressString(generalMessageDetails.getFrom()),
                        generalMessageDetails.getSubject()));
            }

            builder.setStyle(inboxStyle)
                    .setSmallIcon(R.drawable.ic_email_multiply_encrypted)
                    .setContentIntent(getInboxPendingIntent(context, accountDao))
                    //.setDeleteIntent()
                    .setContentTitle(context.getString(R.string.incoming_message,
                            generalMessageDetailsList.size()));
        } else {
            GeneralMessageDetails generalMessageDetails = generalMessageDetailsList.get(0);
            builder.setContentText(formatText(generalMessageDetails.getSubject(),
                    ContextCompat.getColor(context, android.R.color.black)))
                    .setContentIntent(getInboxPendingIntent(context, accountDao))
                    .setContentTitle(EmailUtil.getFirstAddressString(generalMessageDetails.getFrom()))
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(
                            formatText(generalMessageDetails.getSubject(),
                                    ContextCompat.getColor(context, android.R.color.black))))
                    //.setDeleteIntent()
                    .setSmallIcon(R.drawable.ic_email_encrypted)
                    .addAction(generateReplyAction(context));
        }

        notificationManagerCompat.notify(NOTIFICATIONS_GROUP_MESSAGES, builder.build());
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void notifyWithGroupSupport(Context context, AccountDao accountDao,
                                        List<GeneralMessageDetails> generalMessageDetailsList) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager != null) {
            prepareAndShowMessageGroup(context, accountDao, notificationManager);
        }

        for (GeneralMessageDetails generalMessageDetails : generalMessageDetailsList) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NotificationChannelManager
                    .CHANNEL_ID_MESSAGES)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setCategory(NotificationCompat.CATEGORY_EMAIL)
                    .setSmallIcon(R.drawable.ic_email_encrypted)
                    .setLargeIcon(generateLargeIcon(context, generalMessageDetails))
                    .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                    //.setDeleteIntent()
                    .setAutoCancel(true)
                    .setContentTitle(EmailUtil.getFirstAddressString(generalMessageDetails.getFrom()))
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(generalMessageDetails.getSubject()))
                    .addAction(generateReplyAction(context))
                    .setGroup(GROUP_NAME_FLOWCRYPT_MESSAGES)
                    .setContentText(generalMessageDetails.getSubject())
                    .setContentIntent(getInboxPendingIntent(context, accountDao))
                    .setSubText(accountDao.getEmail());

            notificationManagerCompat.notify(generalMessageDetails.getUid(), builder.build());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void prepareAndShowMessageGroup(Context context, AccountDao accountDao,
                                            NotificationManager notificationManager) {
        int groupResourceId = R.drawable.ic_email_encrypted;

        for (StatusBarNotification statusBarNotification : notificationManager.getActiveNotifications()) {
            if (GROUP_NAME_FLOWCRYPT_MESSAGES.equals(statusBarNotification.getNotification().getGroup())) {
                groupResourceId = R.drawable.ic_email_multiply_encrypted;
                break;
            }
        }

        NotificationCompat.Builder groupBuilder =
                new NotificationCompat.Builder(context, NotificationChannelManager.CHANNEL_ID_MESSAGES)
                        .setSmallIcon(groupResourceId)
                        .setContentInfo(accountDao.getEmail())
                        .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                        .setSubText(accountDao.getEmail())
                        .setGroup(GROUP_NAME_FLOWCRYPT_MESSAGES)
                        .setAutoCancel(true)
                        .setContentIntent(getInboxPendingIntent(context, accountDao))
                        .setGroupSummary(true);
        notificationManager.notify(NOTIFICATIONS_GROUP_MESSAGES, groupBuilder.build());
    }

    private PendingIntent getInboxPendingIntent(Context context, AccountDao accountDao) {
        Intent inboxIntent = new Intent(context, EmailManagerActivity.class);
        inboxIntent.putExtra(EmailManagerActivity.EXTRA_KEY_ACCOUNT_DAO, accountDao);
        inboxIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return PendingIntent.getActivity(context, 0, inboxIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private Bitmap generateLargeIcon(Context context, GeneralMessageDetails generalMessageDetails) {
        return BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
    }

    private NotificationCompat.Action generateReplyAction(Context context) {
        Intent intent = new Intent(context, SplashActivity.class);

        PendingIntent cancelDownloadPendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        return new NotificationCompat.Action.Builder(R.mipmap.ic_reply_green, context.getString(R.string
                .reply), cancelDownloadPendingIntent).build();
    }
}
