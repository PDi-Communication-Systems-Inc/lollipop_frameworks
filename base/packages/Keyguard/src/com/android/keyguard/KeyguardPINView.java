/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.keyguard;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.content.pm.PackageManager;
import java.io.IOException;
import android.util.Log;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import java.util.List;
import android.content.Intent;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.content.ComponentName;
import android.widget.TextView.*;
//import com.pdiarm.newuserconfirmation;
import android.widget.Toast;

/**
 * Displays a PIN pad for unlocking.
 */
public class KeyguardPINView extends KeyguardPinBasedInputView{

    private final AppearAnimationUtils mAppearAnimationUtils;
    private ViewGroup mKeyguardBouncerFrame;
    private ViewGroup mRow0;
    private ViewGroup mRow1;
    private ViewGroup mRow2;
    private ViewGroup mRow3;
    private View mDivider;
    private int mDisappearYTranslation;
    Context context;
    private static final String TAG="LockPatternKeyguardView"; 

    public KeyguardPINView(Context context) {
        this(context, null);
    }

    public KeyguardPINView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mAppearAnimationUtils = new AppearAnimationUtils(context);
        mDisappearYTranslation = getResources().getDimensionPixelSize(
                R.dimen.disappear_y_translation);
    }

    protected void resetState() {
        super.resetState();
        if (KeyguardUpdateMonitor.getInstance(mContext).getMaxBiometricUnlockAttemptsReached()) {
            mSecurityMessageDisplay.setMessage(R.string.faceunlock_multiple_failures, true);
        } else {
            mSecurityMessageDisplay.setMessage(R.string.kg_pin_instructions, false);
        }
    }

    @Override
    protected int getPasswordTextViewId() {
        return R.id.pinEntry;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mKeyguardBouncerFrame = (ViewGroup) findViewById(R.id.keyguard_bouncer_frame);
        mRow0 = (ViewGroup) findViewById(R.id.row0);
        mRow1 = (ViewGroup) findViewById(R.id.row1);
        mRow2 = (ViewGroup) findViewById(R.id.row2);
        mRow3 = (ViewGroup) findViewById(R.id.row3);
        mDivider = findViewById(R.id.divider);

        final View ok = findViewById(R.id.key_enter);
        if (ok != null) {
            ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    doHapticKeyClick();
                    if (mPasswordEntry.isEnabled()) {
                        verifyPasswordAndUnlock();
                    }
                }
            });
            ok.setOnHoverListener(new LiftToActivateListener(getContext()));
        }

        final View login_ok = findViewById(R.id.login_user);
        if (login_ok != null) {
            login_ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    doHapticKeyClick();
                    if (mPasswordEntry.isEnabled()) {
                        verifyPasswordAndUnlock();
                    }
                }
            });
            login_ok.setOnHoverListener(new LiftToActivateListener(getContext()));
        }

        View newUser = findViewById(R.id.im_a_new_user);
        if (newUser != null) {
            newUser.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                                //Error:In some cases the new user confirmation app does not come to the foreground
                                //Fix: kill the existing new user confirmation app and start a new one

                                //switch to admin user
                                try {
                                        Process p = null;
                                        p = Runtime.getRuntime().exec("/system/bin/am switch-user 0");
                                        p.waitFor();
                                        Thread.sleep(500);
                                        }
                                        catch (IOException e) {
                                        // TODO Code to run in input/output exception
                                        if ((e != null) && (e.getMessage() != null))
                                                Log.e(TAG, e.getMessage());
                                        Log.e(TAG, "IOException - the requested program could not be executed ");
                                        }
                                        catch (Exception e) {
                                        // TODO Code to run in input/output exception
                                                Log.e(TAG, "General Exception");
                                        }

                                ActivityManager manager =  (ActivityManager) getContext().getSystemService(Context.ACTIVITY_SERVICE);
                                List<RunningAppProcessInfo> activities = ((ActivityManager) manager).getRunningAppProcesses(); // returns null if no running processes

                                if(activities != null) {
                                        for (int i = 0; i < activities.size(); i++){
                                                Log.i(TAG, "Found App:" + activities.get(i).processName);

                                
                                 if (activities.get(i).processName.equals("com.pdiarm.newuserconfirmation")){          
                                                        Log.i(TAG, "App " + activities.get(i).processName + "found to be running, going to kill it");
                                                        android.os.Process.killProcess(activities.get(i).pid);
                                                        break;
                                                }
                                        }

                                        try {
                                                Thread.sleep(1000); //sleep for a second
                                        }
                                        catch (Exception e) {
                                                if((e != null) && (e.getMessage() != null))
                                                                Log.e(TAG,e.getMessage());
                                        }
                                }

                                //call the new user confirmation Activity
                               //changes mGangakhedkar                       
                          //     Intent intent = new Intent(this,"com.pdiarm.newuserconfirmation.NewUserConfirmationActivity");
                             /*   Intent intent = new Intent(Intent.ACTION_LAUNCH);
                                 intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                                | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

                              // i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);i
                               try{
                              getContext().startActivity(intent);
                               }

                               catch(android.content.ActivityNotFoundException e){

                                 Log.e("Keygyuard","Activity not found");                                
                              // Toast.makeText(getApplicationContext(),"NwUser Activity not found", Toast.LENGTH_LONG).show();
                               }  */

                               /*   Intent intent = new Intent(KeyguardPINView.this,NewUserConfirmationActivity.class);
                              // intent.setClassName("com.pdiarm.newuserconfirmation","com.pdiarm.newuserconfirmation.NewUserConfirmationActivity");
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                                | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                getContext().startActivity(intent); */
                                 
                                Intent intent = getContext().getPackageManager().getLaunchIntentForPackage("com.pdiarm.newuserconfirmation");
                                if(intent != null)
                                     {
                                   intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                   getContext().startActivity(intent);
                                 }

                }
            });
        }

        // The delete button is of the PIN keyboard itself in some (e.g. tablet) layouts,
        // not a separate view
        View pinDelete = findViewById(R.id.delete_button);
        if (pinDelete != null) {
            pinDelete.setVisibility(View.VISIBLE);
             pinDelete.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    // check for time-based lockouts
                    if (mPasswordEntry.isEnabled()) {
                      /*  CharSequence str = mPasswordEntry.getText();
                        if (str.length() > 0) {
                            mPasswordEntry.setText(str.subSequence(0,str.length()-1));
                           //  mPasswordEntry = str.subSequence(0,str.length()-1); */
                           mPasswordEntry.deleteLastChar();
                        }
                    
                    doHapticKeyClick();
               } 
            });
            pinDelete.setOnLongClickListener(new View.OnLongClickListener() {
                public boolean onLongClick(View v) {
                    // check for time-based lockouts
                    if (mPasswordEntry.isEnabled()) {
                       // mPasswordEntry.setText("");;
                       resetPasswordText(true /*animate */);
                    }
                    doHapticKeyClick();
                    return true;
                }
            });
       } 

      //  mPasswordEntry.setKeyListener(DigitsKeyListener.getInstance());
       // mPasswordEntry.setInputType(InputType.TYPE_CLASS_NUMBER
         //       | InputType.TYPE_NUMBER_VARIATION_PASSWORD);

        mPasswordEntry.requestFocus();
    }

    @Override
    public void showUsabilityHint() {
    }

    @Override
    public int getWrongPasswordStringId() {
        return R.string.kg_wrong_pin;
    }

    @Override
    public void startAppearAnimation() {
        enableClipping(false);
        setAlpha(1f);
        setTranslationY(mAppearAnimationUtils.getStartTranslation());
        animate()
                .setDuration(500)
                .setInterpolator(mAppearAnimationUtils.getInterpolator())
                .translationY(0);
        mAppearAnimationUtils.startAppearAnimation(new View[][] {
                new View[] {
                        mRow0, null, null
                },
                new View[] {
                        findViewById(R.id.key1), findViewById(R.id.key2), findViewById(R.id.key3)
                },
                new View[] {
                        findViewById(R.id.key4), findViewById(R.id.key5), findViewById(R.id.key6)
                },
                new View[] {
                        findViewById(R.id.key7), findViewById(R.id.key8), findViewById(R.id.key9)
                },
                new View[] {
                        null, findViewById(R.id.key0), findViewById(R.id.key_enter)
                },
                new View[] {
                        null, mEcaView, null
                }},
                new Runnable() {
                    @Override
                    public void run() {
                        enableClipping(true);
                    }
                });
    }

    @Override
    public boolean startDisappearAnimation(Runnable finishRunnable) {
        animate()
                .alpha(0f)
                .translationY(mDisappearYTranslation)
                .setInterpolator(AnimationUtils
                        .loadInterpolator(mContext, android.R.interpolator.fast_out_linear_in))
                .setDuration(100)
                .withEndAction(finishRunnable);
        return true;
    }

    private void enableClipping(boolean enable) {
        mKeyguardBouncerFrame.setClipToPadding(enable);
        mKeyguardBouncerFrame.setClipChildren(enable);
        mRow1.setClipToPadding(enable);
        mRow2.setClipToPadding(enable);
        mRow3.setClipToPadding(enable);
        setClipChildren(enable);
    }

    @Override
    public boolean hasOverlappingRendering() {
        return false;
    }
}
