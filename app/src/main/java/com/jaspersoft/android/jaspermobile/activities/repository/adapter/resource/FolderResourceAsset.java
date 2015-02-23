package com.jaspersoft.android.jaspermobile.activities.repository.adapter.resource;

import com.jaspersoft.android.jaspermobile.R;

/**
 * @author Tom Koptel
 * @since 2.0
 */
class FolderResourceAsset implements ResourceAsset {
    @Override
    public int getResourceIcon() {
        return R.drawable.sample_repo_blue;
    }

    @Override
    public int getResourceBackground() {
        return R.color.dashboard_item_bg;
    }
}
