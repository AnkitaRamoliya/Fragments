package com.oozeetech.bizdesk.fragment.drawer;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.oozeetech.bizdesk.BaseFragment;
import com.oozeetech.bizdesk.R;
import com.oozeetech.bizdesk.fragment.homefragment.BrokerageFragment;
import com.oozeetech.bizdesk.fragment.homefragment.PartyPaymentFragment;
import com.oozeetech.bizdesk.models.chartdata.DashboardChartRequest;
import com.oozeetech.bizdesk.models.chartdata.DashboardChartResponse;
import com.oozeetech.bizdesk.ui.AddNewBizActivity;
import com.oozeetech.bizdesk.ui.NotificationActivity;
import com.oozeetech.bizdesk.utils.Constants;
import com.oozeetech.bizdesk.utils.DateUtils;
import com.oozeetech.bizdesk.utils.Utils;
import com.oozeetech.bizdesk.widget.DButton;
import com.oozeetech.bizdesk.widget.DTextView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.oozeetech.bizdesk.utils.FontUtils.fontName;


public class HomeFragment extends BaseFragment implements View.OnClickListener {

    @BindView(R.id.home_slide_tabs)
    TabLayout home_slide_tabs;
    @BindView(R.id.viewpager)
    ViewPager viewpager;
    @BindView(R.id.fabAdd)
    FloatingActionButton fabAdd;
    Unbinder unbinder;
    @BindView(R.id.llMyBiz)
    LinearLayout llMyBiz;
    @BindView(R.id.llPaymentOutStanding)
    LinearLayout llPaymentOutStanding;
    @BindView(R.id.llPaymentReceipt)
    LinearLayout llPaymentReceipt;
    @BindView(R.id.llPriceList)
    LinearLayout llPriceList;
    @BindView(R.id.llSetting)
    LinearLayout llSetting;
    @BindView(R.id.llMyProfile)
    LinearLayout llMyProfile;
    @BindView(R.id.activity_main)
    LinearLayout activityMain;
    @BindView(R.id.chartBar)
    BarChart chartBar;
    @BindView(R.id.btnImgPrevGraph)
    ImageView btnImgPrevGraph;
    @BindView(R.id.btnImgNextGraph)
    ImageView btnImgNextGraph;
    List<DashboardChartResponse.Data> data;
    //    Calendar now = Calendar.getInstance();
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private AlertDialog dialog;
    private DTextView txtTitle;
    private DTextView txtRough;
    private DTextView txtPolished;
    private DButton btnCancel;
    private String currentDate;
    private Callback<DashboardChartResponse> dashboardChartResponseCallback = new Callback<DashboardChartResponse>() {
        @Override
        public void onResponse(Call<DashboardChartResponse> call, Response<DashboardChartResponse> response) {
            dismissProgress();
            if (response.isSuccessful()) {
                if (response.body().getReturnCode().equals("1")) {
                    data = new ArrayList<>();
                    data.addAll(response.body().getData());
//                    currentDate = response.body().getReturnValue();
                    if (data.size() == 0)
                        try {
                            chartBar.clear();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    else
                        try {
                            initChart(data);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                } else if (response.body().getReturnCode().equals("21"))
                    showDialog("", response.body().getReturnMsg(), response.body().getReturnCode());
            } else {
                log.LOGE("Code: " + response.code() + " Message: " + response.message());
            }
        }

        @Override
        public void onFailure(Call<DashboardChartResponse> call, Throwable t) {
            t.printStackTrace();
            log.LOGE("Failure Response");
            dismissProgress();
        }
    };

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getChildFragmentManager());
        viewpager.setAdapter(mSectionsPagerAdapter);
        viewpager.setOffscreenPageLimit(2);
        home_slide_tabs.setupWithViewPager(viewpager);
        changeTabsFont();
    }

    private void initChart(final List<DashboardChartResponse.Data> chartData) {

        try {
            if (chartBar.getDescription() != null)
                chartBar.getDescription().setEnabled(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        chartBar.setTouchEnabled(false);
        chartBar.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        chartBar.getXAxis().setDrawGridLines(false);
        chartBar.getAxisRight().setEnabled(false);
        chartBar.getAxisLeft().setEnabled(false);
        chartBar.animateY(2500);
//        chartBar.getXAxis().setAxisMaximum(chartData.size() < 6 ? chartData.size() : 6);
        chartBar.setFitBars(true);
        chartBar.getLegend().setEnabled(false);

        ArrayList<BarEntry> list = new ArrayList<>();
        for (int i = 0; i < chartData.size(); i++) {
            list.add(new BarEntry(i, (float) chartData.get(i).getAmount()));
        }
        chartBar.getXAxis().setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                String[] s = TextUtils.split(chartData.get((int) value % chartData.size()).getPaymentDate(), "-");
                String date = DateUtils.convertDateStringToString(s[0], "MMMM", "MMM");
                return date + "-" + s[1];
            }
        });

        BarDataSet set1 = new BarDataSet(list, "Data Set");
        set1.setColors(ContextCompat.getColor(getActivity(), R.color.gray_400));
        //for set values on bar
        set1.setDrawValues(false);

