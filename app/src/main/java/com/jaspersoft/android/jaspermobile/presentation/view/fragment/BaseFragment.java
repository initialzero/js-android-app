package com.jaspersoft.android.jaspermobile.presentation.view.fragment;

import android.os.Bundle;

import com.jaspersoft.android.jaspermobile.internal.di.HasComponent;

import roboguice.fragment.RoboFragment;

/**
 * @author Tom Koptel
 * @since 2.3
 */
abstract class BaseFragment extends RoboFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    /**
     * Gets a component for dependency injection by its type.
     */
    @SuppressWarnings("unchecked")
    protected <C> C getComponent(Class<C> componentType) {
        return componentType.cast(((HasComponent<C>) getActivity()).getComponent());
    }
}