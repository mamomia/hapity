package com.app.hopity.signupwizard;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.text.Html;
import android.util.Log;

import com.android.volley.Request;
import com.app.hopity.DashboardActivity;
import com.app.hopity.R;
import com.app.hopity.appdata.Constants;
import com.app.hopity.appdata.GlobalSharedPrefs;
import com.app.hopity.appdata.SessionManager;
import com.app.hopity.modelsResponse.ErrorModel;
import com.app.hopity.modelsResponse.RegisterResponseModel;
import com.cuneytayyildiz.simplegcm.SimpleGcm;

import net.lateralview.simplerestclienthandler.RestClientManager;
import net.lateralview.simplerestclienthandler.base.RequestCallbacks;
import net.lateralview.simplerestclienthandler.base.RequestHandler;

import org.codepond.wizardroid.WizardFlow;
import org.codepond.wizardroid.layouts.BasicWizardLayout;
import org.codepond.wizardroid.persistence.ContextVariable;

import java.util.HashMap;

import cn.pedant.SweetAlert.SweetAlertDialog;
import ua.com.crosp.solutions.library.prettytoast.PrettyToast;

/**
 * Created by Mushi on 3/7/2016.
 */
public class SignupWizard extends BasicWizardLayout {

    private final String htmlText = "<h1>Copyright (c) 2014-2025 Hapity. Inc</h1>\n" +
            "\n" +
            "<p>\n" +
            "*** END USER LICENSE AGREEMENT ***  \n" +
            "</p>\n" +
            "\n" +
            "<p>\n" +
            "IMPORTANT: PLEASE READ THIS LICENSE CAREFULLY BEFORE USING THIS SOFTWARE.\n" +
            "</p>\n" +
            "\n" +
            "<h2>1. LICENSE</h2>\n" +
            "\n" +
            "<p>\n" +
            "By receiving, opening the file package, and/or using Hapity 3.+(\"Software\") containing this software, you agree that this End User User License Agreement(EULA) is a legally binding and valid contract and agree to be bound by it. You agree to abide by the intellectual property laws and all of the terms and conditions of this Agreement.\n" +
            "</p>\n" +
            "\n" +
            "<p>\n" +
            "Unless you have a different license agreement signed by Hapity. Inc your use of Hapity 3.+ indicates your acceptance of this license agreement and warranty.\n" +
            "</p>\n" +
            "\n" +
            "<p>\n" +
            "Subject to the terms of this Agreement, Hapity. Inc grants to you a limited, non-exclusive, non-transferable license, without right to sub-license, to use Hapity 3.+ in accordance with this Agreement and any other written agreement with Hapity. Inc. Hapity. Inc does not transfer the title of Hapity 3.+ to you; the license granted to you is not a sale. This agreement is a binding legal agreement between Hapity. Inc and the purchasers or users of Hapity 3.+.\n" +
            "</p>\n" +
            "\n" +
            "<p>\n" +
            "If you do not agree to be bound by this agreement, remove Hapity 3.+ from your computer now and, if applicable, promptly return to Hapity. Inc by mail any copies of Hapity 3.+ and related documentation and packaging in your possession. \n" +
            "</p>\n" +
            " \n" +
            "<h2>2. DISTRIBUTION</h2>\n" +
            "\n" +
            "<p>\n" +
            "Hapity 3.+ and the license herein granted shall not be copied, shared, distributed, re-sold, offered for re-sale, transferred or sub-licensed in whole or in part except that you may make one copy for archive purposes only. For information about redistribution of Hapity 3.+ contact Hapity. Inc.\n" +
            "</p>\n" +
            " \n" +
            "<h2>3. USER AGREEMENT</h2>\n" +
            "\n" +
            "<h3>3.1 Use</h3>\n" +
            "\n" +
            "<p>\n" +
            "Your license to use Hapity 3.+ is limited to the number of licenses purchased by you. You shall not allow others to use, copy or evaluate copies of Hapity 3.+.\n" +
            "</p>\n" +
            "\n" +
            "<h3>3.2 Use Restrictions</h3>\n" +
            "\n" +
            "<p>\n" +
            "You shall use Hapity 3.+ in compliance with all applicable laws and not for any unlawful purpose. Without limiting the foregoing, use, display or distribution of Hapity 3.+ together with material that is pornographic, racist, vulgar, obscene, defamatory, libelous, abusive, promoting hatred, discriminating or displaying prejudice based on religion, ethnic heritage, race, sexual orientation or age is strictly prohibited.\n" +
            "</p>\n" +
            "\n" +
            "<p>\n" +
            "Each licensed copy of Hapity 3.+ may be used on one single computer location by one user. Use of Hapity 3.+ means that you have loaded, installed, or run Hapity 3.+ on a computer or similar device. If you install Hapity 3.+ onto a multi-user platform, server or network, each and every individual user of Hapity 3.+ must be licensed separately.\n" +
            "</p>\n" +
            "\n" +
            "<p>\n" +
            "You may make one copy of Hapity 3.+ for backup purposes, providing you only have one copy installed on one computer being used by one person. Other users may not use your copy of Hapity 3.+ . The assignment, sublicense, networking, sale, or distribution of copies of Hapity 3.+ are strictly forbidden without the prior written consent of Hapity. Inc. It is a violation of this agreement to assign, sell, share, loan, rent, lease, borrow, network or transfer the use of Hapity 3.+. If any person other than yourself uses Hapity 3.+ registered in your name, regardless of whether it is at the same time or different times, then this agreement is being violated and you are responsible for that violation!  \n" +
            "</p>\n" +
            "\n" +
            "<h3>3.3 Copyright Restriction</h3>\n" +
            "\n" +
            "<p>\n" +
            "This Software contains copyrighted material, trade secrets and other proprietary material. You shall not, and shall not attempt to, modify, reverse engineer, disassemble or decompile Hapity 3.+. Nor can you create any derivative works or other works that are based upon or derived from Hapity 3.+ in whole or in part.\n" +
            "</p>\n" +
            "  \n" +
            "</p>\n" +
            "Hapity. Inc's name, logo and graphics file that represents Hapity 3.+ shall not be used in any way to promote products developed with Hapity 3.+ . Hapity. Inc retains sole and exclusive ownership of all right, title and interest in and to Hapity 3.+ and all Intellectual Property rights relating thereto.\n" +
            "</p>\n" +
            "\n" +
            "</p>\n" +
            "Copyright law and international copyright treaty provisions protect all parts of Hapity 3.+, products and services. No program, code, part, image, audio sample, or text may be copied or used in any way by the user except as intended within the bounds of the single user program. All rights not expressly granted hereunder are reserved for Hapity. Inc. \n" +
            "</p>\n" +
            "\n" +
            "<h3>3.4 Limitation of Responsibility</h3>\n" +
            "\n" +
            "<p>\n" +
            "You will indemnify, hold harmless, and defend Hapity. Inc , its employees, agents and distributors against any and all claims, proceedings, demand and costs resulting from or in any way connected with your use of Hapity. Inc's Software.\n" +
            "</p>\n" +
            "\n" +
            "<p>\n" +
            "In no event (including, without limitation, in the event of negligence) will Hapity. Inc , its employees, agents or distributors be liable for any consequential, incidental, indirect, special or punitive damages whatsoever (including, without limitation, damages for loss of profits, loss of use, business interruption, loss of information or data, or pecuniary loss), in connection with or arising out of or related to this Agreement, Hapity 3.+ or the use or inability to use Hapity 3.+ or the furnishing, performance or use of any other matters hereunder whether based upon contract, tort or any other theory including negligence.\n" +
            "</p>\n" +
            "\n" +
            "<p> \n" +
            "Hapity. Inc's entire liability, without exception, is limited to the customers' reimbursement of the purchase price of the Software (maximum being the lesser of the amount paid by you and the suggested retail price as listed by Hapity. Inc ) in exchange for the return of the product, all copies, registration papers and manuals, and all materials that constitute a transfer of license from the customer back to Hapity. Inc.\n" +
            "</p>\n" +
            "  \n" +
            "<h3>3.5 Warranties</h3>\n" +
            "\n" +
            "<p>\n" +
            "Except as expressly stated in writing, Hapity. Inc makes no representation or warranties in respect of this Software and expressly excludes all other warranties, expressed or implied, oral or written, including, without limitation, any implied warranties of merchantable quality or fitness for a particular purpose.\n" +
            "</p>\n" +
            "\n" +
            "<h3>3.6 Governing Law</h3>\n" +
            "\n" +
            "<p>\n" +
            "This Agreement shall be governed by the law of the Pakistan applicable therein. You hereby irrevocably attorn and submit to the non-exclusive jurisdiction of the courts of Pakistan therefrom. If any provision shall be considered unlawful, void or otherwise unenforceable, then that provision shall be deemed severable from this License and not affect the validity and enforceability of any other provisions.\n" +
            "</p>\n" +
            "\n" +
            "<h3>3.7 Termination</h3>\n" +
            "\n" +
            "<p>\n" +
            "Any failure to comply with the terms and conditions of this Agreement will result in automatic and immediate termination of this license. Upon termination of this license granted herein for any reason, you agree to immediately cease use of Hapity 3.+ and destroy all copies of Hapity 3.+ supplied under this Agreement. The financial obligations incurred by you shall survive the expiration or termination of this license.  \n" +
            "</p>\n" +
            "\n" +
            "<h2>4. DISCLAIMER OF WARRANTY</h2>\n" +
            "\n" +
            "<p>\n" +
            "THIS SOFTWARE AND THE ACCOMPANYING FILES ARE SOLD \"AS IS\" AND WITHOUT WARRANTIES AS TO PERFORMANCE OR MERCHANTABILITY OR ANY OTHER WARRANTIES WHETHER EXPRESSED OR IMPLIED. THIS DISCLAIMER CONCERNS ALL FILES GENERATED AND EDITED BY Hapity 3.+ AS WELL.\n" +
            "</p>\n" +
            "\n" +
            "<h2>5. CONSENT OF USE OF DATA</h2>\n" +
            "\n" +
            "<p>\n" +
            "You agree that Hapity. Inc may collect and use information gathered in any manner as part of the product support services provided to you, if any, related to Hapity 3.+.Hapity. Inc may also use this information to provide notices to you which may be of use or interest to you.\n" +
            "</p>";
    @ContextVariable
    private String profile_picture = "";
    @ContextVariable
    private boolean uploadProfilePictureFlag = false;
    @ContextVariable
    private String username = "";
    @ContextVariable
    private String email = "";
    @ContextVariable
    private String password = "";
    private String TAG = "Sign up wizard";
    private SessionManager session;

