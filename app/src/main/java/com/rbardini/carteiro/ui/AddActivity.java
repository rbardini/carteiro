package com.rbardini.carteiro.ui;

import android.app.AlertDialog;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.rbardini.carteiro.CarteiroApplication;
import com.rbardini.carteiro.R;
import com.rbardini.carteiro.db.DatabaseHelper;
import com.rbardini.carteiro.model.Shipment;
import com.rbardini.carteiro.model.ShipmentRecord;
import com.rbardini.carteiro.ui.transition.MorphTransition;
import com.rbardini.carteiro.ui.transition.RoundIconTransition;
import com.rbardini.carteiro.util.AnalyticsUtils;
import com.rbardini.carteiro.util.PostalUtils;
import com.rbardini.carteiro.util.UIUtils;
import com.rbardini.carteiro.util.validator.TrackingCodeValidation;
import com.rbardini.carteiro.util.validator.TrackingCodeValidator;

import java.util.Date;
import java.util.Locale;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

public class AddActivity extends AppCompatActivity {
  private static final String TAG = "AddActivity";
  private static final String SHIPMENT_KEY = "shipment";
  private static final int DEFAULT_INPUT_TYPES = InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS;

  private static final int NOT_FOUND      = 0x1;
  private static final int DELIVERED_ITEM = 0x2;
  private static final int RETURNED_ITEM  = 0x4;
  private static final int NET_ERROR      = 0x8;

  private boolean mIsFormView;

  private SharedPreferences mPrefs;
  private ActionBar mActionBar;

  private View mFormView;
  private View mConfirmationView;
  private View mLoadingView;
  private TextView mContentText;
  private TextInputLayout mTrackingNumberInput;
  private TextInputEditText mTrackingNumberField;
  private TextInputEditText mItemNameField;
  private Button mCancelButton;
  private Button mAddButton;
  private Button mSkipButton;
  private Button mJustOnceButton;
  private Button mAlwaysButton;

