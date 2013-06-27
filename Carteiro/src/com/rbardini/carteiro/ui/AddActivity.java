package com.rbardini.carteiro.ui;

import java.util.List;

import org.alfredlibrary.utilitarios.correios.Rastreamento;
import org.alfredlibrary.utilitarios.correios.RegistroRastreamento;
import org.alfredlibrary.utilitarios.texto.Texto;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.rbardini.carteiro.CarteiroApplication;
import com.rbardini.carteiro.model.PostalItem;
import com.rbardini.carteiro.model.PostalRecord;
import com.rbardini.carteiro.R;
import com.rbardini.carteiro.db.DatabaseHelper;
import com.rbardini.carteiro.util.PostalUtils;
import com.rbardini.carteiro.util.UIUtils;

public class AddActivity extends SherlockFragmentActivity implements AddDialogFragment.OnAddDialogActionListener, TextWatcher {
  private static final int DEFAULT_INPUT_TYPES = InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS;

  private CarteiroApplication app;

  private EditText trkCode;
  private EditText itemDesc;
  private CheckBox fav;
  private FragmentManager fragManager;

  private PostalItem pi;
  private ProgressDialog progress;
  private AsyncTask<?, ?, ?> task;
  private DatabaseHelper dh;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.add);
        setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);

        app = (CarteiroApplication) getApplication();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        trkCode = (EditText) findViewById(R.id.trk_code_fld);
        itemDesc = (EditText) findViewById(R.id.item_desc_fld);
        fav = (CheckBox) findViewById(R.id.star_chkbox);
        fragManager = getSupportFragmentManager();

        pi = (PostalItem) getLastCustomNonConfigurationInstance();
        progress = new ProgressDialog(this);
        dh = ((CarteiroApplication) getApplication()).getDatabaseHelper();

        trkCode.addTextChangedListener(this);

        handleIntent();
    }

    @Override
    public void onResume() {
      super.onResume();

      if (pi != null && fragManager.findFragmentByTag(AddDialogFragment.TAG) == null) {
          task = new InsertPostalItem(dh).execute(pi);
        }
    }

    @Override
    public void onDestroy() {
      super.onDestroy();

      if (progress.isShowing()) {
        progress.dismiss();
      }
        if (task != null && task.getStatus() == AsyncTask.Status.RUNNING) {
            task.cancel(true);
        }
      if (dh.inTransaction()) {
        dh.endTransaction();
      }
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
      return pi;
    }

    @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
      IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
      if (scanResult != null) {
        try {
          String contents = data.getStringExtra("SCAN_RESULT");
            validateCod(contents);
            trkCode.setText(contents);
            itemDesc.requestFocus();
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

    @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getSupportMenuInflater().inflate(R.menu.add_actions, menu);

    return super.onCreateOptionsMenu(menu);
  }

    @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        UIUtils.goHome(this);
        return true;

      case R.id.scan_opt:
        IntentIntegrator scan = new IntentIntegrator(this);
        AlertDialog install = scan.initiateScan();
        if (install != null) {
          install.setIcon(android.R.drawable.ic_dialog_info);
          install.setTitle(R.string.title_alert_barcode_install);
          install.setMessage(getString(R.string.msg_alert_barcode_install));
          install.getButton(DialogInterface.BUTTON_POSITIVE).setText(R.string.install_btn);
          install.getButton(DialogInterface.BUTTON_NEGATIVE).setText(R.string.negative_btn);
        }
        return true;

      default:
        return super.onOptionsItemSelected(item);
    }
    }

    @Override
  public void onConfirmAddPostalItem(PostalItem pi) {
      onPostalItemAdded(pi);
  }

  @Override
  public void onCancelAddPostalItem(PostalItem pi) {
    dh.deletePostalItem(pi.getCod());
    this.pi = null;
  }

  public void onAddPostalItemClick(View v) {
      String cod = trkCode.getText().toString();
      String desc = itemDesc.getText().toString();

      try {
        validateCod(cod);
        pi = new PostalItem(cod, (!desc.equals("") ? desc : null), fav.isChecked());
      task = new InsertPostalItem(dh).execute(pi);
      } catch(Exception e) {
        trkCode.setError(e.getMessage());
      }
    }

    private void handleIntent() {
      Intent intent = getIntent();
      if (Intent.ACTION_VIEW.equals(intent.getAction())) {
        Uri data = intent.getData();
        String cod = data.getQueryParameter("P_COD_UNI");
        if (cod == null) { cod = data.getQueryParameter("P_COD_LIS"); }
        if (cod != null) {
          try {
            validateCod(cod);
            trkCode.setText(cod);
                itemDesc.requestFocus();
            } catch (Exception e) {
              UIUtils.showToast(this, e.getMessage());
            }
        } else {
          UIUtils.showToast(this, getString(R.string.msg_alert_cod_not_found));
        }
      }
    }

    private void validateCod(String cod) throws Exception {
      if (cod == null || cod.equals("")) {
        throw new Exception(getString(R.string.msg_alert_no_cod));
    } else if (cod.length() != 13) {
      throw new Exception(getString(R.string.msg_alert_short_cod));
    } else if (!"".equals(Texto.manterNumeros(cod.substring(0, 1)))
          || !"".equals(Texto.manterNumeros(cod.substring(11, 13)))
          || Texto.manterNumeros(cod.substring(2, 11)).length() != 9) {
      throw new Exception(getString(R.string.msg_alert_wrong_cod));
    } else if (dh.isPostalItem(cod)) {
      throw new Exception(String.format(getString(R.string.toast_item_already_exists), cod));
    }
  }

  protected void onPostalItemAdded(PostalItem pi) {
    app.setUpdatedList();

    Intent intent = new Intent(this, RecordActivity.class);
    intent.putExtra("postalItem", pi);
    intent.putExtra("isNew", true);
    startActivity(intent);
    finish();
    }

    private class InsertPostalItem extends AsyncTask<Object, Void, PostalItem> {
      private DatabaseHelper dh;
      private String error;

      public InsertPostalItem(DatabaseHelper dh) {
        super();
        this.dh = dh;
      }

      @Override
    protected void onPreExecute() {
        progress.setMessage(getString(R.string.title_tracking_obj));
        progress.setOnCancelListener(new DialogInterface.OnCancelListener() {
          @Override
        public void onCancel(DialogInterface dialog) {
            cancel(true);
          }
        });
        progress.show();
      }

    @Override
    protected PostalItem doInBackground(Object... params) {
      PostalItem pi = (PostalItem) params[0];

      dh.beginTransaction();
      try {
        List<RegistroRastreamento> list = Rastreamento.rastrear(pi.getCod());
        pi.setReg(list.get(0));
        dh.insertPostalItem(pi);
        for(int i=0, length=list.size(); i<length; i++) {
          PostalRecord pr = new PostalRecord(pi.getCod(), length-i-1, list.get(i));
              dh.insertPostalRecord(pr);
        }
            dh.setTransactionSuccessful();
            if (pi.getStatus().equals(PostalUtils.Status.ENTREGA_EFETUADA)) {
              throw new Exception(getString(R.string.title_alert_delivered_item));
            }
            if (pi.getStatus().equals(PostalUtils.Status.DEVOLVIDO_AO_REMETENTE)) {
              throw new Exception(getString(R.string.title_alert_returned_item));
            }
      } catch (Exception e) {
        error = e.getMessage();
        if (error.equals("O sistema dos Correios não possui dados sobre o objeto informado") ||
          error.startsWith("Não foi possível obter contato com o site")) {
          dh.insertPostalItem(pi);
          PostalRecord pr = new PostalRecord(pi.getCod(), -1);
          pr.setStatus(PostalUtils.Status.NAO_ENCONTRADO);
          dh.insertPostalRecord(pr);
          dh.setTransactionSuccessful();
        }
        } finally {
          dh.endTransaction();
        }

      // Get fresh information from the database, including possible defaults for empty fields
      return dh.getPostalItem(pi.getCod());
    }

    @Override
    protected void onPostExecute(PostalItem pi) {
      if (progress.isShowing()) {
        progress.dismiss();
          }

      if (error != null) {
        int id = error.equals("O sistema dos Correios não possui dados sobre o objeto informado") ? AddDialogFragment.NOT_FOUND :
          error.startsWith("Não foi possível obter contato com o site") ? AddDialogFragment.NET_ERROR :
          error.equals(getString(R.string.title_alert_delivered_item)) ? AddDialogFragment.DELIVERED_ITEM :
          error.equals(getString(R.string.title_alert_returned_item)) ? AddDialogFragment.RETURNED_ITEM : -1;

        if (id != -1) {
          try {
            AddDialogFragment dialog = AddDialogFragment.newInstance(id, pi);
            dialog.setCancelable(false);
            dialog.show(fragManager, AddDialogFragment.TAG);
          } catch (Exception e) {
            onConfirmAddPostalItem(pi);
          }
        } else {
          UIUtils.showToast(AddActivity.this, error);
        }
      } else {
        onPostalItemAdded(pi);
      }

      task = null;
    }

    @Override
    protected void onCancelled(PostalItem pi) {
      if (progress.isShowing()) {
        progress.dismiss();
          }

      task = null;
    }
    }

    @Override
  public void onTextChanged(CharSequence s, int start, int before, int count) {
      int length = s.length();
        trkCode.setInputType(DEFAULT_INPUT_TYPES | (length < 2 || length > 10 ? InputType.TYPE_CLASS_TEXT : InputType.TYPE_CLASS_NUMBER));
    }

  @Override
  public void afterTextChanged(Editable s) {}

  @Override
  public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
}
