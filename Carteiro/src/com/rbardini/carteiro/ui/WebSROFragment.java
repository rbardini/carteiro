package com.rbardini.carteiro.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.rbardini.carteiro.R;

public class WebSROFragment extends Fragment {
  public static final String TAG = "WebSROFragment";

  SherlockFragmentActivity mActivity;
  WebView mWebView;

  public static WebSROFragment newInstance(String cod) {
    WebSROFragment f = new WebSROFragment();
    Bundle args = new Bundle();
    args.putString("cod", cod);
    f.setArguments(args);

    return f;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.websro_webview, container, false);

    mActivity = (SherlockFragmentActivity) getActivity();
    mWebView = (WebView) view.findViewById(R.id.webview);

    mWebView.setWebChromeClient(new WebChromeClient() {
      @Override
      public void onProgressChanged(WebView view, int progress) {
        mActivity.setSupportProgressBarIndeterminateVisibility(progress != 100);
      }
    });
    mWebView.setBackgroundColor(getResources().getColor(R.color.websro));
    mWebView.loadUrl(getString(R.string.websro_url, getArguments().getString("cod")));

    return view;
  }

  @Override
  public void onPause() {
    super.onPause();

    mActivity.setSupportProgressBarIndeterminateVisibility(false);
  }

  public boolean canGoBack() {
    return mWebView != null && mWebView.canGoBack();
  }

  public void goBack() {
    if (mWebView != null) mWebView.goBack();
  }
}
