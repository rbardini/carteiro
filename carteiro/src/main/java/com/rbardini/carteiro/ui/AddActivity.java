package com.rbardini.carteiro.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.rbardini.carteiro.CarteiroApplication;
import com.rbardini.carteiro.R;
import com.rbardini.carteiro.db.DatabaseHelper;
import com.rbardini.carteiro.model.PostalItem;
import com.rbardini.carteiro.model.PostalItemRecord;
import com.rbardini.carteiro.model.PostalRecord;
import com.rbardini.carteiro.util.PostalUtils;
import com.rbardini.carteiro.util.UIUtils;
import com.rbardini.carteiro.util.validator.TrackingCodeValidation;
import com.rbardini.carteiro.util.validator.TrackingCodeValidator;

import org.alfredlibrary.utilitarios.correios.Rastreamento;
import org.alfredlibrary.utilitarios.correios.RegistroRastreamento;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddActivity extends AppCompatActivity {
  private static final int DEFAULT_INPUT_TYPES = InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS;

  public static final int NOT_FOUND      = 0x1;
  public static final int DELIVERED_ITEM = 0x2;
  public static final int RETURNED_ITEM  = 0x4;
  public static final int NET_ERROR      = 0x8;

  private boolean mIsFormView;

  private CarteiroApplication app;
  private ActionBar mActionBar;

  private View mFormView;
  private View mLoadingView;
  private TextView mContentText;
  private TextInputLayout mTrackingNumberInput;
  private EditText mTrackingNumberField;
  private EditText mItemNameField;
  private View mButtonBar;
  private Button mAddButton;

  private PostalItemRecord mPostalItemRecord;
  private AsyncTask<?, ?, ?> mRequestPostalItemRecordTask;
  private DatabaseHelper dh;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.add);
    setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);

    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    mIsFormView = true;

    app = (CarteiroApplication) getApplication();
    mActionBar = getSupportActionBar();

    mFormView = findViewById(R.id.form_layout);
    mLoadingView = findViewById(R.id.loading_indicator);
    mContentText = (TextView) findViewById(R.id.content_text);
    mTrackingNumberInput = (TextInputLayout) findViewById(R.id.trk_code_input);
    mTrackingNumberField = (EditText) findViewById(R.id.trk_code_fld);
    mItemNameField = (EditText) findViewById(R.id.item_desc_fld);
    mButtonBar = findViewById(R.id.button_bar);
    mAddButton = (Button) findViewById(R.id.add_button);

    if (savedInstanceState != null) mPostalItemRecord = (PostalItemRecord) savedInstanceState.getSerializable("postalItemRecord");
    dh = ((CarteiroApplication) getApplication()).getDatabaseHelper();

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
    mTrackingNumberField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
      @Override
      public void onFocusChange(View view, boolean hasFocus) {
        if (hasFocus) return;

        String error = null;
        try {
          parseCod(mTrackingNumberField.getText().toString().toUpperCase(Locale.getDefault()));
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

    handleIntent();
  }

  @Override
  public void onResume() {
    super.onResume();

    if (mPostalItemRecord != null) {
      mRequestPostalItemRecordTask = new RequestPostalItemRecordTask().execute(mPostalItemRecord);
    }
  }

  @Override
  public void onDestroy() {
    super.onDestroy();

    if (mRequestPostalItemRecordTask != null && mRequestPostalItemRecordTask.getStatus() == AsyncTask.Status.RUNNING) {
      mRequestPostalItemRecordTask.cancel(true);
    }
    if (dh.inTransaction()) {
      dh.endTransaction();
    }
  }

  @Override
  public void onSaveInstanceState(Bundle savedInstanceState) {
    savedInstanceState.putSerializable("postalItemRecord", mPostalItemRecord);
    super.onSaveInstanceState(savedInstanceState);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.add_actions, menu);
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
        String cod = parseCod(contents);
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

  public void scanBarcode() {
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
    String desc = mItemNameField.getText().toString();

    if (mPostalItemRecord != null) {
      addPostalItemRecord();

    } else {
      String error = null;
      try {
        cod = parseCod(cod);
        mPostalItemRecord = new PostalItemRecord(new PostalItem(cod, (desc.trim().equals("") ? null : desc)));
        mRequestPostalItemRecordTask = new RequestPostalItemRecordTask().execute(mPostalItemRecord);
      } catch (Exception e) {
        error = e.getMessage();
      } finally {
        mTrackingNumberInput.setError(error);
      }
    }
  }

  public void onCancelClick(View v) {
    if (mPostalItemRecord != null) {
      hideConfirmationView();
      resetConfirmation();
      showFormView();

      mPostalItemRecord = null;

    } else {
      finish();
    }
  }

  private void toggleFormView(boolean show) {
    int visibility = show ? View.VISIBLE : View.GONE;

    if (show) mActionBar.show(); else mActionBar.hide();
    mFormView.setVisibility(visibility);
    mButtonBar.setVisibility(visibility);

    mIsFormView = show;
    invalidateOptionsMenu();
  }

  private void showFormView() {
    toggleFormView(true);
  }

  private void hideFormView() {
    toggleFormView(false);
  }

  private void toggleLoadingView(boolean show) {
    int visibility = show ? View.VISIBLE : View.GONE;
    mLoadingView.setVisibility(visibility);
  }

  private void showLoadingView() {
    toggleLoadingView(true);
  }

  private void hideLoadingView() {
    toggleLoadingView(false);
  }

  private void toggleConfirmationView(boolean show) {
    int visibility = show ? View.VISIBLE : View.GONE;

    if (show) mActionBar.show(); else mActionBar.hide();
    mContentText.setVisibility(visibility);
    mButtonBar.setVisibility(visibility);
  }

  private void showConfirmationView() {
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
        message = getString(R.string.msg_alert_not_found, mPostalItemRecord.getCod());
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
  }

  private void resetConfirmation() {
    mActionBar.setTitle(R.string.title_add);
    mContentText.setText(null);
  }

  private void handleIntent() {
    Intent intent = getIntent();
    if (Intent.ACTION_VIEW.equals(intent.getAction())) {
      Uri data = intent.getData();
      String cod = data.getQueryParameter("P_COD_UNI");
      if (cod == null) { cod = data.getQueryParameter("P_COD_LIS"); }
      if (cod != null) {
        try {
          cod = parseCod(cod);
          mTrackingNumberField.setText(cod);
          onAddClick(null);
        } catch (Exception e) {
          UIUtils.showToast(this, e.getMessage());
        }
      } else {
        UIUtils.showToast(this, getString(R.string.msg_alert_cod_not_found));
      }
    }
  }

  private String parseCod(String cod) throws Exception {
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

  private void addPostalItemRecord() {
    mPostalItemRecord.saveTo(dh);
    app.setUpdatedList();

    Intent intent = new Intent(this, RecordActivity.class);
    intent.putExtra("postalItem", mPostalItemRecord.getPostalItem());
    intent.putExtra("isNew", true);
    startActivity(intent);
    finish();
  }

  private class RequestPostalItemRecordTask extends AsyncTask<Object, Void, PostalItemRecord> {
    private String error;

    @Override
    protected void onPreExecute() {
      hideFormView();
      showLoadingView();
    }

    @Override
    protected PostalItemRecord doInBackground(Object... params) {
      PostalItemRecord pir = (PostalItemRecord) params[0];

      PostalItem pi = pir.getPostalItem();
      List<PostalRecord> prList = new ArrayList<>();

      try {
        List<RegistroRastreamento> rrList = Rastreamento.rastrear(pi.getCod());
        pi.setReg(rrList.get(0));

        for (int i=0, length=rrList.size(); i<length; i++) {
          PostalRecord pr = new PostalRecord(pi.getCod(), length-i-1, rrList.get(i));
          prList.add(pr);
        }

        if (pi.getStatus().equals(PostalUtils.Status.ENTREGA_EFETUADA)) {
          throw new Exception(getString(R.string.title_alert_delivered_item));
        }
        if (pi.getStatus().equals(PostalUtils.Status.DEVOLVIDO_AO_REMETENTE)) {
          throw new Exception(getString(R.string.title_alert_returned_item));
        }

      } catch (Exception e) {
        error = e.getMessage();
        if (error.equals(PostalUtils.Error.NOT_FOUND) || error.startsWith(PostalUtils.Error.NET_ERROR)) {
          PostalRecord pr = new PostalRecord(pi.getCod(), -1, new Date(), PostalUtils.Status.NAO_ENCONTRADO);
          pi.setReg(pr.getReg());
          prList.add(pr);
        }

      } finally {
        pir.setPostalItem(pi);
        pir.setPostalRecords(prList);
      }

      return pir;
    }

    @Override
    protected void onPostExecute(PostalItemRecord pir) {
      if (error != null) {
        int id = error.equals(PostalUtils.Error.NOT_FOUND) ? NOT_FOUND :
          error.startsWith(PostalUtils.Error.NET_ERROR) ? NET_ERROR :
          error.equals(getString(R.string.title_alert_delivered_item)) ? DELIVERED_ITEM :
          error.equals(getString(R.string.title_alert_returned_item)) ? RETURNED_ITEM : -1;

        if (id != -1) {
          hideLoadingView();
          setConfirmation(id);
          showConfirmationView();

        } else {
          UIUtils.showToast(AddActivity.this, error);
        }
      } else {
        addPostalItemRecord();
      }

      mRequestPostalItemRecordTask = null;
    }

    @Override
    protected void onCancelled(PostalItemRecord pir) {
      mRequestPostalItemRecordTask = null;
      hideLoadingView();
      showFormView();
    }
  }
}
