package com.oozeetech.bizdesk.fragment.drawer.paymentreport;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.oozeetech.bizdesk.BaseFragment;
import com.oozeetech.bizdesk.R;
import com.oozeetech.bizdesk.adapter.MultipleGetPaymentReportAdapter;
import com.oozeetech.bizdesk.listener.MonthWiseListener;
import com.oozeetech.bizdesk.models.payment.PaymentReportRequest;
import com.oozeetech.bizdesk.models.payment.PaymentReportResponse;
import com.oozeetech.bizdesk.utils.Constants;
import com.oozeetech.bizdesk.utils.Utils;
import com.oozeetech.bizdesk.widget.DTextView;

import java.text.DecimalFormat;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentReportFragment extends BaseFragment {
    public int type = 0;
    Unbinder unbinder;
    MultipleGetPaymentReportAdapter adapter;
    @BindView(R.id.lstPartyPayment)
    RecyclerView lstPartyPayment;
    @BindView(R.id.txtINRAmount)
    DTextView txtINRAmount;
    @BindView(R.id.txtUSDAmount)
    DTextView txtUSDAmount;
    @BindView(R.id.llNoRecordFound)
    LinearLayout llNoRecordFound;
    @BindView(R.id.ltheader)
    LinearLayout ltheader;
    @BindView(R.id.llTotal)
    LinearLayout llTotal;
    private Callback<PaymentReportResponse> getPaymentReportResponseCallback = new Callback<PaymentReportResponse>() {
        @Override
        public void onResponse(Call<PaymentReportResponse> call, Response<PaymentReportResponse> response) {
            dismissProgress();
            if (isAdded()) {
                if (response.isSuccessful()) {
                    if (response.body().getReturnCode().equals("1")) {
                        try {
                            lstPartyPayment.setVisibility(View.VISIBLE);
                            llTotal.setVisibility(View.VISIBLE);
                            llNoRecordFound.setVisibility(View.GONE);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        adapter.clear();
                        adapter.addAll(response.body().getData().getPayment());
                        DecimalFormat format = new DecimalFormat("00.00");
                        double inr = Double.parseDouble(response.body().getData().getRsTotalAmt());
                        txtINRAmount.setText(format.format(inr));
                        double usd = Double.parseDouble(response.body().getData().getDollarTotalAmt());
                        txtUSDAmount.setText(format.format(usd));
                    } else if (response.body().getReturnCode().equals("21"))
                        showDialog("", response.body().getReturnMsg(), response.body().getReturnCode());
                    else {
                        try {
                            lstPartyPayment.setVisibility(View.GONE);
                            llTotal.setVisibility(View.GONE);
                            llNoRecordFound.setVisibility(View.VISIBLE);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    log.LOGE("Code: " + response.code() + " Message: " + response.message());
                }
            }
        }

        @Override
        public void onFailure(Call<PaymentReportResponse> call, Throwable t) {
            t.printStackTrace();
            log.LOGE("Failure Response");
            dismissProgress();
        }
    };
    private String partyId = "0";
    private long month = 0, year = 0;
    private MonthWiseListener monthWiseListener = new MonthWiseListener() {
        @Override
        public void monthWiseListener(long month1, long year1) {
            type = 0;
            month = month1;
            year = year1;
            initRecyclerView();
            callGetPaymentReportAPI();
        }
    };
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            type = intent.getIntExtra("type", 0);
            partyId = intent.getStringExtra(Constants.PARTY_IDS);
            month = 0;
            year = 0;
            initRecyclerView();
            callGetPaymentReportAPI();
        }
    };

    public PaymentReportFragment() {
        // Required empty public constructor
    }

    private void callGetPaymentReportAPI() {
        if (!Utils.isInternetConnected(getActivity()))
            showNoInternetDialog();
        else {
            showProgress();
            PaymentReportRequest request = new PaymentReportRequest();
            request.setAPIKey(Constants.API_KEY);
            request.setToken(Utils.getToken(getActivity()));
            request.setPaymentType(1);
            request.setPartyIDs(partyId);
            request.setIsMonthWise(type);
            request.setMonthInInteger(month);
            request.setYearInInteger(year);
            requestAPI.postPaymentReportRequest(request).enqueue(getPaymentReportResponseCallback);
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    protected int getFragmentLayout() {
        return R.layout.fragment_party_payment_report;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // TODO: inflate a fragment view
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        unbinder = ButterKnife.bind(this, rootView);
        initRecyclerView();
        callGetPaymentReportAPI();
        getActivity().registerReceiver(receiver, new IntentFilter("DateFilter"));
        return rootView;
    }
//
//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == RESULT_OK) {
//            GetPartyResponse.Data d = (GetPartyResponse.Data) data.getSerializableExtra(Constants.GET_PARTY);
//            partyId = d.getID();
//            initRecyclerView();
//            callGetPaymentReportAPI();
//        }
////        callGetPaymentReportAPI();
//    }

    private void initRecyclerView() {
        adapter = new MultipleGetPaymentReportAdapter(getActivity(), type, monthWiseListener);
        lstPartyPayment.setLayoutManager(new LinearLayoutManager(getActivity()));
        lstPartyPayment.setHasFixedSize(true);
        lstPartyPayment.setAdapter(adapter);

//        lstPartyPayment.addOnItemTouchListener(
//                new RecyclerItemClickListener(getActivity(), new RecyclerItemClickListener.OnItemClickListener() {
//                    @Override
//                    public void onItemClick(View view, int position) {
//                        // TODO Handle item click
//                    }
//                })
//        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}