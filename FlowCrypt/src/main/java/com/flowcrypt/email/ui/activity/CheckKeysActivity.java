/*
 * Business Source License 1.0 © 2017 FlowCrypt Limited (tom@cryptup.org).
 * Use limitations apply. See https://github.com/FlowCrypt/flowcrypt-android/blob/master/LICENSE
 * Contributors: DenBond7
 */

package com.flowcrypt.email.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.flowcrypt.email.R;
import com.flowcrypt.email.model.PrivateKeyDetails;
import com.flowcrypt.email.model.results.LoaderResult;
import com.flowcrypt.email.security.KeyStoreCryptoManager;
import com.flowcrypt.email.ui.activity.base.BaseActivity;
import com.flowcrypt.email.ui.loader.EncryptAndSavePrivateKeysAsyncTaskLoader;
import com.flowcrypt.email.util.GeneralUtil;
import com.flowcrypt.email.util.UIUtil;

import java.util.ArrayList;

/**
 * This class describes checking the received private keys. Here we validate and save encrypted
 * via {@link KeyStoreCryptoManager} keys to the database. If one of received private keys is
 * valid, we will return {@link Activity#RESULT_OK}.
 *
 * @author Denis Bondarenko
 *         Date: 21.07.2017
 *         Time: 9:59
 *         E-mail: DenBond7@gmail.com
 */

