package com.jaspersoft.android.jaspermobile.widget;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.jaspersoft.android.jaspermobile.R;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

import timber.log.Timber;

/**
 * @author Andrew Tivodar
 * @since 2.0
 */
@EViewGroup(R.layout.view_navigation_panel)
public class NavigationPanelLayout extends RelativeLayout {

    private final String TAG = NavigationPanelLayout.class.getName();

    private NavigationListener mListener;
    private View selectedItemView;
    boolean isShowingMenu;

    @ViewById(R.id.vg_navigation_menu)
    ViewGroup navigationMenu;

    @ViewById(R.id.lv_accounts_menu)
    ListView accountsMenu;

    public NavigationPanelLayout(Context context) {
        super(context);
    }

    public NavigationPanelLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NavigationPanelLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setListener(NavigationListener listener) {
        this.mListener = listener;
    }

    @AfterViews()
    final void initNavigationLayout() {
        isShowingMenu = true;
        Timber.tag(TAG);
    }

    @Click(R.id.vg_profile)
    final void profilePanelCkicked() {
        isShowingMenu = !isShowingMenu;
        showActivatedPanel(isShowingMenu);
    }

    @Click({R.id.vg_library, R.id.vg_repository, R.id.vg_favorites, R.id.vg_saved_items, R.id.vg_settings})
    final void navigationMenuItemSelect(View newSelectItem) {
        setItemSelected(newSelectItem, true);
        if (selectedItemView != null) {
            if (selectedItemView == newSelectItem) {
                return;
            }
            setItemSelected(selectedItemView, false);
        }

        selectedItemView = newSelectItem;

        if (mListener != null) {
            mListener.onNavigate(newSelectItem.getId());
        }
    }

    private void showActivatedPanel(boolean isShowingMenu) {
        if (!isShowingMenu) {
            navigationMenu.setVisibility(GONE);
            accountsMenu.setVisibility(VISIBLE);
        } else {
            navigationMenu.setVisibility(VISIBLE);
            accountsMenu.setVisibility(GONE);
        }
    }

    private void setItemSelected(View item, boolean selected) {
        item.setSelected(selected);
        try {
            ViewGroup itemGroup = ((ViewGroup) item);

            for (int i = 0; i < itemGroup.getChildCount(); i++) {
                View view = itemGroup.getChildAt(i);
                view.setSelected(selected);
            }
        } catch (ClassCastException e) {
            Timber.w(TAG, "Selected navigation item is not a layout.");
        }
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("instanceState", super.onSaveInstanceState());
        bundle.putBoolean("isShowingMenu", isShowingMenu);
        bundle.putInt("selectedViewId", selectedItemView.getId());
        return bundle;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            isShowingMenu = bundle.getBoolean("isShowingMenu");
            selectedItemView = findViewById(bundle.getInt("selectedViewId"));
            state = bundle.getParcelable("instanceState");

            showActivatedPanel(isShowingMenu);
            setItemSelected(selectedItemView, true);
        }
        super.onRestoreInstanceState(state);
    }

    public interface NavigationListener {
        public void onNavigate(int viewId);
        public void onProfileChange();
    }

}
