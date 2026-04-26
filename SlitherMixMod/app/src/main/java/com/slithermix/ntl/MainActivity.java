package com.slithermix.ntl;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.webkit.WebViewAssetLoader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class MainActivity extends Activity {

    private WebView webView;
    private WebViewAssetLoader assetLoader;
    private JoystickView joystick;
    private BoostButton boostButton;

    // Current skin state
    private int skinHue1 = 0;
    private int skinHue2 = 180;
    private int skinPattern = 0;
    private String skinAccessory = "none";

    @SuppressLint({"SetJavaScriptEnabled", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Fullscreen immersive
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Hide system UI
        View decor = getWindow().getDecorView();
        decor.setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );

        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webview);
        joystick = findViewById(R.id.joystick);
        boostButton = findViewById(R.id.boost_btn);

        setupWebView();
        setupControls();
        setupButtons();

        // Load slither.io
        webView.loadUrl("https://slither.io");
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {
        // Setup asset loader to serve local assets at appassets domain
        assetLoader = new WebViewAssetLoader.Builder()
            .setDomain("appassets.androidplatform.net")
            .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(this))
            .build();

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setAllowFileAccess(true);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setUserAgentString(
            "Mozilla/5.0 (Linux; Android 11; Mobile) AppleWebKit/537.36 " +
            "(KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36 SlitherMixMod/1.0"
        );

        // Hardware acceleration
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        webView.setBackgroundColor(Color.BLACK);

        // JavaScript bridge for skin control
        webView.addJavascriptInterface(new SlitherBridge(), "SlitherMixAndroid");

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int progress) {
                // Could show loading indicator here
            }
        });

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view,
                    WebResourceRequest request) {
                // Serve local assets via asset loader
                WebResourceResponse response = assetLoader.shouldInterceptRequest(request.getUrl());
                if (response != null) return response;
                return super.shouldInterceptRequest(view, request);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (url.contains("slither.io") || url.contains("slither.com")) {
                    injectMobileScript(view);
                }
            }
        });
    }

    private void injectMobileScript(WebView view) {
        // Load our injection script from assets and inject it
        try {
            InputStream is = getAssets().open("mobile_inject.js");
            byte[] buf = new byte[is.available()];
            is.read(buf);
            is.close();
            String script = new String(buf, StandardCharsets.UTF_8);
            // Wrap in IIFE to avoid polluting global scope
            String wrapped = "(function(){" + script + "})();";
            view.evaluateJavascript(wrapped, null);
        } catch (IOException e) {
            e.printStackTrace();
            // Fallback: inject minimal bridge
            view.evaluateJavascript(
                "console.log('[SlitherMixMod] Asset injection failed: " + e.getMessage() + "');",
                null
            );
        }
    }

    private void setupControls() {
        // Connect joystick to WebView
        joystick.setListener(new JoystickView.JoystickListener() {
            @Override
            public void onJoystickMoved(float normX, float normY, boolean active) {
                if (!active) {
                    // Release - send to center
                    String js = String.format(
                        "if(window.AndroidBridge) window.AndroidBridge.onJoystickInput(0, 0, false);",
                        normX, normY
                    );
                    webView.post(() -> webView.evaluateJavascript(js, null));
                } else {
                    // Scale joystick to canvas position
                    String js = String.format(
                        "try{" +
                        "var c=document.getElementById('mc')||document.querySelector('canvas');" +
                        "if(c){" +
                        "var r=c.getBoundingClientRect();" +
                        "var cx=r.left+r.width/2, cy=r.top+r.height/2;" +
                        "var tx=cx+(%f*300), ty=cy+(%f*300);" +
                        "c.dispatchEvent(new MouseEvent('mousemove',{bubbles:true,clientX:tx,clientY:ty}));" +
                        "}}catch(e){}",
                        normX, normY
                    );
                    webView.post(() -> webView.evaluateJavascript(js, null));
                }
            }
        });

        // Connect boost button to WebView
        boostButton.setBoostListener(new BoostButton.BoostListener() {
            @Override
            public void onBoost(boolean pressed) {
                String type = pressed ? "mousedown" : "mouseup";
                String js = String.format(
                    "try{" +
                    "var c=document.getElementById('mc')||document.querySelector('canvas');" +
                    "if(c){" +
                    "var r=c.getBoundingClientRect();" +
                    "var cx=r.left+r.width/2, cy=r.top+r.height/2;" +
                    "c.dispatchEvent(new MouseEvent('%s',{bubbles:true,button:0,buttons:%d,clientX:cx,clientY:cy}));" +
                    "}}catch(e){}",
                    type, pressed ? 1 : 0
                );
                webView.post(() -> webView.evaluateJavascript(js, null));
            }
        });
    }

    private void setupButtons() {
        ImageView skinBtn = findViewById(R.id.skin_btn);
        ImageView settingsBtn = findViewById(R.id.settings_btn);

        skinBtn.setOnClickListener(v -> showSkinDialog());
        settingsBtn.setOnClickListener(v -> showSettingsDialog());
    }

    private void showSkinDialog() {
        // Show skin customizer
        SkinDialog dialog = new SkinDialog(this, skinHue1, skinHue2, skinPattern, skinAccessory);
        dialog.setOnSkinAppliedListener((hue1, hue2, pattern, accessory) -> {
            skinHue1 = hue1;
            skinHue2 = hue2;
            skinPattern = pattern;
            skinAccessory = accessory;
            applySkinToGame();
        });
        dialog.show();
    }

    private void applySkinToGame() {
        String js = String.format(
            "if(window.AndroidBridge){" +
            "window.AndroidBridge.onSkinSelected(%d,%d,%d,'%s');" +
            "}else if(window.SlitherMixSkin){" +
            "window.SlitherMixSkin.setColors(%d,%d);" +
            "window.SlitherMixSkin.setPattern(%d);" +
            "window.SlitherMixSkin.setAccessory('%s');" +
            "}",
            skinHue1, skinHue2, skinPattern, skinAccessory,
            skinHue1, skinHue2, skinPattern, skinAccessory
        );
        webView.evaluateJavascript(js, null);
    }

    private void showSettingsDialog() {
        new AlertDialog.Builder(this)
            .setTitle("SlitherMixMod Settings")
            .setItems(new String[]{
                "🔄 Reload Game",
                "📺 Toggle Controls",
                "🎮 NTL Mod Panel",
                "ℹ️ About"
            }, (dialog, which) -> {
                switch (which) {
                    case 0:
                        webView.reload();
                        break;
                    case 1:
                        toggleControls();
                        break;
                    case 2:
                        openNTLPanel();
                        break;
                    case 3:
                        showAbout();
                        break;
                }
            })
            .show();
    }

    private boolean controlsVisible = true;
    private void toggleControls() {
        controlsVisible = !controlsVisible;
        joystick.setVisibility(controlsVisible ? View.VISIBLE : View.GONE);
        boostButton.setVisibility(controlsVisible ? View.VISIBLE : View.GONE);
        Toast.makeText(this,
            controlsVisible ? "Controls shown" : "Controls hidden",
            Toast.LENGTH_SHORT).show();
    }

    private void openNTLPanel() {
        // Try to open NTL mod panel via JS
        webView.evaluateJavascript(
            "try{" +
            "var btn=document.getElementById('ntl_btn')||document.querySelector('[id*=\"ntl\"]');" +
            "if(btn)btn.click();" +
            "}catch(e){}",
            null
        );
    }

    private void showAbout() {
        new AlertDialog.Builder(this)
            .setTitle("SlitherMixMod v1.0")
            .setMessage(
                "🐍 Slither.io × NTL Mod × Mobile Controls\n\n" +
                "• NTL Mod v9.18 by [NTL] Nothing To Lose\n" +
                "• Mobile arrow/joystick controls\n" +
                "• Custom skin system\n" +
                "• Inspired by mobile slither SWF\n\n" +
                "Play like a pro on mobile!"
            )
            .setPositiveButton("OK", null)
            .show();
    }

    // JavaScript → Android bridge
    public class SlitherBridge {
        @JavascriptInterface
        public void onModLoaded() {
            runOnUiThread(() ->
                Toast.makeText(MainActivity.this,
                    "NTL Mod loaded! 🐍",
                    Toast.LENGTH_SHORT).show()
            );
        }

        @JavascriptInterface
        public void onScore(int score) {
            // Could update a score overlay here
        }

        @JavascriptInterface
        public void log(String msg) {
            android.util.Log.d("SlitherMixMod", msg);
        }
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        webView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        webView.onResume();
        // Re-apply immersive mode
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );
    }
}
