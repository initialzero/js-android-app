package com.jaspersoft.android.jaspermobile.activities.intro.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.jaspersoft.android.jaspermobile.R;

/**
 * Created by AndrewTivodar on 06.11.2014.
 */
public class IntroPagerAdapter extends PagerAdapter {

    private final int pageCount = 4;
    private Context mContext;

    public IntroPagerAdapter(Context context) {
        mContext = context;
    }

    @Override
    public int getCount() {
        return pageCount;
    }

    @Override
    public Object instantiateItem(ViewGroup collection, int position) {
        View page;
        switch (position) {
            case 0:
                page = LayoutInflater.from(mContext).inflate(R.layout.intro_logo_page, null);
                break;
            case 1:
                page = LayoutInflater.from(mContext).inflate(R.layout.intro_info_page, null);
                ((TextView) page.findViewById(R.id.tvInfo_iip)).setText(mContext.getResources().getString(R.string.ip_second_text));
                ((ImageView) page.findViewById(R.id.ivInfo_iip)).setImageResource(R.drawable.im_intro_first);
                break;
            case 2:
                page = LayoutInflater.from(mContext).inflate(R.layout.intro_info_page, null);
                ((TextView) page.findViewById(R.id.tvInfo_iip)).setText(mContext.getResources().getString(R.string.ip_third_text));
                ((ImageView) page.findViewById(R.id.ivInfo_iip)).setImageResource(R.drawable.im_intro_second);
                break;
            case 3:
                page = LayoutInflater.from(mContext).inflate(R.layout.intro_info_page, null);
                ((TextView) page.findViewById(R.id.tvInfo_iip)).setText(mContext.getResources().getString(R.string.ip_fourth_text));
                ((ImageView) page.findViewById(R.id.ivInfo_iip)).setImageResource(R.drawable.im_intro_third);
                break;
            default:
                page = LayoutInflater.from(mContext).inflate(R.layout.intro_logo_page, null);
        }
        collection.addView(page);
        return page;
    }

    @Override
    public void destroyItem(ViewGroup collection, int position, Object view) {
         collection.removeView((View) view);
    }

    @Override
    public boolean isViewFromObject(View view, Object o) {
        return view == o;
    }
}

