package com.sismics.docs.activity;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.androidquery.AQuery;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.sismics.docs.R;
import com.sismics.docs.listener.CallbackListener;
import com.sismics.docs.model.application.ApplicationContext;
import com.sismics.docs.resource.UserResource;
import com.sismics.docs.ui.form.Validator;
import com.sismics.docs.ui.form.validator.Required;
import com.sismics.docs.util.DialogUtil;
import com.sismics.docs.util.PreferenceUtil;

import org.apache.http.Header;
import org.json.JSONObject;

/**
 * Login activity.
 * 
 * @author bgamard
 */
public class LoginActivity extends ActionBarActivity {

    /**
     * User interface.
     */
    private View loginForm;
    private View progressBar;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        
        AQuery aq = new AQuery(this);
        aq.id(R.id.loginExplain)
            .text(Html.fromHtml(getString(R.string.login_explain)))
            .getTextView()
            .setMovementMethod(LinkMovementMethod.getInstance());
        
        final EditText txtServer = aq.id(R.id.txtServer).getEditText();
        final EditText txtUsername = aq.id(R.id.txtUsername).getEditText();
        final EditText txtPassword = aq.id(R.id.txtPassword).getEditText();
        final Button btnConnect = aq.id(R.id.btnConnect).getButton();
        loginForm = aq.id(R.id.loginForm).getView();
        progressBar = aq.id(R.id.progressBar).getView();
        
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        
        loginForm.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        
        // Form validation
        final Validator validator = new Validator(false);
        validator.addValidable(this, txtServer, new Required());
        validator.addValidable(this, txtUsername, new Required());
        validator.addValidable(this, txtPassword, new Required());
        validator.setOnValidationChanged(new CallbackListener() {
            @Override
            public void onComplete() {
                btnConnect.setEnabled(validator.isValidated());
            }
        });

        // Preset saved server URL
        String serverUrl = PreferenceUtil.getStringPreference(this, PreferenceUtil.PREF_SERVER_URL);
        if (serverUrl != null) {
            txtServer.setText(serverUrl);
        }
        
        tryConnect();
        
        // Login button
        btnConnect.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                loginForm.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                
                PreferenceUtil.setServerUrl(LoginActivity.this, txtServer.getText().toString());
                
                try {
                    UserResource.login(getApplicationContext(), txtUsername.getText().toString(), txtPassword.getText().toString(), new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject json) {
                            // Empty previous user caches
                            PreferenceUtil.resetUserCache(getApplicationContext());

                            // Getting user info and redirecting to main activity
                            ApplicationContext.getInstance().fetchUserInfo(LoginActivity.this, new CallbackListener() {
                                @Override
                                public void onComplete() {
                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            });
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                            loginForm.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.GONE);

                            if (responseString != null && responseString.contains("\"ForbiddenError\"")) {
                                DialogUtil.showOkDialog(LoginActivity.this, R.string.login_fail_title, R.string.login_fail);
                            } else {
                                DialogUtil.showOkDialog(LoginActivity.this, R.string.network_error_title, R.string.network_error);
                            }
                        }
                    });
                } catch (IllegalArgumentException e) {
                    // Given URL is not valid
                    loginForm.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                    PreferenceUtil.setServerUrl(LoginActivity.this, null);
                    DialogUtil.showOkDialog(LoginActivity.this, R.string.invalid_url_title, R.string.invalid_url);
                }
            }
        });
    }

    /**
     * Try to get a "session".
     */
    private void tryConnect() {
        String serverUrl = PreferenceUtil.getStringPreference(this, PreferenceUtil.PREF_SERVER_URL);
        if (serverUrl == null) {
            // Server URL is empty
            loginForm.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
            return;
        }
        
        if (ApplicationContext.getInstance().isLoggedIn()) {
            // If we are already connected (from cache data)
            // redirecting to main activity
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            // Trying to get user data
            UserResource.info(getApplicationContext(), new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, final JSONObject json) {
                    if (json.optBoolean("anonymous", true)) {
                        loginForm.setVisibility(View.VISIBLE);
                        return;
                    }
                    
                    // Save user data in application context
                    ApplicationContext.getInstance().setUserInfo(getApplicationContext(), json);
                    
                    // Redirecting to main activity
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
                
                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    DialogUtil.showOkDialog(LoginActivity.this, R.string.network_error_title, R.string.network_error);
                    loginForm.setVisibility(View.VISIBLE);
                }
                
                @Override
                public void onFinish() {
                    progressBar.setVisibility(View.GONE);
                }
            });
        }
    }
}