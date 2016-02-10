package com.jaspersoft.android.jaspermobile.presentation.view.fragment;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import com.jaspersoft.android.jaspermobile.internal.di.components.ProfileComponent;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public abstract class BaseFragment extends Fragment implements ComponentProvider {
    @SuppressWarnings("unchecked")
    @NonNull
    public <C> C getComponent(Class<C> componentType) {
        return ComponentProviderDelegate.INSTANCE.getComponent(getActivity(), componentType);
    }

    @NonNull
    public ProfileComponent getProfileComponent() {
        return ComponentProviderDelegate.INSTANCE.getProfileComponent(getActivity());
    }
}