  private Shipment mShipment;
  private AsyncTask<?, ?, ?> mFetchShipmentTask;
  private DatabaseHelper dh;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_add);
    setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);

    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    mIsFormView = true;

    mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
    mActionBar = getSupportActionBar();

    mFormView = findViewById(R.id.form_layout);
    mConfirmationView = findViewById(R.id.confirmation_layout);
    mLoadingView = findViewById(R.id.loading_indicator);
    mContentText = findViewById(R.id.content_text);
    mTrackingNumberInput = findViewById(R.id.trk_code_input);
    mTrackingNumberField = findViewById(R.id.trk_code_fld);
    mItemNameField = findViewById(R.id.item_desc_fld);
    mCancelButton = findViewById(R.id.cancel_button);
    mAddButton = findViewById(R.id.add_button);
    mSkipButton = findViewById(R.id.skip_button);
    mJustOnceButton = findViewById(R.id.just_once_button);
    mAlwaysButton = findViewById(R.id.always_button);

    if (savedInstanceState != null) mShipment = (Shipment) savedInstanceState.getSerializable(SHIPMENT_KEY);
    dh = ((CarteiroApplication) getApplication()).getDatabaseHelper();

    setupTransition();
    setupFormFields();
    handleIntent();
  }

  @Override
  public void onResume() {
    super.onResume();

    if (mShipment != null) {
      mFetchShipmentTask = new FetchShipmentTask().execute(mShipment);
    }
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    cancelShipmentFetch();
  }

  @Override
  public void onSaveInstanceState(Bundle savedInstanceState) {
    savedInstanceState.putSerializable(SHIPMENT_KEY, mShipment);
    super.onSaveInstanceState(savedInstanceState);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.add_actions, menu);

    TypedArray attrs = getTheme().obtainStyledAttributes(new int[] {android.R.attr.textColorPrimary});
    menu.findItem(R.id.scan_opt).getIcon().mutate().setColorFilter(attrs.getColor(0, Color.BLACK), PorterDuff.Mode.SRC_ATOP);

    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    menu.findItem(R.id.scan_opt).setVisible(mIsFormView);
    return super.onPrepareOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    final int itemId = item.getItemId();

    switch (itemId) {
      case R.id.scan_opt:
        scanBarcode();
        return true;

      default:
        return super.onOptionsItemSelected(item);
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
    if (scanResult != null && data != null) {
      try {
        String contents = data.getStringExtra("SCAN_RESULT");
        String cod = validateCod(contents);
        mTrackingNumberField.setText(cod);
        mItemNameField.requestFocus();
      } catch (Exception e) {
        UIUtils.showToast(this, e.getMessage());
      }
    }
  }

  @Override
  protected void onNewIntent(Intent intent) {
    setIntent(intent);
    handleIntent();
  }

  private void scanBarcode() {
    IntentIntegrator scan = new IntentIntegrator(this);
    AlertDialog install = scan.initiateScan();

    if (install != null) {
      install.setTitle(R.string.title_alert_barcode_install);
      install.setMessage(getString(R.string.msg_alert_barcode_install));
      install.getButton(DialogInterface.BUTTON_POSITIVE).setText(R.string.install_btn);
      install.getButton(DialogInterface.BUTTON_NEGATIVE).setText(R.string.negative_btn);
    }
  }

  public void onAddClick(View v) {
    String cod = mTrackingNumberField.getText().toString().toUpperCase(Locale.getDefault());
    String desc = mItemNameField.getText().toString().trim();
    String error = null;

    try {
      cod = validateCod(cod);

      mShipment = new Shipment(cod);
      if (!desc.equals("")) mShipment.setName(desc);
      mFetchShipmentTask = new FetchShipmentTask().execute(mShipment);

    } catch (Exception e) {
      error = e.getMessage();

    } finally {
      mTrackingNumberInput.setError(error);
    }
  }

  public void onCancelClick(View v) {
    if (mIsFormView) {
      dismiss(v);

    } else {
      hideConfirmationView();
      resetConfirmation();
      showFormView();

      mShipment = null;
    }
  }

  public void onSkipClick(View v) {
    cancelShipmentFetch();
    addShipment();
  }

  public void onJustOnceClick(View v) {
    addShipment();
  }

  public void onAlwaysClick(View v) {
    int preferenceKey = getConfirmationPreferenceKeyById((Integer) v.getTag());
    mPrefs.edit().putBoolean(getString(preferenceKey), true).apply();

    onJustOnceClick(null);
  }

  public void dismiss(View v) {
    finishAfterTransition();
  }

  private void hideSoftKeyboard() {
    InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
    imm.hideSoftInputFromWindow(mAddButton.getWindowToken(), 0);
  }

  private void toggleFormView(boolean show) {
    int visibility = show ? View.VISIBLE : View.GONE;

    mFormView.setVisibility(visibility);
    mCancelButton.setVisibility(visibility);
    mAddButton.setVisibility(visibility);

    mIsFormView = show;
    invalidateOptionsMenu();
  }

  private void showFormView() {
    toggleFormView(true);
  }

  private void hideFormView() {
    toggleFormView(false);
    hideSoftKeyboard();
  }

  private void toggleLoadingView(boolean show) {
    int visibility = show ? View.VISIBLE : View.GONE;
    mLoadingView.setVisibility(visibility);
    mSkipButton.setVisibility(visibility);
  }

  private void showLoadingView() {
    mActionBar.setTitle(R.string.title_fetching_item_data);
    toggleLoadingView(true);
  }

  private void hideLoadingView() {
    toggleLoadingView(false);
  }

  private void toggleConfirmationView(boolean show) {
    int visibility = show ? View.VISIBLE : View.GONE;

    mConfirmationView.setVisibility(visibility);
    mCancelButton.setVisibility(visibility);
    mJustOnceButton.setVisibility(visibility);
    mAlwaysButton.setVisibility(visibility);
  }

  private void showConfirmationView(int id) {
    setConfirmation(id);
    toggleConfirmationView(true);
  }

  private void hideConfirmationView() {
    toggleConfirmationView(false);
  }

  private void setConfirmation(int id) {
    final String title, message;

    switch (id) {
      case NOT_FOUND:
        title = getString(R.string.title_alert_not_found);
        message = getString(R.string.msg_alert_not_found, mShipment.getNumber());
        break;

      case DELIVERED_ITEM:
        title = getString(R.string.title_alert_delivered_item);
        message = getString(R.string.msg_alert_delivered_item);
        break;

      case RETURNED_ITEM:
        title = getString(R.string.title_alert_returned_item);
        message = getString(R.string.msg_alert_returned_item);
        break;

      case NET_ERROR:
      default:
        title = getString(R.string.title_alert_net_error);
        message = getString(R.string.msg_alert_net_error);
        break;
    }

    mActionBar.setTitle(title);
    mContentText.setText(message);
    mAlwaysButton.setTag(id);
  }

  private void resetConfirmation() {
    mActionBar.setTitle(R.string.title_add);
    mContentText.setText(null);
  }

  private void setupTransition() {
    View content = findViewById(R.id.content);

    if (!RoundIconTransition.setup(this, content)) {
      int dialogColor = ContextCompat.getColor(this, R.color.theme_background);
      int dialogRadius = getResources().getDimensionPixelSize(R.dimen.dialog_radius);

      MorphTransition.setup(this, content, dialogColor, dialogRadius);
    }
  }

  private void setupFormFields() {
    if (mPrefs.getBoolean(getString(R.string.pref_key_use_adaptive_keyboard), true)) {
      mTrackingNumberField.addTextChangedListener(new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
          int length = s.length();
          mTrackingNumberField.setInputType(DEFAULT_INPUT_TYPES | (length < 2 || length > 10 ? InputType.TYPE_CLASS_TEXT : InputType.TYPE_CLASS_NUMBER));
          mAddButton.setEnabled(length == 13);
        }

        @Override
        public void afterTextChanged(Editable s) {}
      });
    }

    mTrackingNumberField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
      @Override
      public void onFocusChange(View view, boolean hasFocus) {
        if (hasFocus) return;

        String error = null;

        try {
          validateCod(mTrackingNumberField.getText().toString().toUpperCase(Locale.getDefault()));

        } catch (Exception e) {
          error = e.getMessage();

        } finally {
          mTrackingNumberInput.setError(error);
        }
      }
    });

    mItemNameField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
      @Override
      public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        onAddClick(null);
        return true;
      }
    });
  }

  private void handleIntent() {
    Intent intent = getIntent();
    String cod;

    if (Intent.ACTION_VIEW.equals(intent.getAction())) {
      Uri data = intent.getData();

      if (data == null) {
        fillFromClipboard();
        return;
      }

      cod = data.getQueryParameter("P_COD_UNI");

      if (cod == null) {
        cod = data.getQueryParameter("P_COD_LIS");
      }

      if (cod != null) {
        try {
          cod = validateCod(cod);
          mTrackingNumberField.setText(cod);
          onAddClick(null);

        } catch (Exception e) {
          UIUtils.showToast(this, e.getMessage());
        }

      } else {
        UIUtils.showToast(this, R.string.msg_alert_cod_not_found);
      }

    } else {
      fillFromClipboard();
    }
  }

  private void fillFromClipboard() {
    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
    String cod;

    if (clipboard.hasPrimaryClip()) {
      ClipDescription clipDesc = clipboard.getPrimaryClipDescription();

      if (clipDesc.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN) || clipDesc.hasMimeType(ClipDescription.MIMETYPE_TEXT_HTML)) {
        CharSequence text = clipboard.getPrimaryClip().getItemAt(0).getText();

        if (text != null) {
          cod = text.toString().trim();

          try {
            cod = validateCod(cod);

            if (!dh.isPostalItem(cod)) {
              mTrackingNumberField.setText(cod);
            }

          } catch (Exception e) {}
        }
      }
    }
  }

  private String validateCod(String cod) throws Exception {
    TrackingCodeValidation validation = TrackingCodeValidator.validate(cod);
    String error;

    if (validation.isValid()) {
      if (dh.isPostalItem(cod)) {
        error = getString(R.string.toast_item_already_exists, cod);
        throw new Exception(error);
      }
      return validation.getCod();
    }

    switch (validation.getResult()) {
      case EMPTY:
        error = getString(R.string.msg_alert_empty_cod);
        break;
      case WRONG_LENGTH:
        error = getString(R.string.msg_alert_wrong_length_cod);
        break;
      case BAD_FORMAT:
        error = getString(R.string.msg_alert_bad_format_cod);
        break;
      case INVALID_CHECK_DIGIT:
        error = getString(R.string.msg_alert_invalid_dv_cod);
        break;
      default:
        error = getString(R.string.msg_alert_default_error_cod);
        break;
    }

    throw new Exception(error);
  }

  private void addShipment() {
    if (mShipment.isEmpty()) {
      mShipment.addRecord(new ShipmentRecord(new Date(), PostalUtils.Status.NAO_ENCONTRADO));
    }

    mShipment.saveTo(dh);

    Intent intent = new Intent(this, RecordActivity.class)
      .putExtra(RecordActivity.EXTRA_SHIPMENT, mShipment)
      .putExtra(RecordActivity.EXTRA_IS_NEW_SHIPMENT, true);
    startActivity(intent);
    finish();

    AnalyticsUtils.recordShipmentAdd(this, mShipment);
  }

  private void cancelShipmentFetch() {
    if (mFetchShipmentTask != null && mFetchShipmentTask.getStatus() == AsyncTask.Status.RUNNING) {
      mFetchShipmentTask.cancel(true);
    }

    if (dh.inTransaction()) {
      dh.endTransaction();
    }
  }

  private int getConfirmationPreferenceKeyById(int id) {
    switch (id) {
      case NOT_FOUND:
        return R.string.pref_key_always_add_not_found;

      case DELIVERED_ITEM:
        return R.string.pref_key_always_add_delivered;

      case RETURNED_ITEM:
        return R.string.pref_key_always_add_returned;

      case NET_ERROR:
        return R.string.pref_key_always_add_net_error;

      default:
        return -1;
    }
  }

  private boolean shouldAskForConfirmation(int id) {
    int preferenceKey = getConfirmationPreferenceKeyById(id);
    return preferenceKey != -1 && !mPrefs.getBoolean(getString(preferenceKey), false);
  }

  private class FetchShipmentTask extends AsyncTask<Object, Void, Shipment> {
    private String error;

    @Override
    protected void onPreExecute() {
      hideFormView();
      showLoadingView();
    }

    @Override
    protected Shipment doInBackground(Object... params) {
      Shipment shipment = (Shipment) params[0];

      try {
        shipment.fetchRecords(AddActivity.this);

        if (shipment.isEmpty()) {
          error = getString(R.string.title_alert_not_found);

        } else {
          String status = shipment.getLastRecord().getStatus();

          if (status.equals(PostalUtils.Status.ENTREGA_EFETUADA)) {
            error = getString(R.string.title_alert_delivered_item);

          } else if (status.equals(PostalUtils.Status.DEVOLVIDO_AO_REMETENTE)) {
            error = getString(R.string.title_alert_returned_item);
          }
        }

      } catch (Exception e) {
        error = e.getMessage();
      }

      return shipment;
    }

    @Override
    protected void onPostExecute(Shipment shipment) {
      if (error != null) {
        int id =
          error.equals(PostalUtils.Error.NET_ERROR) ? NET_ERROR :
          error.equals(getString(R.string.title_alert_not_found)) ? NOT_FOUND :
          error.equals(getString(R.string.title_alert_delivered_item)) ? DELIVERED_ITEM :
          error.equals(getString(R.string.title_alert_returned_item)) ? RETURNED_ITEM : -1;

        if (id == -1) {
          UIUtils.showToast(AddActivity.this, R.string.toast_unexpected_error);
          Log.e(TAG, error);

        } else {
          if (shouldAskForConfirmation(id)) {
            hideLoadingView();
            showConfirmationView(id);

          } else {
            addShipment();
          }
        }

      } else {
        addShipment();
      }

      mFetchShipmentTask = null;
    }

    @Override
    protected void onCancelled(Shipment shipment) {
      mFetchShipmentTask = null;
    }
  }
}
