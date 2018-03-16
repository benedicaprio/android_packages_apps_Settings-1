/*
 * Copyright (C) 2018 The Android Open Source Project
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

package com.android.settings.fuelgauge;

import static com.android.settings.SettingsActivity.EXTRA_SHOW_FRAGMENT;
import static com.android.settings.SettingsActivity.EXTRA_SHOW_FRAGMENT_TITLE_RESID;
import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.support.v7.preference.Preference;

import com.android.settings.R;
import com.android.settings.SettingsActivity;
import com.android.settings.core.InstrumentedPreferenceFragment;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.List;

@RunWith(RobolectricTestRunner.class)
public class RestrictAppPreferenceControllerTest {

    @Mock
    private AppOpsManager mAppOpsManager;
    @Mock
    private AppOpsManager.PackageOps mPackageOps;
    @Mock
    private SettingsActivity mSettingsActivity;
    @Mock
    private InstrumentedPreferenceFragment mFragment;
    private List<AppOpsManager.PackageOps> mPackageOpsList;
    private RestrictAppPreferenceController mRestrictAppPreferenceController;
    private Preference mPreference;
    private Context mContext;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        mContext = spy(RuntimeEnvironment.application);
        doReturn(mAppOpsManager).when(mContext).getSystemService(Context.APP_OPS_SERVICE);
        doReturn(mContext).when(mSettingsActivity).getApplicationContext();
        mRestrictAppPreferenceController =
            new RestrictAppPreferenceController(mSettingsActivity, mFragment);
        mPackageOpsList = new ArrayList<>();
        mPreference = new Preference(mContext);
        mPreference.setKey(mRestrictAppPreferenceController.getPreferenceKey());
    }

    @Test
    public void testUpdateState_oneApp_showCorrectSummary() {
        mPackageOpsList.add(mPackageOps);
        doReturn(mPackageOpsList).when(mAppOpsManager).getPackagesForOps(any());

        mRestrictAppPreferenceController.updateState(mPreference);

        assertThat(mPreference.getSummary()).isEqualTo("1 app");
    }

    @Test
    public void testUpdateState_twoApps_showCorrectSummary() {
        mPackageOpsList.add(mPackageOps);
        mPackageOpsList.add(mPackageOps);
        doReturn(mPackageOpsList).when(mAppOpsManager).getPackagesForOps(any());

        mRestrictAppPreferenceController.updateState(mPreference);

        assertThat(mPreference.getSummary()).isEqualTo("2 apps");
    }

    @Test
    public void testUpdateState_zeroApp_disabled() {
        doReturn(mPackageOpsList).when(mAppOpsManager).getPackagesForOps(any());

        mRestrictAppPreferenceController.updateState(mPreference);

        assertThat(mPreference.isEnabled()).isFalse();
    }

    @Test
    public void testHandlePreferenceTreeClick_startFragment() {
        final ArgumentCaptor<Intent> intent = ArgumentCaptor.forClass(Intent.class);

        mRestrictAppPreferenceController.handlePreferenceTreeClick(mPreference);

        verify(mSettingsActivity).startActivity(intent.capture());
        assertThat(intent.getValue().getStringExtra(EXTRA_SHOW_FRAGMENT))
                .isEqualTo(RestrictedAppDetails.class.getName());
        assertThat(intent.getValue().getIntExtra(EXTRA_SHOW_FRAGMENT_TITLE_RESID, -1))
                .isEqualTo(R.string.restricted_app_title);
    }
}