    public SignupWizard() {
        super();
    }

    @Override
    public WizardFlow onSetup() {
        session = new SessionManager(getActivity());
        return new WizardFlow.Builder()
                .addStep(SignupStep1.class)
                .addStep(SignupStep2.class)
                .addStep(SignupStep3.class)
                .addStep(SignupStep4.class)
                .create();
    }

    @Override
    public void onWizardComplete() {
        super.onWizardComplete();

        // show the eula license agreement
        String title = getString(R.string.app_name) + " v 2.+";
        //Includes the updates as well so users know what changed.
        String message = getString(R.string.updatesEula) + "\n\n" + Html.fromHtml(htmlText);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("I Accept", new Dialog.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Mark this version as read.
                        if (!username.isEmpty() && !email.isEmpty() && !password.isEmpty()) {
                            validateSimpleUserSignUp task = new validateSimpleUserSignUp();
                            task.execute(username, email, password);
                        } else {
                            PrettyToast.showError(getActivity(), "We cant process your request, please check you have entered all information correct.");
                        }
                        dialogInterface.dismiss();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new Dialog.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Close the activity as they have declined the EULA
                        dialog.dismiss();
                    }
                });
        builder.create().show();
    }

    private class validateSimpleUserSignUp extends AsyncTask<String, Void, String> {
        private SweetAlertDialog progressSignUp;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressSignUp = new SweetAlertDialog(getActivity(), SweetAlertDialog.PROGRESS_TYPE);
            progressSignUp.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
            progressSignUp.setTitleText("Signing Up, Please wait..");
            progressSignUp.setCancelable(false);
            progressSignUp.show();
        }//close onPreExecute

        @Override
        protected String doInBackground(String... params) {

            String registrationId = "";
            // Check if Google Play Service is installed in Device
            // Play services is needed to handle GCM stuffs
            if (SimpleGcm.isRegistered(getContext())) {
                // Register Device in GCM Server
                registrationId = SimpleGcm.getRegistrationId(getContext());
                Log.e(TAG, "reg id : " + registrationId);
            }
            HashMap<String, String> paramsHash = new HashMap<>();
            paramsHash.put("username", params[0]);
            paramsHash.put("email", params[1]);
            paramsHash.put("password", params[2]);
            if (uploadProfilePictureFlag)
                paramsHash.put("upload_picture", profile_picture);
            else
                paramsHash.put("profile_picture", "");

            paramsHash.put("reg_id", registrationId);
            paramsHash.put("type", "android");
            final String finalRegistrationId = registrationId;
            RestClientManager.getInstance().makeJsonRequest(Request.Method.POST,
                    Constants.SIGN_UP_URL,
                    new RequestHandler<>(new RequestCallbacks<RegisterResponseModel, ErrorModel>() {
                        @Override
                        public void onRequestSuccess(RegisterResponseModel response) {
                            try {
                                progressSignUp.dismiss();
                                String status = response.status;
                                if (status.equalsIgnoreCase("success")) {

                                    GlobalSharedPrefs.hapityPref.edit().putString("type", "simple").apply();
                                    GlobalSharedPrefs.hapityPref.edit().putInt("userid", response.user_info[0].user_id).apply();
                                    GlobalSharedPrefs.hapityPref.edit().putString("USER_EMAIL", response.user_info[0].email).apply();
                                    GlobalSharedPrefs.hapityPref.edit().putString("USER_NAME", response.user_info[0].username).apply();
                                    GlobalSharedPrefs.hapityPref.edit().putString("USER_PROFILE_PICTURE", response.user_info[0].profile_picture).apply();
                                    GlobalSharedPrefs.hapityPref.edit().putString("appToken", response.user_info[0].token).apply();
                                    GlobalSharedPrefs.hapityPref.edit().putString(Constants.GCM_REG_KEY, finalRegistrationId).apply();

                                    // creating a session and fire up dashboard
                                    session.createLoginSession(response.user_info[0].username, response.user_info[0].email,
                                            response.user_info[0].token);
                                    session.storeGcmRegId(finalRegistrationId);

                                    Intent i = new Intent(getActivity().getApplicationContext(), DashboardActivity.class);
                                    // Closing all the Activities
                                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    // Add new Flag to start new Activity
                                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(i);
                                    getActivity().finish();

                                } else {
                                    PrettyToast.showError(getActivity().getApplicationContext(), response.error + ", Please try again");
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                PrettyToast.showError(getActivity().getApplicationContext(), "Something went wrong, Please try again");
                            }
                        }

                        @Override
                        public void onRequestError(ErrorModel error) {
                            if (getActivity().getApplicationContext() != null) {
                                progressSignUp.dismiss();
                                PrettyToast.showError(getActivity().getApplicationContext(), "There was a problem connecting to server, Please try again");
                            }
                        }

                    }, paramsHash));

            return "";
        }//close doInBackground
    }// close validateUserTask
}
