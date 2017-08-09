/*
 * Business Source License 1.0 © 2017 FlowCrypt Limited (tom@cryptup.org).
 * Use limitations apply. See https://github.com/FlowCrypt/flowcrypt-android/blob/master/LICENSE
 * Contributors: DenBond7
 */

package com.flowcrypt.email.ui.activity.base;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.flowcrypt.email.R;
import com.flowcrypt.email.model.KeyDetails;
import com.flowcrypt.email.model.ValidateKeyLoaderResult;
import com.flowcrypt.email.model.results.LoaderResult;
import com.flowcrypt.email.service.CheckClipboardToFindPrivateKeyService;
import com.flowcrypt.email.ui.activity.fragment.dialog.InfoDialogFragment;
import com.flowcrypt.email.ui.loader.ValidateKeyAsyncTaskLoader;
import com.flowcrypt.email.util.GeneralUtil;
import com.flowcrypt.email.util.UIUtil;

/**
 * The base import key activity. This activity defines a logic of import a key (private or
 * public) via select a file or using clipboard.
 *
 * @author Denis Bondarenko
 *         Date: 03.08.2017
 *         Time: 12:35
 *         E-mail: DenBond7@gmail.com
 */

public abstract class BaseImportKeyActivity extends BaseCheckClipboardBackStackActivity
        implements View.OnClickListener, LoaderManager.LoaderCallbacks<LoaderResult> {

    public static final String KEY_EXTRA_IS_THROW_ERROR_IF_DUPLICATE_FOUND
            = GeneralUtil.generateUniqueExtraKey("KEY_EXTRA_IS_THROW_ERROR_IF_DUPLICATE_FOUND",
            BaseImportKeyActivity.class);

    public static final String KEY_EXTRA_PRIVATE_KEY_DETAILS_FROM_CLIPBOARD
            = GeneralUtil.generateUniqueExtraKey("KEY_EXTRA_PRIVATE_KEY_DETAILS_FROM_CLIPBOARD",
            BaseImportKeyActivity.class);

    public static final String KEY_EXTRA_TITLE
            = GeneralUtil.generateUniqueExtraKey("KEY_EXTRA_TITLE", BaseImportKeyActivity.class);

    private static final int REQUEST_CODE_SELECT_KEYS_FROM_FILES_SYSTEM = 10;
    private static final int REQUEST_CODE_PERMISSION_READ_EXTERNAL_STORAGE = 11;

    protected ClipboardManager clipboardManager;
    protected KeyDetails keyDetails;

    protected View layoutContentView;
    protected View layoutProgress;
    protected boolean isCheckingPrivateKeyNow;
    protected boolean isThrowErrorIfDuplicateFound;
    protected boolean isCheckClipboardFromServiceEnable = true;
    private String title;

    public abstract void onKeyFromFileValidated();

    public abstract void onKeyFromClipBoardValidated();

    public static Intent newIntent(Context context, String title, Class<?> cls) {
        return newIntent(context, title, false, cls);
    }

    public static Intent newIntent(Context context, String title, boolean
            isThrowErrorIfDuplicateFound, Class<?> cls) {
        return newIntent(context, title, null, isThrowErrorIfDuplicateFound, cls);
    }

    public static Intent newIntent(Context context, String title,
                                   KeyDetails keyDetails,
                                   boolean isThrowErrorIfDuplicateFound, Class<?> cls) {
        Intent intent = new Intent(context, cls);
        intent.putExtra(KEY_EXTRA_TITLE, title);
        intent.putExtra(KEY_EXTRA_PRIVATE_KEY_DETAILS_FROM_CLIPBOARD, keyDetails);
        intent.putExtra(KEY_EXTRA_IS_THROW_ERROR_IF_DUPLICATE_FOUND, isThrowErrorIfDuplicateFound);
        return intent;
    }

    @Override
    public int getContentViewResourceId() {
        return R.layout.activity_import_private_key;
    }

    @Override
    public View getRootView() {
        return findViewById(R.id.layoutContent);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bindService(new Intent(this, CheckClipboardToFindPrivateKeyService.class),
                this, Context.BIND_AUTO_CREATE);

        if (getIntent() != null) {
            this.isThrowErrorIfDuplicateFound =
                    getIntent().getBooleanExtra(KEY_EXTRA_IS_THROW_ERROR_IF_DUPLICATE_FOUND, false);
            this.keyDetails =
                    getIntent().getParcelableExtra(KEY_EXTRA_PRIVATE_KEY_DETAILS_FROM_CLIPBOARD);
            this.title =
                    getIntent().getStringExtra(KEY_EXTRA_TITLE);
        }

        clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

        initViews();

        if (keyDetails != null) {
            onKeyFromClipBoardValidated();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (isServiceBound && !isCheckingPrivateKeyNow && isCheckClipboardFromServiceEnable) {
            keyDetails = checkClipboardToFindPrivateKeyService.getKeyDetails();

            if (keyDetails != null) {
                onKeyFromClipBoardValidated();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        isCheckClipboardFromServiceEnable = true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_SELECT_KEYS_FROM_FILES_SYSTEM:
                isCheckClipboardFromServiceEnable = false;

                switch (resultCode) {
                    case Activity.RESULT_OK:
                        if (data != null) {
                            keyDetails = new KeyDetails(
                                    GeneralUtil.getFileNameFromUri(this, data.getData()),
                                    null,
                                    data.getData(),
                                    KeyDetails.Type.FILE, isPrivateKeyChecking(), null);

                            getSupportLoaderManager().restartLoader(R.id
                                    .loader_id_validate_key_from_file, null, this);
                        }
                        break;
                }
                break;

            default:
                super.onActivityResult(requestCode, resultCode, data);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_CODE_PERMISSION_READ_EXTERNAL_STORAGE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    runSelectFileIntent();
                } else {
                    UIUtil.showSnackbar(getRootView(),
                            getString(R.string.access_to_read_the_sdcard_id_denied),
                            getString(R.string.change), new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    GeneralUtil.showAppSettingScreen(BaseImportKeyActivity.this);
                                }
                            });
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (isCheckingPrivateKeyNow) {
            getSupportLoaderManager().destroyLoader(R.id.loader_id_validate_key_from_file);
            getSupportLoaderManager()
                    .destroyLoader(R.id.loader_id_validate_key_from_clipboard);
            isCheckingPrivateKeyNow = false;
            UIUtil.exchangeViewVisibility(getApplicationContext(), false, layoutProgress,
                    layoutContentView);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonLoadFromFile:
                dismissSnackBar();
                keyDetails = null;

                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED) {
                    runSelectFileIntent();
                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        showAnExplanationForReadSdCard();
                    } else {
                        ActivityCompat.requestPermissions(this,
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                REQUEST_CODE_PERMISSION_READ_EXTERNAL_STORAGE);
                    }
                }
                break;

            case R.id.buttonLoadFromClipboard:
                dismissSnackBar();
                keyDetails = null;

                if (clipboardManager.hasPrimaryClip()) {
                    ClipData.Item item = clipboardManager.getPrimaryClip().getItemAt(0);
                    CharSequence privateKeyFromClipboard = item.getText();
                    if (!TextUtils.isEmpty(privateKeyFromClipboard)) {
                        keyDetails = new KeyDetails(null,
                                privateKeyFromClipboard.toString(),
                                KeyDetails.Type.CLIPBOARD, isPrivateKeyChecking());

                        getSupportLoaderManager().restartLoader(R.id
                                .loader_id_validate_key_from_clipboard, null, this);
                    } else {
                        showClipboardIsEmptyInfoDialog();
                    }
                } else {
                    showClipboardIsEmptyInfoDialog();
                }
                break;
        }
    }

    @Override
    public Loader<LoaderResult> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case R.id.loader_id_validate_key_from_file:
                isCheckingPrivateKeyNow = true;
                UIUtil.exchangeViewVisibility(getApplicationContext(),
                        true, layoutProgress, layoutContentView);
                return new ValidateKeyAsyncTaskLoader(getApplicationContext(),
                        keyDetails, true);

            case R.id.loader_id_validate_key_from_clipboard:
                isCheckingPrivateKeyNow = true;
                UIUtil.exchangeViewVisibility(getApplicationContext(),
                        true, layoutProgress, layoutContentView);
                return new ValidateKeyAsyncTaskLoader(getApplicationContext(),
                        keyDetails, false);

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
        switch (loader.getId()) {
            case R.id.loader_id_validate_key_from_file:
            case R.id.loader_id_validate_key_from_clipboard:
                isCheckingPrivateKeyNow = false;
                UIUtil.exchangeViewVisibility(getApplicationContext(),
                        false, layoutProgress, layoutContentView);
        }
    }

    @Override
    public void handleSuccessLoaderResult(int loaderId, Object result) {
        switch (loaderId) {
            case R.id.loader_id_validate_key_from_file:
                isCheckingPrivateKeyNow = false;
                UIUtil.exchangeViewVisibility(getApplicationContext(),
                        false, layoutProgress, layoutContentView);
                ValidateKeyLoaderResult validateKeyLoaderResultForFile =
                        (ValidateKeyLoaderResult) result;

                if (validateKeyLoaderResultForFile.isValidated()) {
                    keyDetails.setValue(validateKeyLoaderResultForFile.getKey());
                    keyDetails.setPgpContact(validateKeyLoaderResultForFile.getPgpContact());
                    onKeyFromFileValidated();
                } else {
                    showInfoSnackbar(getRootView(),
                            getString(R.string.file_has_wrong_pgp_structure,
                                    isPrivateKeyChecking() ? getString(R.string.private_) :
                                            getString(R.string.public_)));
                }
                break;

            case R.id.loader_id_validate_key_from_clipboard:
                isCheckingPrivateKeyNow = false;
                UIUtil.exchangeViewVisibility(getApplicationContext(),
                        false, layoutProgress, layoutContentView);
                ValidateKeyLoaderResult validateKeyLoaderResultForClipboard =
                        (ValidateKeyLoaderResult) result;
                if (validateKeyLoaderResultForClipboard.isValidated()) {
                    keyDetails.setValue(validateKeyLoaderResultForClipboard.getKey());
                    keyDetails.setPgpContact(validateKeyLoaderResultForClipboard.getPgpContact());
                    onKeyFromClipBoardValidated();
                } else {
                    showInfoSnackbar(getRootView(),
                            getString(R.string.clipboard_has_wrong_structure,
                                    isPrivateKeyChecking() ? getString(R.string.private_) :
                                            getString(R.string.public_)));
                }
                break;

            default:
                super.handleSuccessLoaderResult(loaderId, result);
        }
    }

    @Override
    public void handleFailureLoaderResult(int loaderId, Exception e) {
        switch (loaderId) {
            case R.id.loader_id_validate_key_from_file:
            case R.id.loader_id_validate_key_from_clipboard:
                isCheckingPrivateKeyNow = false;
                UIUtil.exchangeViewVisibility(getApplicationContext(),
                        false, layoutProgress, layoutContentView);
                showInfoSnackbar(getRootView(), e.getMessage());
                break;

            default:
                super.handleFailureLoaderResult(loaderId, e);
        }
    }

    /**
     * Show an explanation to the user for read the sdcard.
     * After the user sees the explanation, we try again to request the permission.
     */
    private void showAnExplanationForReadSdCard() {
        UIUtil.showSnackbar(getRootView(),
                getString(R.string.read_sdcard_permission_explanation_text),
                getString(R.string.do_request), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ActivityCompat.requestPermissions(BaseImportKeyActivity.this,
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                REQUEST_CODE_PERMISSION_READ_EXTERNAL_STORAGE);
                    }
                });
    }

    /**
     * Show an explanation to the user for read the sdcard.
     * After the user sees the explanation, we try again to request the permission.
     */
    private void runSelectFileIntent() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(Intent.createChooser(intent,
                getString(R.string.select_key_to_import)),
                REQUEST_CODE_SELECT_KEYS_FROM_FILES_SYSTEM);
    }

    private void showClipboardIsEmptyInfoDialog() {
        InfoDialogFragment infoDialogFragment = InfoDialogFragment.newInstance
                (getString(R.string.hint), getString(R.string
                        .hint_clipboard_is_empty, isPrivateKeyChecking() ? getString(R.string
                        .private_) : getString(R.string.public_), getString(R.string.app_name)));
        infoDialogFragment.show(getSupportFragmentManager(),
                InfoDialogFragment.class.getSimpleName());
    }

    private void initViews() {
        layoutContentView = findViewById(R.id.layoutContentView);
        layoutProgress = findViewById(R.id.layoutProgress);

        TextView textViewImportKeyTitle = (TextView) findViewById(R.id.textViewImportKeyTitle);
        textViewImportKeyTitle.setText(title);

        if (findViewById(R.id.buttonLoadFromFile) != null) {
            findViewById(R.id.buttonLoadFromFile).setOnClickListener(this);
        }

        if (findViewById(R.id.buttonLoadFromClipboard) != null) {
            findViewById(R.id.buttonLoadFromClipboard).setOnClickListener(this);
        }
    }

}