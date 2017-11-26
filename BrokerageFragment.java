package com.oozeetech.bizdesk.fragment.homefragment;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.oozeetech.bizdesk.BaseFragment;
import com.oozeetech.bizdesk.R;
import com.oozeetech.bizdesk.adapter.PartyPaymentAdapter;
import com.oozeetech.bizdesk.models.payment.GetPaymentOutstandingRequest;
import com.oozeetech.bizdesk.models.payment.GetPaymentOutstandingResponse;
import com.oozeetech.bizdesk.utils.Constants;
import com.oozeetech.bizdesk.utils.Utils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class BrokerageFragment extends BaseFragment {

    PartyPaymentAdapter adapter;

    @BindView(R.id.lstPartyPayment)
    RecyclerView lstPartyPayment;
    Unbinder unbinder;
    @BindView(R.id.llNoRecordFound)
    LinearLayout llNoRecordFound;
    Callback<GetPaymentOutstandingResponse> getPartyPaymentOutstandingCallback = new Callback<GetPaymentOutstandingResponse>() {
        @Override
        public void onResponse(Call<GetPaymentOutstandingResponse> call, Response<GetPaymentOutstandingResponse> response) {
            dismissProgress();
            if (response.isSuccessful()) {
                if (response.body().getReturnCode().equals("1")) {
                    GetPaymentOutstandingResponse.Data data = response.body().getData();

                    adapter.clear();
                    adapter.addAll(data.getList());
                    try {
                        lstPartyPayment.setVisibility(View.VISIBLE);
                        llNoRecordFound.setVisibility(View.GONE);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (response.body().getReturnCode().equals("21"))
                    showDialog("", response.body().getReturnMsg(), response.body().getReturnCode());
                else {
                    lstPartyPayment.setVisibility(View.GONE);
                    llNoRecordFound.setVisibility(View.VISIBLE);
                }
            } else {
                log.LOGE("Code: " + response.code() + " Message: " + response.message());
            }
        }

        @Override
        public void onFailure(Call<GetPaymentOutstandingResponse> call, Throwable t) {
            t.printStackTrace();
            dismissProgress();
        }
    };

    public BrokerageFragment() {
        // Required empty public constructor
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    protected int getFragmentLayout() {
        return R.layout.fragment_party_payment;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // TODO: inflate a fragment view
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        unbinder = ButterKnife.bind(this, rootView);

        initListView();
        if (!Utils.isInternetConnected(getActivity()))
            showNoInternetDialog();
        else
            callPartyPaymentOutstanding();

        return rootView;
    }

    private void callPartyPaymentOutstanding() {
        if (!Utils.isInternetConnected(getActivity()))
            showNoInternetDialog();
        else {
            showProgress();
            GetPaymentOutstandingRequest request = new GetPaymentOutstandingRequest();
            request.setAPIKey(Constants.API_KEY);
            request.setToken(Utils.getToken(getActivity()));
            request.setPaymentType(2);
            request.setPageIndex(-1);
            request.setPartyIDs("");
            request.setCustomerIDs("");
            request.setSaleFromDate("");
            request.setSaleToDate("");
            request.setPaymentFromDate("");
            request.setPaymentToDate("");

            requestAPI.postGetPaymentOutstandingRequest(request).enqueue(getPartyPaymentOutstandingCallback);
        }
    }

    private void initListView() {

        adapter = new PartyPaymentAdapter(getActivity());
        lstPartyPayment.setLayoutManager(new LinearLayoutManager(getActivity()));
        lstPartyPayment.setHasFixedSize(true);
        lstPartyPayment.setAdapter(adapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
