package com.jaspersoft.android.jaspermobile.widget;

import android.accounts.Account;
import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.retrofit.sdk.account.AccountManagerUtil;
import com.jaspersoft.android.retrofit.sdk.account.AccountServerData;
import com.jaspersoft.android.retrofit.sdk.account.BasicAccountProvider;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.ViewById;

import java.util.List;

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

    @ViewById(R.id.tv_profile)
    TextView tvProfile;

    @ViewById(R.id.iv_profile_arrow)
    ImageView ivProfileArrow;

    //---------------------------------------------------------------------
    // Public methods
    //---------------------------------------------------------------------

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

    public void setItemSelected(int viewId) {
        navigationMenuItemSelect(findViewById(viewId));
    }

    @AfterViews()
    final void initNavigationLayout() {
        isShowingMenu = true;
        AccountsAdapter accountsAdapter = new AccountsAdapter(getContext());
        View accountsFooter = LayoutInflater.from(getContext()).inflate(R.layout.view_accounts_footer, null, false);
        accountsFooter.findViewById(R.id.vg_add_account).setOnClickListener(onAddProfileClickListener);
        accountsFooter.findViewById(R.id.vg_manage_accounts).setOnClickListener(onManageProfileClickListener);
        accountsMenu.addFooterView(accountsFooter);
        accountsMenu.setAdapter(accountsAdapter);
        Account currentAccount = BasicAccountProvider.get(getContext()).getAccount();
        tvProfile.setText(currentAccount != null ? currentAccount.name : "Select Account");
        Timber.tag(TAG);
    }

    private OnClickListener onAddProfileClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mListener != null) mListener.onNavigate(R.id.vg_add_account);
        }
    };

    private OnClickListener onManageProfileClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mListener != null) mListener.onNavigate(R.id.vg_manage_accounts);
        }
    };

    @Click(R.id.vg_profile)
    final void profilePanelClicked() {
        isShowingMenu = !isShowingMenu;
        showActivatedPanel(isShowingMenu);
    }

    @Click({R.id.tv_settings, R.id.tv_profile, R.id.tv_about})
    final void navigationSubItemClick(View view) {
        if (mListener != null) {
            mListener.onNavigate(view.getId());
        }
    }

    @Click({R.id.vg_library, R.id.vg_repository, R.id.vg_favorites, R.id.vg_saved_items})
    final void navigationMenuItemSelect(View newSelectItem) {
        setItemSelected(newSelectItem, true);
        if (selectedItemView != null) {
            if (selectedItemView == newSelectItem) {
                if (mListener != null) {
                    mListener.onNavigate(0);
                }
                return;
            }
            setItemSelected(selectedItemView, false);
        }

        selectedItemView = newSelectItem;

        if (mListener != null) {
            mListener.onNavigate(newSelectItem.getId());
        }
    }

    @ItemClick(R.id.lv_accounts_menu)
    public void onAccountSelect(AccountServerData accountsData) {
        Account[] accounts = AccountManagerUtil.get(getContext()).getAccounts();
        for (Account account : accounts) {
            if (accountsData.getAlias().equals(account.name))
                if (mListener != null) mListener.onProfileChange(account);
        }
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        NavigationPanelSavedState savedState = new NavigationPanelSavedState(superState);
        savedState.isShowingMenu = this.isShowingMenu;
        savedState.selectedViewId = selectedItemView != null ? selectedItemView.getId() : -1;

        return savedState;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof NavigationPanelSavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }

        NavigationPanelSavedState savedState = (NavigationPanelSavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());

        this.isShowingMenu = savedState.isShowingMenu;
        showActivatedPanel(isShowingMenu);

        if (savedState.selectedViewId != -1) {
            this.selectedItemView = findViewById(savedState.selectedViewId);
            setItemSelected(selectedItemView, true);
        }
    }

    //---------------------------------------------------------------------
    // Helper methods
    //---------------------------------------------------------------------

    private void showActivatedPanel(boolean isShowingMenu) {
        if (!isShowingMenu) {
            ivProfileArrow.setImageResource(R.drawable.ic_arrow_up);
            navigationMenu.setVisibility(GONE);
            accountsMenu.setVisibility(VISIBLE);
        } else {
            ivProfileArrow.setImageResource(R.drawable.ic_arrow_down);
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

    //---------------------------------------------------------------------
    // Nested classes
    //---------------------------------------------------------------------

    private class AccountsAdapter extends BaseAdapter {

        private List<AccountServerData> mJasperAccounts;
        LayoutInflater mInflater;

        private AccountsAdapter(Context context) {
            mJasperAccounts = AccountManagerUtil.get(context).getAccountServers(true);
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return mJasperAccounts.size();
        }

        @Override
        public AccountServerData getItem(int position) {
            return mJasperAccounts.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            AccountsViewHolder mViewHolder;

            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.item_account, null);
                mViewHolder = new AccountsViewHolder();
                mViewHolder.tvAccountName = (TextView) convertView.findViewById(R.id.tv_account_name);
                mViewHolder.tvAccountVersion = (TextView) convertView.findViewById(R.id.tv_account_version);
                convertView.setTag(mViewHolder);
            } else {
                mViewHolder = (AccountsViewHolder) convertView.getTag();
            }

            mViewHolder.tvAccountName.setText(getItem(position).getAlias());

            AccountServerData serverData = getItem(position);
            String userName = serverData.getUsername();
            mViewHolder.tvAccountVersion.setText(userName == null ? "?" : userName.substring(0, 1));

            return convertView;
        }

        private class AccountsViewHolder {
            TextView tvAccountName;
            TextView tvAccountVersion;
        }
    }

    static class NavigationPanelSavedState extends BaseSavedState {
        int selectedViewId;
        boolean isShowingMenu;

        NavigationPanelSavedState(Parcelable superState) {
            super(superState);
        }

        private NavigationPanelSavedState(Parcel in) {
            super(in);
            selectedViewId = in.readInt();
            isShowingMenu = in.readInt() != 0;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(selectedViewId);
            out.writeInt(isShowingMenu ? 1 : 0);
        }

        public static final Parcelable.Creator<NavigationPanelSavedState> CREATOR =
                new Parcelable.Creator<NavigationPanelSavedState>() {
                    public NavigationPanelSavedState createFromParcel(Parcel in) {
                        return new NavigationPanelSavedState(in);
                    }

                    public NavigationPanelSavedState[] newArray(int size) {
                        return new NavigationPanelSavedState[size];
                    }
                };
    }

    public interface NavigationListener {
        /**
         * @param viewId returns selected view Id or 0 for same view
         */
        public void onNavigate(int viewId);

        public void onProfileChange(Account account);
    }

}
