package com.wdh.itemtouchdemo.ui;

import android.content.DialogInterface;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wdh.itemtouchdemo.ConstanceValue;
import com.wdh.itemtouchdemo.R;
import com.wdh.itemtouchdemo.SharedPreferencesMgr;
import com.wdh.itemtouchdemo.bean.Channel;
import com.wdh.itemtouchdemo.listener.OnChannelListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnChannelListener {
    private String TITLE_SELECTED = "explore_title_selected";
    private String TITLE_UNSELECTED = "explore_title_unselected";
    private ViewPager mViewPager;
    private TabLayout tabLayout;
    private ImageView mIv,iconCategory;
    private List<Fragment> mFragments;
    public List<Channel> mSelectedDatas = new ArrayList<>();
    public List<Channel> mUnSelectedDatas = new ArrayList<>();
    private Gson mGson = new Gson();
     private  ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferencesMgr.init(this, "itemdemo");
        initData();
        initView();
        onClick();
    }

    private void onClick() {
        mIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChannelDialogFragment dialogFragment = ChannelDialogFragment.newInstance(mSelectedDatas, mUnSelectedDatas);
                dialogFragment.setOnChannelListener(MainActivity.this);
                dialogFragment.show(getSupportFragmentManager(), "CHANNEL");
                dialogFragment.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        adapter.notifyDataSetChanged();
                        mViewPager.setOffscreenPageLimit(mSelectedDatas.size());
                        //tab.setCurrentItem(tab.getSelectedTabPosition());

                        ViewGroup slidingTabStrip = (ViewGroup) tabLayout.getChildAt(0);
                        //注意：因为最开始设置了最小宽度，所以重新测量宽度的时候一定要先将最小宽度设置为0
                        slidingTabStrip.setMinimumWidth(0);
                        slidingTabStrip.measure(0, 0);
                        slidingTabStrip.setMinimumWidth(slidingTabStrip.getMeasuredWidth() + iconCategory.getMeasuredWidth());

                        //保存选中和未选中的channel
                        SharedPreferencesMgr.setString(ConstanceValue.TITLE_SELECTED, mGson.toJson(mSelectedDatas));
                        SharedPreferencesMgr.setString(ConstanceValue.TITLE_UNSELECTED, mGson.toJson(mUnSelectedDatas));

                    }
                });
            }
        });
    }

    private void initData() {
        String selectTitle = SharedPreferencesMgr.getString(TITLE_SELECTED, "");
        String unselectTitle = SharedPreferencesMgr.getString(TITLE_UNSELECTED, "");
        if (TextUtils.isEmpty(selectTitle) || TextUtils.isEmpty(unselectTitle)) {
            String[] titleStr = getResources().getStringArray(R.array.category_name);
            String[] titlesCode = getResources().getStringArray(R.array.category_type);
            //默认添加了全部频道
            for (int i = 0; i < titlesCode.length; i++) {
                String t = titleStr[i];
                String code = titlesCode[i];
                mSelectedDatas.add(new Channel(t, code));
            }
            String selectedStr = mGson.toJson(mSelectedDatas);
            SharedPreferencesMgr.setString(TITLE_SELECTED, selectedStr);
        } else {
            //之前添加过
            List<Channel> selecteData = mGson.fromJson(selectTitle, new TypeToken<List<Channel>>() {
            }.getType());
            List<Channel> unselecteData = mGson.fromJson(unselectTitle, new TypeToken<List<Channel>>() {
            }.getType());
            mSelectedDatas.addAll(selecteData);
            mUnSelectedDatas.addAll(unselecteData);
        }

    }

    private void initView() {
        mViewPager = (ViewPager) findViewById(R.id.viewpage);
        tabLayout = (TabLayout) findViewById(R.id.tablayout);
        mIv = (ImageView) findViewById(R.id.add_btn);
        iconCategory= (ImageView) findViewById(R.id.new_category_tip);

        mFragments = new ArrayList<>();
        for (int i = 0; i < mSelectedDatas.size(); i++) {
            mFragments.add(DemoFragment.newInstance(mSelectedDatas.get(i).Title, mSelectedDatas.get(i).TitleCode));
        }
        mViewPager.setAdapter(adapter);
        mViewPager.setOffscreenPageLimit(mFragments.size());
        tabLayout.setupWithViewPager(mViewPager);/*使用ViewPager启动TabLayout*/
        /*一旦TabLayout的位置发生改变就会添加一个TabLayout上的页面选择监听器*/
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));
        /*无论页面发生变化或则反卷都会添加一个页面变化监听器*/
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
    }

    @Override
    public void onItemMove(int starPos, int endPos) {
        listMove(mSelectedDatas, starPos, endPos);
        listMove(mFragments, starPos, endPos);
    }
    private void listMove(List datas, int starPos, int endPos) {
        Object o = datas.get(starPos);
        //先删除之前的位置
        datas.remove(starPos);
        //添加到现在的位置
        datas.add(endPos, o);
    }
    @Override
    public void onMoveToMyChannel(int starPos, int endPos) {
        //移动到我的频道
        Channel channel = mUnSelectedDatas.remove(starPos);
        mSelectedDatas.add(endPos, channel);
        mFragments.add(DemoFragment.newInstance(channel.Title,channel.TitleCode));
    }

    @Override
    public void onMoveToOtherChannel(int starPos, int endPos) {
        //移动到推荐频道
        mUnSelectedDatas.add(endPos, mSelectedDatas.remove(starPos));
        mFragments.remove(starPos);
    }

    public class ViewPagerAdapter extends FragmentStatePagerAdapter {

        ViewPagerAdapter(FragmentManager fm) {  //传值
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return mFragments == null ? 0 : mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mSelectedDatas == null ? "" : mSelectedDatas.get(position).Title;
        }
    }

}
