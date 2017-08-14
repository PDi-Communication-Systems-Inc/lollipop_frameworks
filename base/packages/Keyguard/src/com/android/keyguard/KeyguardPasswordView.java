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
import android.graphics.Rect;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.TextKeyListener;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.content.Intent;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.InputMethodSubtype;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import java.io.IOException;


import java.util.List;
/**
 * Displays an alphanumeric (latin-1) key entry for the user to enter
 * an unlock password
 */

public class KeyguardPasswordView extends KeyguardAbsKeyInputView
        implements KeyguardSecurityView, OnEditorActionListener, TextWatcher {

    private final boolean mShowImeAtScreenOn;
    private final int mDisappearYTranslation;

    InputMethodManager mImm;
    private TextView mPasswordEntry;
    private Interpolator mLinearOutSlowInInterpolator;
    private Interpolator mFastOutLinearInInterpolator;
    private static final String TAG="LockPatternKeyguardPasswordView";


    public KeyguardPasswordView(Context context) {
        this(context, null);
    }

    public KeyguardPasswordView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mShowImeAtScreenOn = context.getResources().
                getBoolean(R.bool.kg_show_ime_at_screen_on);
        mDisappearYTranslation = getResources().getDimensionPixelSize(
                R.dimen.disappear_y_translation);
        mLinearOutSlowInInterpolator = AnimationUtils.loadInterpolator(
                context, android.R.interpolator.linear_out_slow_in);
        mFastOutLinearInInterpolator = AnimationUtils.loadInterpolator(
                context, android.R.interpolator.fast_out_linear_in);
    }

    protected void resetState() {
        mSecurityMessageDisplay.setMessage(R.string.kg_password_instructions, false);
        mPasswordEntry.setEnabled(true);
    }

    @Override
    protected int getPasswordTextViewId() {
        return R.id.passwordEntry;
    }

    @Override
    public boolean needsInput() {
        return true;
    }

    @Override
    public void onResume(final int reason) {
        super.onResume(reason);

        // Wait a bit to focus the field so the focusable flag on the window is already set then.
        post(new Runnable() {
            @Override
            public void run() {
                mPasswordEntry.requestFocus();
                if (reason != KeyguardSecurityView.SCREEN_ON || mShowImeAtScreenOn) {
                // hide the keyboard and don't show it implicitly. Bug 644
                // mImm.showSoftInput(mPasswordEntry, InputMethodManager.SHOW_IMPLICIT);
                }
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        mImm.hideSoftInputFromWindow(getWindowToken(), 0);
    }

    @Override
    public void reset() {
        super.reset();
        mPasswordEntry.requestFocus();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        boolean imeOrDeleteButtonVisible = false;

        mImm = (InputMethodManager) getContext().getSystemService(
                Context.INPUT_METHOD_SERVICE);


        mPasswordEntry = (TextView) findViewById(getPasswordTextViewId());
        mImm.hideSoftInputFromWindow(mPasswordEntry.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        mPasswordEntry.setKeyListener(TextKeyListener.getInstance());
        mPasswordEntry.setInputType(InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        mPasswordEntry.setOnEditorActionListener(this);
        mPasswordEntry.addTextChangedListener(this);

        // Poke the wakelock any time the text is selected or modified
        mPasswordEntry.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mCallback.userActivity();
            }
        });

        // Set selected property on so the view can send accessibility events.
        mPasswordEntry.setSelected(true);

        mPasswordEntry.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void afterTextChanged(Editable s) {
                if (mCallback != null) {
                    mCallback.userActivity();
                }
            }
        });

        mPasswordEntry.requestFocus();

        // If there's more than one IME, enable the IME switcher button
        View switchImeButton = findViewById(R.id.switch_ime_button);
        if (switchImeButton != null && hasMultipleEnabledIMEsOrSubtypes(mImm, false)) {
            switchImeButton.setVisibility(View.VISIBLE);
            imeOrDeleteButtonVisible = true;
            switchImeButton.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    mCallback.userActivity(); // Leave the screen on a bit longer
                    mImm.showInputMethodPicker();
                }
            });
        }

        // If no icon is visible, reset the start margin on the password field so the text is
        // still centered.
        if (!imeOrDeleteButtonVisible) {
            android.view.ViewGroup.LayoutParams params = mPasswordEntry.getLayoutParams();
            if (params instanceof MarginLayoutParams) {
                final MarginLayoutParams mlp = (MarginLayoutParams) params;
                mlp.setMarginStart(0);
                mPasswordEntry.setLayoutParams(params);
            }
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
                                Intent intent = getContext().getPackageManager().getLaunchIntentForPackage("com.pdiarm.newuserconfirmation");
                                if(intent != null)
                                     {
                                   intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                   getContext().startActivity(intent);
                                 }

                }
            });
        }


    }

    @Override
    protected boolean onRequestFocusInDescendants(int direction, Rect previouslyFocusedRect) {
        // send focus to the password field
        return mPasswordEntry.requestFocus(direction, previouslyFocusedRect);
    }

    @Override
    protected void resetPasswordText(boolean animate) {
        mPasswordEntry.setText("");
    }

    @Override
    protected String getPasswordText() {
        return mPasswordEntry.getText().toString();
    }

    @Override
    protected void setPasswordEntryEnabled(boolean enabled) {
        mPasswordEntry.setEnabled(enabled);
    }

    /**
     * Method adapted from com.android.inputmethod.latin.Utils
     *
     * @param imm The input method manager
     * @param shouldIncludeAuxiliarySubtypes
     * @return true if we have multiple IMEs to choose from
     */
    private boolean hasMultipleEnabledIMEsOrSubtypes(InputMethodManager imm,
            final boolean shouldIncludeAuxiliarySubtypes) {
        final List<InputMethodInfo> enabledImis = imm.getEnabledInputMethodList();

        // Number of the filtered IMEs
        int filteredImisCount = 0;

        for (InputMethodInfo imi : enabledImis) {
            // We can return true immediately after we find two or more filtered IMEs.
            if (filteredImisCount > 1) return true;
            final List<InputMethodSubtype> subtypes =
                    imm.getEnabledInputMethodSubtypeList(imi, true);
            // IMEs that have no subtypes should be counted.
            if (subtypes.isEmpty()) {
                ++filteredImisCount;
                continue;
            }

            int auxCount = 0;
            for (InputMethodSubtype subtype : subtypes) {
                if (subtype.isAuxiliary()) {
                    ++auxCount;
                }
            }
            final int nonAuxCount = subtypes.size() - auxCount;

            // IMEs that have one or more non-auxiliary subtypes should be counted.
            // If shouldIncludeAuxiliarySubtypes is true, IMEs that have two or more auxiliary
            // subtypes should be counted as well.
            if (nonAuxCount > 0 || (shouldIncludeAuxiliarySubtypes && auxCount > 1)) {
                ++filteredImisCount;
                continue;
            }
        }

        return filteredImisCount > 1
        // imm.getEnabledInputMethodSubtypeList(null, false) will return the current IME's enabled
        // input method subtype (The current IME should be LatinIME.)
                || imm.getEnabledInputMethodSubtypeList(null, false).size() > 1;
    }

    @Override
    public void showUsabilityHint() {
    }

    @Override
    public int getWrongPasswordStringId() {
        return R.string.kg_wrong_password;
    }

    @Override
    public void startAppearAnimation() {
        setAlpha(0f);
        setTranslationY(0f);
        animate()
                .alpha(1)
                .withLayer()
                .setDuration(300)
                .setInterpolator(mLinearOutSlowInInterpolator);
    }

    @Override
    public boolean startDisappearAnimation(Runnable finishRunnable) {
        animate()
                .alpha(0f)
                .translationY(mDisappearYTranslation)
                .setInterpolator(mFastOutLinearInInterpolator)
                .setDuration(100)
                .withEndAction(finishRunnable);
        return true;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        if (mCallback != null) {
            mCallback.userActivity();
        }
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        // Check if this was the result of hitting the enter key
        final boolean isSoftImeEvent = event == null
                && (actionId == EditorInfo.IME_NULL
                || actionId == EditorInfo.IME_ACTION_DONE
                || actionId == EditorInfo.IME_ACTION_NEXT);
        final boolean isKeyboardEnterKey = event != null
                && KeyEvent.isConfirmKey(event.getKeyCode())
                && event.getAction() == KeyEvent.ACTION_DOWN;
        if (isSoftImeEvent || isKeyboardEnterKey) {
            verifyPasswordAndUnlock();
            return true;
        }
        return false;
    }
}
