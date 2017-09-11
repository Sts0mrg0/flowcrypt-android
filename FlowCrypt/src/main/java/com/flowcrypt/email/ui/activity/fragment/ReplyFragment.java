/*
 * Business Source License 1.0 © 2017 FlowCrypt Limited (tom@cryptup.org).
 * Use limitations apply. See https://github.com/FlowCrypt/flowcrypt-android/blob/master/LICENSE
 * Contributors: DenBond7
 */

package com.flowcrypt.email.ui.activity.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.flowcrypt.email.R;
import com.flowcrypt.email.api.email.FoldersManager;
import com.flowcrypt.email.api.email.model.IncomingMessageInfo;
import com.flowcrypt.email.api.email.model.OutgoingMessageInfo;
import com.flowcrypt.email.database.dao.source.ContactsDaoSource;
import com.flowcrypt.email.js.PgpContact;
import com.flowcrypt.email.model.MessageEncryptionType;
import com.flowcrypt.email.ui.activity.CreateMessageActivity;
import com.flowcrypt.email.ui.activity.ReplyActivity;
import com.flowcrypt.email.ui.activity.fragment.base.CreateMessageFragment;

import java.util.List;

/**
 * This fragment describe a logic of sent an encrypted or standard message as a reply.
 *
 * @author DenBond7
 *         Date: 10.05.2017
 *         Time: 09:11
 *         E-mail: DenBond7@gmail.com
 */
public class ReplyFragment extends CreateMessageFragment {

    /**
     * This constant will be used when we create a reply message subject.
     */
    private static final String SUBJECT_PREFIX_RE = "Re: ";

    private EditText editTextReplyEmailMessage;
    private View layoutContent;
    private View progressBarCheckContactsDetails;
    private TextInputLayout textInputLayoutReplyEmailMessage;

    private FoldersManager.FolderType folderType;
    private IncomingMessageInfo incomingMessageInfo;

    public ReplyFragment() {
    }

    @Override
    public void onMessageEncryptionTypeChange(MessageEncryptionType messageEncryptionType) {
        String emailMassageHint = null;
        switch (messageEncryptionType) {
            case ENCRYPTED:
                emailMassageHint = getString(R.string.prompt_compose_security_email);
                break;

            case STANDARD:
                emailMassageHint = getString(R.string.prompt_compose_standard_email);
                break;
        }
        textInputLayoutReplyEmailMessage.setHint(emailMassageHint);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActivity().getIntent() != null) {
            this.incomingMessageInfo = getActivity().getIntent().getParcelableExtra
                    (ReplyActivity.EXTRA_KEY_INCOMING_MESSAGE_INFO);
            if (incomingMessageInfo != null && incomingMessageInfo.getFolder() != null) {
                this.folderType = FoldersManager.getFolderTypeForImapFodler(incomingMessageInfo
                        .getFolder().getAttributes());
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_security_reply, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
/*
        if (incomingMessageInfo != null) {
            updateViews();
        }*/
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        /*if (GeneralUtil.isInternetConnectionAvailable(getContext())
                && onChangeMessageEncryptedTypeListener.getMessageEncryptionType() ==
                MessageEncryptionType.ENCRYPTED) {
            getLoaderManager().restartLoader(
                    R.id.loader_id_update_info_about_pgp_contacts, null, this);
        }*/
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_secure_reply, menu);
    }

    @Override
    public OutgoingMessageInfo getOutgoingMessageInfo() {
        OutgoingMessageInfo outgoingMessageInfo = new OutgoingMessageInfo();
        outgoingMessageInfo.setMessage(editTextReplyEmailMessage.getText().toString());
        outgoingMessageInfo.setSubject(SUBJECT_PREFIX_RE + incomingMessageInfo.getSubject());
        outgoingMessageInfo.setRawReplyMessage(
                incomingMessageInfo.getOriginalRawMessageWithoutAttachments());
        List<PgpContact> pgpContacts = new ContactsDaoSource().getPgpContactsListFromDatabase
                (getContext(), FoldersManager.FolderType.SENT == folderType ?
                        incomingMessageInfo.getTo() : incomingMessageInfo.getFrom());

        outgoingMessageInfo.setToPgpContacts(pgpContacts.toArray(new PgpContact[0]));

        if (getActivity() instanceof CreateMessageActivity) {
            CreateMessageActivity createMessageActivity = (CreateMessageActivity)
                    getActivity();
            outgoingMessageInfo.setFromPgpContact(new PgpContact(createMessageActivity
                    .getSenderEmail(), null));
        }

        return outgoingMessageInfo;
    }

   /* @Override
    public View getUpdateInfoAboutContactsProgressBar() {
        return progressBarCheckContactsDetails;
    }

    @Override
    public List<String> getContactsEmails() {
        if (FoldersManager.FolderType.SENT == folderType) {
            return incomingMessageInfo.getTo();
        } else {
            return incomingMessageInfo.getFrom();
        }
    }*/

    @Override
    public View getContentView() {
        return layoutContent;
    }

    /*@Override
    public boolean isAllInformationCorrect() {
        if (TextUtils.isEmpty(editTextReplyEmailMessage.getText().toString())) {
            UIUtil.showInfoSnackbar(editTextReplyEmailMessage,
                    getString(R.string.sending_message_must_not_be_empty));
        } else if (onChangeMessageEncryptedTypeListener.getMessageEncryptionType() ==
                MessageEncryptionType.ENCRYPTED) {
            if (pgpContacts.isEmpty()) {
                showSnackbar(getView(),
                        getString(R.string.please_update_information_about_contacts),
                        getString(R.string.update), Snackbar.LENGTH_LONG,
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (GeneralUtil.isInternetConnectionAvailable(getContext())) {
                                    getLoaderManager().restartLoader(
                                            R.id.loader_id_update_info_about_pgp_contacts, null,
                                            ReplyFragment.this);
                                } else {
                                    showInfoSnackbar(getView(), getString(R.string
                                            .internet_connection_is_not_available));
                                }
                            }
                        });
            } else if (isAllRecipientsHavePGP(false)) {
                return true;
            }
        } else {
            return true;
        }

        return false;
    }*/

    /**
     * Update views on the screen. This method can be called when we need to update the current
     * screen.
     *//*
    private void updateViews() {
        if (incomingMessageInfo != null) {
            if (FoldersManager.FolderType.SENT == folderType) {
                editTextRecipients.setText(prepareRecipients(incomingMessageInfo.getTo()));
            } else {
                editTextRecipients.setText(prepareRecipients(incomingMessageInfo.getFrom()));
            }
            editTextRecipients.chipifyAllUnterminatedTokens();
        }
    }*/

    private void initViews(View view) {
        this.editTextReplyEmailMessage =
                (EditText) view.findViewById(R.id.editTextReplyEmailMessage);
        this.textInputLayoutReplyEmailMessage =
                (TextInputLayout) view.findViewById(R.id.textInputLayoutReplyEmailMessage);
        this.layoutContent = view.findViewById(R.id.layoutForm);
        this.progressBarCheckContactsDetails =
                view.findViewById(R.id.progressBarCheckContactsDetails);
    }

    private String prepareRecipients(List<String> recipients) {
        String result = "";
        for (String s : recipients) {
            result += s + " ";
        }

        return result;
    }
}
