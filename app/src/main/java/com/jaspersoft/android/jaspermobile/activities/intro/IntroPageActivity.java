package com.jaspersoft.android.jaspermobile.activities.intro;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.Button;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.intro.adapter.IntroPagerAdapter;
import com.jaspersoft.android.jaspermobile.widget.PageIndicatorView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

/**
 * @author Andrew Tivodar
 * @since 1.9
 */
@EActivity(R.layout.intro_page_layout)
public class IntroPageActivity extends Activity implements ViewPager.OnPageChangeListener{

    @ViewById(R.id.btnSkip_AIP)
    Button btnSkip;

    @ViewById(R.id.btnNext_AIP)
    Button btnNext;

    @ViewById(R.id.divider_AIP)
    View divider;

    @ViewById(R.id.vpIntro_AIP)
    ViewPager vpIntro;

    @ViewById(R.id.indicator_AIP)
    PageIndicatorView indicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @AfterViews
    void initIntroViewPager() {
        vpIntro.setOffscreenPageLimit(4);
        vpIntro.setOnPageChangeListener(this);
        vpIntro.setAdapter(new IntroPagerAdapter(this));
    }

    @Click(R.id.btnSkip_AIP)
    void skipIntro() {
        finish();
    }

    @Click(R.id.btnNext_AIP)
    void nextIntroPage() {
        vpIntro.setCurrentItem(vpIntro.getCurrentItem() + 1);
    }

    @Override
    public void onPageScrolled(int i, float v, int i2) {
        indicator.setPagePosition(i, v);
    }

    @Override
    public void onPageSelected(int i) {
        if(i == 3) {
            btnNext.setVisibility(View.GONE);
            divider.setVisibility(View.GONE);
            btnSkip.setText(getString(R.string.ip_start_btn));
        }
        else {
            btnNext.setVisibility(View.VISIBLE);
            divider.setVisibility(View.VISIBLE);
            btnSkip.setText(getString(R.string.ip_skip_btn));
        }
    }

    @Override
    public void onPageScrollStateChanged(int i) {

    }
}
