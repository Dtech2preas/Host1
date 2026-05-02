package com.crunchy.autologin;

import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private LinearLayout settingsPanel;
    private EditText credentialsInput;
    private Button startLoginBtn;
    private FloatingActionButton fabNext, fabSettings;

    private List<String> credentialList = new ArrayList<>();
    private int currentIndex = 0;
    private boolean isAutomating = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webView);
        settingsPanel = findViewById(R.id.settingsPanel);
        credentialsInput = findViewById(R.id.credentialsInput);
        startLoginBtn = findViewById(R.id.startLoginBtn);
        fabNext = findViewById(R.id.fabNext);
        fabSettings = findViewById(R.id.fabSettings);

        setupWebView();

        startLoginBtn.setOnClickListener(v -> {
            String input = credentialsInput.getText().toString();
            if (input.isEmpty()) {
                Toast.makeText(this, "Please enter credentials", Toast.LENGTH_SHORT).show();
                return;
            }
            credentialList = Arrays.asList(input.split("\n"));
            currentIndex = 0;
            settingsPanel.setVisibility(View.GONE);
            startNextLogin();
        });

        fabSettings.setOnClickListener(v -> {
            settingsPanel.setVisibility(settingsPanel.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
        });

        fabNext.setOnClickListener(v -> {
            fabNext.setVisibility(View.GONE);
            currentIndex++;
            if (currentIndex < credentialList.size()) {
                clearAndLoginNext();
            } else {
                Toast.makeText(this, "No more credentials", Toast.LENGTH_SHORT).show();
                settingsPanel.setVisibility(View.VISIBLE);
            }
        });
    }

    private void setupWebView() {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setUserAgentString("Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36");

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (isAutomating && url.contains("sso.crunchyroll.com/login")) {
                    injectLoginScript();
                }

                if (url.contains("crunchyroll.com/discover")) {
                    Toast.makeText(MainActivity.this, "Login Successful!", Toast.LENGTH_LONG).show();
                    fabNext.setVisibility(View.VISIBLE);
                    isAutomating = false;
                }
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return false;
            }
        });
    }

    private void startNextLogin() {
        isAutomating = true;
        webView.loadUrl("https://sso.crunchyroll.com/login");
    }

    private void clearAndLoginNext() {
        CookieManager.getInstance().removeAllCookies(null);
        CookieManager.getInstance().flush();
        webView.clearHistory();
        webView.clearCache(true);
        startNextLogin();
    }

    private void injectLoginScript() {
        if (currentIndex >= credentialList.size()) return;

        String line = credentialList.get(currentIndex);
        int colonIndex = line.indexOf(':');
        if (colonIndex == -1) return;

        String email = line.substring(0, colonIndex).trim();
        String password = line.substring(colonIndex + 1).trim();

        String escapedEmail = escapeJsString(email);
        String escapedPassword = escapeJsString(password);

        String js = "(function() {" +
                "   var email = '" + escapedEmail + "';" +
                "   var password = '" + escapedPassword + "';" +
                "   var step = 0;" +
                "   function triggerInput(element, value) {" +
                "       element.focus();" +
                "       element.value = value;" +
                "       element.dispatchEvent(new Event('input', { bubbles: true }));" +
                "       element.dispatchEvent(new Event('change', { bubbles: true }));" +
                "       element.blur();" +
                "   }" +
                "   function findButtonByText(text) {" +
                "       var btns = document.querySelectorAll('button');" +
                "       for (var i = 0; i < btns.length; i++) {" +
                "           if (btns[i].innerText && btns[i].innerText.toLowerCase().includes(text.toLowerCase())) {" +
                "               return btns[i];" +
                "           }" +
                "       }" +
                "       return null;" +
                "   }" +
                "   function run() {" +
                "       try {" +
                "           var cookieBtn = document.querySelector('#onetrust-accept-btn-handler');" +
                "           if (cookieBtn && cookieBtn.offsetParent !== null) {" +
                "               cookieBtn.click();" +
                "           }" +
                "           var emailField = document.querySelector('input[name=\"username\"], input[name=\"email\"], input[type=\"email\"]');" +
                "           var passField = document.querySelector('input[name=\"password\"], input[type=\"password\"]');" +
                "           var btn = document.querySelector('button[type=\"submit\"], button.login-button') || findButtonByText('log in') || findButtonByText('sign in') || findButtonByText('login');" +
                "           if (emailField && passField && btn) {" +
                "               if (step === 0) {" +
                "                   emailField.click();" +
                "                   triggerInput(emailField, email);" +
                "                   step++;" +
                "                   setTimeout(run, 1000);" +
                "               } else if (step === 1) {" +
                "                   passField.click();" +
                "                   triggerInput(passField, password);" +
                "                   step++;" +
                "                   setTimeout(run, 1000);" +
                "               } else if (step === 2) {" +
                "                   btn.click();" +
                "               }" +
                "           } else {" +
                "               setTimeout(run, 1000);" +
                "           }" +
                "       } catch (e) {" +
                "           setTimeout(run, 1000);" +
                "       }" +
                "   }" +
                "   setTimeout(run, 1000);" +
                "})()";
        webView.evaluateJavascript(js, null);
    }

    private String escapeJsString(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\")
                    .replace("'", "\\'")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r");
    }
}
