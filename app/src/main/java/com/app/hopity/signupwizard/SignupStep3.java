package com.app.hopity.signupwizard;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.app.hopity.R;

import org.codepond.wizardroid.WizardStep;
import org.codepond.wizardroid.persistence.ContextVariable;

/**
 * Created by Mushi on 3/7/2016.
 */
public class SignupStep3 extends WizardStep {

    EditText etEmail;
    @ContextVariable
    private String email;
    private String TAG = "Step 3";

    //You must have an empty constructor for every step
    public SignupStep3() {
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (activity.getCurrentFocus() != null)
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }

    public void setupUI(View view) {
        //Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    hideSoftKeyboard(getActivity());
                    return false;
                }
            });
        }
        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupUI(innerView);
            }
        }
    }

    //Set your layout here
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.layout_signup_step3, container, false);

        etEmail = (EditText) v.findViewById(R.id.etEmailSignup);
        etEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.e(TAG, "afterTextChanged: String : " + s);
                if (!s.toString().contains("@") || s.toString().isEmpty())
                    etEmail.setError("Invalid Email");
                else
                    etEmail.setError(null);
            }
        });

        //and set default values by using Context Variables
        etEmail.setText(email);
        setupUI(v);
        return v;
    }

    @Override
    public void onExit(int exitCode) {
        switch (exitCode) {
            case WizardStep.EXIT_NEXT:
                bindDataFields();
                break;
            case WizardStep.EXIT_PREVIOUS:
                //Do nothing...
                break;
        }
    }

    private void bindDataFields() {
        //Do some work
        email = etEmail.getText().toString().trim();
    }

}
