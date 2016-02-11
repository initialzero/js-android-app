package com.jaspersoft.android.jaspermobile.internal.di.components;

import com.jaspersoft.android.jaspermobile.activities.favorites.FavoritesPageFragment;
import com.jaspersoft.android.jaspermobile.activities.favorites.fragment.FavoritesFragment;
import com.jaspersoft.android.jaspermobile.activities.file.FileLoadFragment;
import com.jaspersoft.android.jaspermobile.activities.file.FileViewerActivity;
import com.jaspersoft.android.jaspermobile.activities.info.fragments.ReportInfoFragment;
import com.jaspersoft.android.jaspermobile.activities.info.fragments.ResourceInfoFragment;
import com.jaspersoft.android.jaspermobile.activities.info.fragments.SimpleInfoFragment;
import com.jaspersoft.android.jaspermobile.activities.library.LibraryPageFragment;
import com.jaspersoft.android.jaspermobile.activities.library.fragment.LibraryFragment;
import com.jaspersoft.android.jaspermobile.activities.recent.RecentPageFragment;
import com.jaspersoft.android.jaspermobile.activities.recent.fragment.RecentFragment;
import com.jaspersoft.android.jaspermobile.activities.repository.RepositoryPageFragment;
import com.jaspersoft.android.jaspermobile.activities.repository.fragment.RepositoryFragment;
import com.jaspersoft.android.jaspermobile.activities.save.SaveReportService;
import com.jaspersoft.android.jaspermobile.activities.save.fragment.SaveItemFragment;
import com.jaspersoft.android.jaspermobile.activities.schedule.JobsFragment;
import com.jaspersoft.android.jaspermobile.activities.schedule.ScheduleActivity;
import com.jaspersoft.android.jaspermobile.activities.settings.fragment.SettingsFragment;
import com.jaspersoft.android.jaspermobile.activities.storage.SavedReportsFragment;
import com.jaspersoft.android.jaspermobile.activities.storage.fragment.SavedItemsFragment;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.WebViewFragment;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.report.ReportCastActivity;
import com.jaspersoft.android.jaspermobile.dialog.ReportOptionsFragmentDialog;
import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.internal.di.PerProfile;
import com.jaspersoft.android.jaspermobile.internal.di.modules.ProfileModule;
import com.jaspersoft.android.jaspermobile.internal.di.modules.activity.ActivityModule;
import com.jaspersoft.android.jaspermobile.internal.di.modules.activity.DashboardModule;
import com.jaspersoft.android.jaspermobile.internal.di.modules.activity.NavigationActivityModule;
import com.jaspersoft.android.jaspermobile.internal.di.modules.activity.ReportModule;
import com.jaspersoft.android.jaspermobile.internal.di.modules.activity.ReportRestViewerModule;
import com.jaspersoft.android.jaspermobile.internal.di.modules.activity.ReportVisualizeViewerModule;
import com.jaspersoft.android.jaspermobile.util.ControllerFragment;
import com.jaspersoft.android.jaspermobile.util.ResourceOpener;
import com.jaspersoft.android.jaspermobile.util.cast.ResourcePresentationService;
import com.jaspersoft.android.jaspermobile.util.filtering.FavoritesResourceFilter;
import com.jaspersoft.android.jaspermobile.util.filtering.LibraryResourceFilter;
import com.jaspersoft.android.jaspermobile.util.filtering.RecentlyViewedResourceFilter;
import com.jaspersoft.android.jaspermobile.util.filtering.RepositoryResourceFilter;
import com.jaspersoft.android.jaspermobile.util.resource.viewbinder.FileResourceBinder;

import dagger.Subcomponent;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@PerProfile
@Subcomponent(
        modules = ProfileModule.class
)
public interface ProfileComponent {
    ReportRestViewerComponent plusReportRestViewer(ActivityModule activityModule,
                                                   ReportRestViewerModule reportModule);

    ReportVisualizeViewerComponent plusReportVisualizeViewer(ActivityModule activityModule,
                                                             ReportVisualizeViewerModule webViewModule);

    ControlsActivityComponent plusControlsPage(ActivityModule activityModule,
                                               ReportModule reportModule);

    DashboardActivityComponent plusDashboardPage(ActivityModule activityModule,
                                                DashboardModule dashboardModule);

    NavigationActivityComponent plusNavigationPage(NavigationActivityModule module);

    BaseActivityComponent plusBase(ActivityModule activityModule);

    Profile getProfile();
    /**
     * TODO remove one after architecture will be revised
     * Hardcoded injections.
     */
    void inject(SaveItemFragment saveItemFragment);
    void inject(LibraryFragment libraryFragment);
    void inject(RepositoryFragment repositoryFragment);
    void inject(ReportOptionsFragmentDialog reportOptionsFragmentDialog);
    void inject(SimpleInfoFragment simpleInfoFragment);
    void inject(ResourceInfoFragment resourceInfoFragment);
    void inject(ReportInfoFragment reportInfoFragment);
    void inject(ScheduleActivity scheduleActivity);
    void inject(JobsFragment jobsFragment);
    void inject(FileLoadFragment fileLoadFragment);
    void inject(FileResourceBinder fileResourceBinder);
    void inject(FileViewerActivity fileViewerActivity);
    void inject(FavoritesFragment favoritesFragment);
    void inject(RecentFragment recentFragment);
    void inject(SavedItemsFragment savedItemsFragment);
    void inject(SaveReportService saveReportService);
    void inject(ReportCastActivity reportCastActivity);
    void inject(ResourcePresentationService resourcePresentationService);
    void inject(SettingsFragment settingsFragment);
    void inject(ControllerFragment controllerFragment);
    void inject(LibraryPageFragment libraryPageFragment);
    void inject(RepositoryPageFragment repositoryPageFragment);
    void inject(RecentPageFragment recentPageFragment);
    void inject(SavedReportsFragment savedReportsFragment);
    void inject(FavoritesPageFragment favoritesPageFragment);
    void inject(WebViewFragment webViewFragment);
    void inject(FavoritesResourceFilter filter);
    void inject(LibraryResourceFilter filter);
    void inject(RecentlyViewedResourceFilter filter);
    void inject(RepositoryResourceFilter filter);
    void inject(ResourceOpener resourceOpener);
}