        ArrayList<IBarDataSet> dataSets = new ArrayList<IBarDataSet>();
        dataSets.add(set1);
        chartBar.setFitBars(set1.isStacked());

        BarData data = new BarData(dataSets);
        data.setBarWidth(0.3f);
        chartBar.setData(data);
    }


    private void changeTabsFont() {
        ViewGroup vg = (ViewGroup) home_slide_tabs.getChildAt(0);
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
        return R.layout.fragment_home;
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

    @OnClick(R.id.fabAdd)
    public void onViewClicked() {
        openDialog();
    }


    private void findViews(View v) {
        txtTitle = v.findViewById(R.id.txtTitle);
        txtRough = v.findViewById(R.id.txtRough);
        txtPolished = v.findViewById(R.id.txtPolished);
        btnCancel = v.findViewById(R.id.btnCancel);

        txtRough.setOnClickListener(this);
        txtPolished.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        currentDate = DateUtils.millisToDate(System.currentTimeMillis(), "yyyy-MM-dd");
//        currentDate = DateUtils.convertDateToString(now.getTime(), "yyyy-MM-dd");
        callGraphApi(0);
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.txtRough:
                dialog.dismiss();
                intent = new Intent(getActivity(), AddNewBizActivity.class);
                intent.putExtra(Constants.TYPE, "Rough");
                startActivity(intent);
                break;
            case R.id.txtPolished:
                dialog.dismiss();
                intent = new Intent(getActivity(), AddNewBizActivity.class);
                intent.putExtra(Constants.TYPE, "Polish");
                startActivity(intent);
                break;
            case R.id.btnCancel:
                dialog.dismiss();
                break;
        }
    }


    private void openDialog() {
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_biz_type, null);
        dialog = Utils.customDialog(getActivity(), v);
        findViews(v);
        dialog.show();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        // TODO Add your menu entries here
        menu.clear();
        inflater.inflate(R.menu.home, menu);
        MenuItem menuItem = menu.findItem(R.id.action_notification);
        menuItem.setVisible(true);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_notification:
                // Do Activity menu item stuff here
                Intent i = new Intent(getActivity(), NotificationActivity.class);
                startActivity(i);
                return true;
            default:
                break;
        }
        return false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // TODO: inflate a fragment view
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        unbinder = ButterKnife.bind(this, rootView);
        return rootView;
    }

    @OnClick({R.id.btnImgPrevGraph, R.id.btnImgNextGraph})
    public void onImageClick(View view) {
        DateFormat outputFormat = new SimpleDateFormat("dd-MMMM-yyyy");
        String inputDateStr = null;
        Date date = null;
        switch (view.getId()) {
            case R.id.btnImgPrevGraph:
                inputDateStr = "01-" + data.get(0).getPaymentDate();
                try {
                    date = outputFormat.parse(inputDateStr);
                    currentDate = DateUtils.convertDateToString(date, "yyyy-MM-dd");
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                log.LOGE(currentDate);

                callGraphApi(0);
                break;
            case R.id.btnImgNextGraph:
                inputDateStr = "01-" + data.get(5).getPaymentDate();
                try {
                    date = outputFormat.parse(inputDateStr);
                    currentDate = DateUtils.convertDateToString(date, "yyyy-MM-dd");
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                log.LOGE(currentDate);

                callGraphApi(1);
                break;
        }
    }

    @OnClick({R.id.llMyBiz, R.id.llPaymentOutStanding, R.id.llPaymentReceipt, R.id.llPriceList, R.id.llSetting, R.id.llMyProfile})
    public void onViewClicked(View view) {
        Intent intent = new Intent("ChangeFragment");
        switch (view.getId()) {
            case R.id.llMyBiz:
                intent.putExtra("Fragment", getString(R.string.nav_item_my_biz));
                break;
            case R.id.llPaymentOutStanding:
                intent.putExtra("Fragment", getString(R.string.nav_item_payment_outstanding));
                break;
            case R.id.llPaymentReceipt:
                intent.putExtra("Fragment", getString(R.string.nav_item_payment_receipt_report));
                break;
            case R.id.llPriceList:
                intent.putExtra("Fragment", getString(R.string.nav_item_price_list));
                break;
            case R.id.llSetting:
                intent.putExtra("Fragment", getString(R.string.nav_item_settings));
                break;
            case R.id.llMyProfile:
                intent.putExtra("Fragment", getString(R.string.nav_item_my_profile));
                break;
        }
        getActivity().sendBroadcast(intent);
    }

    private void callGraphApi(int isNext) {

        if (!Utils.isInternetConnected(getActivity()))
            showNoInternetDialog();
        else {
            showProgress();
            DashboardChartRequest request = new DashboardChartRequest();
            request.setAPIKey(Constants.API_KEY);
            request.setToken(Utils.getToken(getActivity()));
            request.setDate(currentDate);
            request.setIsNext(isNext);

            requestAPI.postDashboardChartRequest(request).enqueue(dashboardChartResponseCallback);
        }
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
                    return new PartyPaymentFragment();
                case 1:
                    return new BrokerageFragment();

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
