package com.rbardini.carteiro.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import com.rbardini.carteiro.R;

import androidx.fragment.app.Fragment;

public class SROFragment extends Fragment {
  public static final String TAG = "SROFragment";

  private static final String SRO_URL = "https://www2.correios.com.br/sistemas/rastreamento/newprint.cfm";

  interface OnStateChangeListener {
    void onProgress(int progress);
    void onLeave();
  }

  private OnStateChangeListener listener;
  private WebView mWebView;

  public static SROFragment newInstance(String cod) {
    SROFragment f = new SROFragment();
    Bundle args = new Bundle();
    args.putString("cod", cod);
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
      throw new ClassCastException(activity.toString() + " must implement OnStateChangeListener");
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

    String postData = "objetos=" + getArguments().getString("cod");
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
