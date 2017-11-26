package com.oozeetech.bizdesk.fragment.drawer.paymentoutstanding;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.oozeetech.bizdesk.BaseFragment;
import com.oozeetech.bizdesk.R;
import com.oozeetech.bizdesk.fragment.drawer.HomeFragment;
import com.oozeetech.bizdesk.ui.drawer.DrawerActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static com.oozeetech.bizdesk.utils.FontUtils.fontName;


public class PaymentOutstandingFragment extends BaseFragment {


    @BindView(R.id.tabs)
    TabLayout tabs;
    @BindView(R.id.viewpager)
    ViewPager viewpager;
    Unbinder unbinder;
    private SectionsPagerAdapter mSectionsPagerAdapter;


    public PaymentOutstandingFragment() {
        // Required empty public constructor
    }

    @Override
    public void onResume() {
        super.onResume();
        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View arg0, int code, KeyEvent arg2) {

                if (code == KeyEvent.KEYCODE_BACK) {
                    if (DrawerActivity.drawer.isDrawerOpen(GravityCompat.START)) {
                        DrawerActivity.drawer.closeDrawer(GravityCompat.START);
                    } else {
                        final FragmentTransaction ft = getFragmentManager().beginTransaction();
                        ft.replace(R.id.container_body, new HomeFragment());
                        DrawerActivity.txtTitle.setText(getString(R.string.nav_item_home));
                        Intent intent = new Intent(getResources().getString(R.string.drawer_item_selected));
                        intent.putExtra(getResources().getString(R.string.drawer_item_number), 0);
                        getActivity().sendBroadcast(intent);
                        ft.commit();

                    }
                }
                return true;
            }
        });
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getChildFragmentManager());
        viewpager.setAdapter(mSectionsPagerAdapter);
        viewpager.setOffscreenPageLimit(2);
        tabs.setupWithViewPager(viewpager);
        changeTabsFont();
    }

    private void changeTabsFont() {

        ViewGroup vg = (ViewGroup) tabs.getChildAt(0);
        int tabsCount = vg.getChildCount();
        for (int j = 0; j < tabsCount; j++) {
            ViewGroup vgTab = (ViewGroup) vg.getChildAt(j);
            int tabChildCount = vgTab.getChildCount();
            for (int i = 0; i < tabChildCount; i++) {
                View tabViewChild = vgTab.getChildAt(i);
                if (tabViewChild instanceof TextView) {
                    ((TextView) tabViewChild).setTypeface(fontName(getActivity(), 1));
                }
            }
        }
    }

    @Override
    protected int getFragmentLayout() {
        return R.layout.fragment_payment_outstanding;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // TODO: inflate a fragment view
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        unbinder = ButterKnife.bind(this, rootView);
//        setHasOptionsMenu(true);
        return rootView;
    }
/*

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        // TODO Add your menu entries here
        menu.clear();
        inflater.inflate(R.menu.home, menu);
        MenuItem menuItem = menu.findItem(R.id.action_filter);
        menuItem.setVisible(true);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent i;
        switch (item.getItemId()) {
            case R.id.action_filter:
                // Do Activity menu item stuff here
                i = new Intent(getActivity(), FilterActivity.class);
                startActivityForResult(i, FILTER_CODE);
                return true;
            default:
                break;
        }

        return false;
    }
*/

    public class SectionsPagerAdapter extends FragmentStatePagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            switch (position) {
                case 0:
                    return new PartyOutStandingFragment();
                case 1:
                    return new BrokerageOutStandingFragment();
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Party Payment";
                case 1:
                    return "Brokerage";
                default:
                    return null;
            }

        }
    }
}
