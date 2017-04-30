package com.rbardini.carteiro.ui;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import com.rbardini.carteiro.R;
import com.rbardini.carteiro.util.PostalUtils;

public class WebSROFragment extends Fragment {
  public static final String TAG = "WebSROFragment";

  interface OnStateChangeListener {
    void onProgress(int progress);
    void onLeave();
  }

  private OnStateChangeListener listener;
  private WebView mWebView;

  public static WebSROFragment newInstance(String cod) {
    WebSROFragment f = new WebSROFragment();
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
    mWebView.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.websro));
    mWebView.loadUrl(String.format(PostalUtils.WEBSRO_URL, getArguments().getString("cod")));

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
