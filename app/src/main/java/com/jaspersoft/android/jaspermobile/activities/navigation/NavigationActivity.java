package com.jaspersoft.android.jaspermobile.activities.navigation;

import android.accounts.Account;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.account.AccountsActivity_;
import com.jaspersoft.android.jaspermobile.activities.favorites.FavoritesPageFragment_;
import com.jaspersoft.android.jaspermobile.activities.repository.LibraryFragment_;
import com.jaspersoft.android.jaspermobile.activities.repository.RepositoryFragment_;
import com.jaspersoft.android.jaspermobile.activities.robospice.RoboToolboxActivity;
import com.jaspersoft.android.jaspermobile.activities.settings.SettingsActivity_;
import com.jaspersoft.android.jaspermobile.activities.storage.SavedReportsFragment_;
import com.jaspersoft.android.jaspermobile.widget.NavigationPanelLayout;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;

import java.util.List;

/**
 * @author Ivan Gadzhega
 * @author Tom Koptel
 * @author Andrew Tivodar
 * @since 1.0
 */
@EActivity(R.layout.activity_navigation)
public class NavigationActivity extends RoboToolboxActivity {
    private static final String DIRECT_TAG = "DIRECT_FRAGMENT";

    @ViewById(R.id.dl_navigation)
    DrawerLayout drawerLayout;
    @ViewById(R.id.npl_navigation_menu)
    NavigationPanelLayout navigationPanelLayout;

    @Extra
    protected int defaultSelection = R.id.vg_library;

    private ActionBarDrawerToggle mDrawerToggle;
    private Bundle mSavedInstanceState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSavedInstanceState = savedInstanceState;
    }

    @AfterViews
    final void init() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.app_label);
        }

        mDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, getToolbar(), R.string.nd_drawer_open, R.string.nd_drawer_close) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
            }
        };
        drawerLayout.setDrawerListener(mDrawerToggle);

        navigationPanelLayout.setListener(new NavigationPanelLayout.NavigationListener() {
            @Override
            public void onNavigate(int viewId) {
                handleNavigationAction(viewId);
                drawerLayout.closeDrawer(navigationPanelLayout);
            }

            @Override
            public void onProfileChange(Account account) {
            }
        });

        if (mSavedInstanceState == null) {
            handleNavigationAction(defaultSelection);
            navigationPanelLayout.setItemSelected(defaultSelection);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean drawerOpen = drawerLayout.isDrawerOpen(navigationPanelLayout);
        hideMenuItems(menu, !drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void handleNavigationAction(int viewId) {
        switch (viewId) {
            case R.id.vg_library:
                commitContent(LibraryFragment_.builder().build());
                break;
            case R.id.vg_repository:
                commitContent(RepositoryFragment_.builder().build());
                break;
            case R.id.vg_saved_items:
                commitContent(SavedReportsFragment_.builder().build());
                break;
            case R.id.vg_favorites:
                commitContent(FavoritesPageFragment_.builder().build());
                break;
            case R.id.vg_manage_accounts:
                AccountsActivity_.intent(this).start();
                break;
            case R.id.tv_settings:
                SettingsActivity_.intent(this).start();
                break;
            case R.id.tv_feedback:
                new FeedBackDialog().show(getSupportFragmentManager(), FeedBackDialog.class.getSimpleName());
                break;
            case R.id.tv_about:
                new AboutDialog().show(getSupportFragmentManager(), AboutDialog.class.getSimpleName());
        }
    }

    //---------------------------------------------------------------------
    // Helper methods
    //---------------------------------------------------------------------

    private void commitContent(@NonNull Fragment directFragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if (fragments != null) {
            for (Fragment fragment : fragments) {
                if (fragment != null) {
                    transaction.remove(fragment);
                }
            }
        }
        transaction
                .replace(R.id.main_frame, directFragment, DIRECT_TAG)
                .commit();
    }

    private void hideMenuItems(Menu menu, boolean visible) {
        for (int i = 0; i < menu.size(); i++) {
            menu.getItem(i).setVisible(visible);
        }
    }

    //---------------------------------------------------------------------
    // Inner classes
    //---------------------------------------------------------------------

    public static class AboutDialog extends DialogFragment {
        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.sa_show_about);
            builder.setMessage(R.string.sa_about_info);
            builder.setCancelable(true);
            builder.setNeutralButton(android.R.string.ok, null);

            Dialog dialog = builder.create();
            dialog.setCanceledOnTouchOutside(true);

            dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialogInterface) {
                    View decorView = getDialog().getWindow().getDecorView();
                    if (decorView != null) {
                        TextView messageText = (TextView) decorView.findViewById(android.R.id.message);
                        if (messageText != null) {
                            messageText.setMovementMethod(LinkMovementMethod.getInstance());
                        }
                    }
                }
            });
            return dialog;
        }
    }

    public static class FeedBackDialog extends DialogFragment {
        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.sa_show_feedback);
            builder.setMessage(R.string.sa_feedback_info);
            builder.setCancelable(true);
            builder.setNegativeButton(android.R.string.cancel, null);
            builder.setPositiveButton(android.R.string.ok,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Intent.ACTION_SEND);
                            intent.setType("message/rfc822");
                            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"js.testdevice@gmail.com"});
                            intent.putExtra(Intent.EXTRA_SUBJECT, "Feedback");
                            try {
                                PackageInfo pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
                                String versionName = pInfo.versionName;
                                String versionCode = String.valueOf(pInfo.versionCode);
                                intent.putExtra(Intent.EXTRA_TEXT, String.format("Version name: %s \nVersion code: %s", versionName, versionCode));
                            } catch (PackageManager.NameNotFoundException e) {
                            }
                            try {
                                getActivity().startActivity(intent);
                            } catch (ActivityNotFoundException e) {
                                Toast.makeText(getActivity(),
                                        getString(R.string.sdr_t_no_app_available, "email"),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
            );

            Dialog dialog = builder.create();
            dialog.setCanceledOnTouchOutside(true);

            return dialog;
        }
    }
}
