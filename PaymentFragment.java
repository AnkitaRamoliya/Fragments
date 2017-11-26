package com.oozeetech.bizdesk.fragment.drawer.paymentreport;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.oozeetech.bizdesk.BaseFragment;
import com.oozeetech.bizdesk.R;
import com.oozeetech.bizdesk.fragment.drawer.HomeFragment;
import com.oozeetech.bizdesk.models.party.GetPartyResponse;
import com.oozeetech.bizdesk.ui.SearchPartyActivity;
import com.oozeetech.bizdesk.ui.drawer.DrawerActivity;
import com.oozeetech.bizdesk.utils.Constants;
import com.oozeetech.bizdesk.utils.Utils;
import com.oozeetech.bizdesk.widget.DButton;
import com.oozeetech.bizdesk.widget.DButtonMaterial;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static android.app.Activity.RESULT_OK;
import static com.oozeetech.bizdesk.fragment.drawer.MyBizFragment.FILTER_CODE;
import static com.oozeetech.bizdesk.utils.FontUtils.fontName;


public class PaymentFragment extends BaseFragment implements View.OnClickListener {

    @BindView(R.id.tabs)
    TabLayout tabs;
    @BindView(R.id.viewpager)
    ViewPager viewpager;
    Unbinder unbinder;
    AlertDialog dialog;
    int type = 0;
    boolean isFilterSelected = false, isPartySelected = false;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private String partyId = "0";
    private DButton btnMonthWise;
    private DButton btnDateWise;
    private DButtonMaterial btnCancel;
    private Menu menu;

    public PaymentFragment() {
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            menu.findItem(R.id.action_party_filter).setIcon(ContextCompat.getDrawable(getActivity(), R.drawable.ic_filter_selected));

            GetPartyResponse.Data d = (GetPartyResponse.Data) data.getSerializableExtra(Constants.GET_PARTY);
            partyId = d.getID();
            callBroadCast();
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    protected int getFragmentLayout() {
        return R.layout.fragment_payment_report;
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
        setHasOptionsMenu(true);
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // TODO Add your menu entries here
        menu.clear();
        this.menu = menu;
        inflater.inflate(R.menu.home, menu);
        menu.findItem(R.id.action_calender).setVisible(true);
        menu.findItem(R.id.action_party_filter).setVisible(true);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent i;
        switch (item.getItemId()) {
            case R.id.action_calender:
                // Do Activity menu item stuff here
                if (!isFilterSelected) {
                    openDialog();
                } else {
                    isFilterSelected = false;
                    menu.findItem(R.id.action_calender).setIcon(ContextCompat.getDrawable(getActivity(), R.drawable.ic_date));
                    type = 0;
                    callBroadCast();
                }
                return true;
            case R.id.action_party_filter:
                // Do Activity menu item stuff here
                if (!isPartySelected) {
                    i = new Intent(getActivity(), SearchPartyActivity.class);
                    i.putExtra(Constants.TYPE, "Party");
                    i.putExtra(Constants.PARTY_IDS, partyId);
                    startActivityForResult(i, FILTER_CODE);
                    isPartySelected = true;
                } else {
                    isPartySelected = false;
                    menu.findItem(R.id.action_party_filter).setIcon(ContextCompat.getDrawable(getActivity(), R.drawable.ic_party_filter));
                    partyId = "0";
                    callBroadCast();
                }
                return true;
            default:
                break;
        }

        return false;
    }

    private void openDialog() {
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_view_receipt, null);
        dialog = Utils.customDialog(getActivity(), v);
        findViews(v);
        dialog.show();
    }

    private void findViews(View v) {
        btnMonthWise = v.findViewById(R.id.btnMonthWise);
        btnDateWise = v.findViewById(R.id.btnDateWise);
        btnCancel = v.findViewById(R.id.btnCancel);

        btnMonthWise.setOnClickListener(this);
        btnDateWise.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btnMonthWise:
                isFilterSelected = true;
                type = 1;
                menu.findItem(R.id.action_calender).setIcon(ContextCompat.getDrawable(getActivity(), R.drawable.ic_calender_selected));
                callBroadCast();
                break;
            case R.id.btnDateWise:
                type = 0;
                menu.findItem(R.id.action_calender).setIcon(ContextCompat.getDrawable(getActivity(), R.drawable.ic_date));
                callBroadCast();
                break;
            case R.id.btnCancel:
                break;
        }
        dialog.dismiss();
    }

    private void callBroadCast() {
        Intent intent = new Intent("DateFilter");
        intent.putExtra(Constants.TYPE, type);
        intent.putExtra(Constants.PARTY_IDS, partyId);
        getActivity().sendBroadcast(intent);
    }

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
                    return new PaymentReportFragment();
                case 1:
                    return new BrokerageReportFragment();

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