public class CheckKeysActivity extends BaseActivity implements View.OnClickListener,
        LoaderManager.LoaderCallbacks<LoaderResult> {

    public static final int RESULT_NEGATIVE = 10;

    public static final String KEY_EXTRA_PRIVATE_KEYS = GeneralUtil.generateUniqueExtraKey(
            "KEY_EXTRA_PRIVATE_KEYS", CheckKeysActivity.class);
    public static final String KEY_EXTRA_BOTTOM_TITLE = GeneralUtil.generateUniqueExtraKey(
            "KEY_EXTRA_BOTTOM_TITLE", CheckKeysActivity.class);
    public static final String KEY_EXTRA_CHECK_BUTTON_TITLE = GeneralUtil.generateUniqueExtraKey(
            "KEY_EXTRA_CHECK_BUTTON_TITLE", CheckKeysActivity.class);
    public static final String KEY_EXTRA_NEGATIVE_ACTION_BUTTON_TITLE =
            GeneralUtil.generateUniqueExtraKey(
                    "KEY_EXTRA_NEGATIVE_ACTION_BUTTON_TITLE", CheckKeysActivity.class);
    public static final String KEY_EXTRA_IS_THROW_ERROR_IF_DUPLICATE_FOUND =
            GeneralUtil.generateUniqueExtraKey(
                    "KEY_EXTRA_IS_THROW_ERROR_IF_DUPLICATE_FOUND", CheckKeysActivity.class);

    private ArrayList<PrivateKeyDetails> privateKeyDetailsList;
    private EditText editTextKeyPassword;
    private View progressBar;
    private String bottomTitle;
    private String checkButtonTitle;
    private String anotherAccountButtonTitle;
    private boolean isThrowErrorIfDuplicateFound;

    public static Intent newIntent(Context context, ArrayList<PrivateKeyDetails> privateKeys,
                                   String bottomTitle, String checkButtonTitle,
                                   String negativeActionButtonTitle,
                                   boolean isThrowErrorIfDuplicateFound) {
        Intent intent = new Intent(context, CheckKeysActivity.class);
        intent.putExtra(KEY_EXTRA_PRIVATE_KEYS, privateKeys);
        intent.putExtra(KEY_EXTRA_BOTTOM_TITLE, bottomTitle);
        intent.putExtra(KEY_EXTRA_CHECK_BUTTON_TITLE, checkButtonTitle);
        intent.putExtra(KEY_EXTRA_NEGATIVE_ACTION_BUTTON_TITLE, negativeActionButtonTitle);
        intent.putExtra(KEY_EXTRA_IS_THROW_ERROR_IF_DUPLICATE_FOUND, isThrowErrorIfDuplicateFound);
        return intent;
    }

    @Override
    public boolean isDisplayHomeAsUpEnabled() {
        return false;
    }

    @Override
    public int getContentViewResourceId() {
        return R.layout.activity_check_keys;
    }

    @Override
    public View getRootView() {
        return findViewById(R.id.layoutContent);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent() != null) {
            this.privateKeyDetailsList = getIntent().getParcelableArrayListExtra
                    (KEY_EXTRA_PRIVATE_KEYS);
            this.bottomTitle = getIntent().getStringExtra(KEY_EXTRA_BOTTOM_TITLE);
            this.checkButtonTitle = getIntent().getStringExtra(KEY_EXTRA_CHECK_BUTTON_TITLE);
            this.anotherAccountButtonTitle = getIntent().getStringExtra
                    (KEY_EXTRA_NEGATIVE_ACTION_BUTTON_TITLE);
            this.isThrowErrorIfDuplicateFound = getIntent().getBooleanExtra(
                    KEY_EXTRA_IS_THROW_ERROR_IF_DUPLICATE_FOUND, false);
        }

        initViews();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonCheck:
                UIUtil.hideSoftInput(this, editTextKeyPassword);
                if (privateKeyDetailsList != null && !privateKeyDetailsList.isEmpty()) {
                    if (TextUtils.isEmpty(editTextKeyPassword.getText().toString())) {
                        showInfoSnackbar(editTextKeyPassword,
                                getString(R.string.passphrase_must_be_non_empty));
                    } else {
                        if (getSnackBar() != null) {
                            getSnackBar().dismiss();
                        }

                        getSupportLoaderManager().restartLoader(R.id
                                .loader_id_encrypt_and_save_private_keys_infos, null, this);
                    }
                }
                break;

            case R.id.buttonNegativeAction:
                setResult(RESULT_NEGATIVE);
                finish();
                break;
        }
    }

    @Override
    public Loader<LoaderResult> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case R.id.loader_id_encrypt_and_save_private_keys_infos:
                progressBar.setVisibility(View.VISIBLE);
                return new EncryptAndSavePrivateKeysAsyncTaskLoader(this, privateKeyDetailsList,
                        editTextKeyPassword.getText().toString(), isThrowErrorIfDuplicateFound);

            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<LoaderResult> loader, LoaderResult loaderResult) {
        handleLoaderResult(loader, loaderResult);
    }

    @Override
    public void onLoaderReset(Loader<LoaderResult> loader) {

    }

    @Override
    public void handleFailureLoaderResult(int loaderId, Exception e) {
        switch (loaderId) {
            case R.id.loader_id_encrypt_and_save_private_keys_infos:
                progressBar.setVisibility(View.GONE);
                showInfoSnackbar(getRootView(), TextUtils.isEmpty(e.getMessage())
                        ? getString(R.string.can_not_read_this_private_key) : e.getMessage());
                break;
        }
    }

    @Override
    public void handleSuccessLoaderResult(int loaderId, Object result) {
        switch (loaderId) {
            case R.id.loader_id_encrypt_and_save_private_keys_infos:
                progressBar.setVisibility(View.GONE);
                boolean booleanResult = (boolean) result;
                if (booleanResult) {
                    setResult(Activity.RESULT_OK);
                    finish();
                } else {
                    showInfoSnackbar(getRootView(), getString(R.string
                            .password_is_incorrect));
                }
                break;
        }
    }

    private void initViews() {
        if (findViewById(R.id.buttonCheck) != null) {
            Button buttonCheck = (Button) findViewById(R.id.buttonCheck);
            buttonCheck.setText(checkButtonTitle);
            buttonCheck.setOnClickListener(this);
        }

        if (findViewById(R.id.buttonNegativeAction) != null) {
            Button buttonSelectAnotherAccount =
                    (Button) findViewById(R.id.buttonNegativeAction);
            buttonSelectAnotherAccount.setText(anotherAccountButtonTitle);
            buttonSelectAnotherAccount.setOnClickListener(this);
        }

        TextView textViewCheckKeysTitle = (TextView) findViewById(R.id.textViewCheckKeysTitle);
        if (textViewCheckKeysTitle != null) {
            textViewCheckKeysTitle.setText(bottomTitle);
        }

        editTextKeyPassword = (EditText) findViewById(R.id.editTextKeyPassword);
        progressBar = findViewById(R.id.progressBar);
    }
}