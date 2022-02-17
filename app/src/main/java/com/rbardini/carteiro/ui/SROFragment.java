package com.rbardini.carteiro.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.rbardini.carteiro.R;

import androidx.fragment.app.Fragment;

public class SROFragment extends Fragment {
  public static final String TAG = "SROFragment";

  private static final String COD_KEY = "cod";
  private static final String SRO_URL = "https://rastreamento.correios.com.br/app/index.php";

  interface OnStateChangeListener {
    void onProgress(int progress);
    void onLeave();
  }

  private OnStateChangeListener listener;
  private WebView mWebView;

  public static SROFragment newInstance(String cod) {
    SROFragment f = new SROFragment();
    Bundle args = new Bundle();
    args.putString(COD_KEY, cod);
    f.setArguments(args);

    return f;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Activity activity = getActivity();

    try {
      listener = (OnStateChangeListener) activity;
    } catch (ClassCastException e) {
      throw new ClassCastException(activity + " must implement OnStateChangeListener");
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.webview, container, false);

    mWebView = (WebView) view.findViewById(R.id.webview);
    mWebView.setWebChromeClient(new WebChromeClient() {
      @Override
      public void onProgressChanged(WebView view, int progress) {
        listener.onProgress(progress);
      }
    });

    WebSettings settings = mWebView.getSettings();
    settings.setDomStorageEnabled(true);
    settings.setJavaScriptEnabled(true);

    String postData = "objetos=" + getArguments().getString(COD_KEY);
    mWebView.postUrl(SRO_URL, postData.getBytes());

    return view;
  }

  @Override
  public void onPause() {
    super.onPause();
    listener.onLeave();
  }

  public boolean canGoBack() {
    return mWebView != null && mWebView.canGoBack();
  }

  public void goBack() {
    if (mWebView != null) mWebView.goBack();
  }
}
