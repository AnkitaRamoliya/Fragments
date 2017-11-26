package com.oozeetech.bizdesk.fragment.drawer.filterfragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.oozeetech.bizdesk.BaseFragment;
import com.oozeetech.bizdesk.R;
import com.oozeetech.bizdesk.adapter.FilterPartyAdapter;
import com.oozeetech.bizdesk.listener.FilterListener;
import com.oozeetech.bizdesk.models.party.GetPartyRequest;
import com.oozeetech.bizdesk.models.party.GetPartyResponse;
import com.oozeetech.bizdesk.utils.Constants;
import com.oozeetech.bizdesk.utils.Utils;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.app.Activity.RESULT_OK;
import static com.oozeetech.bizdesk.ui.AddNewBizActivity.SELECT_PARTY_CODE;

public class PartyFragment extends BaseFragment {
    public static String partyIds = "";
    FilterPartyAdapter adapter;
    @BindView(R.id.rclSearchParty)
    RecyclerView rclSearchParty;
    Unbinder unbinder;
    @BindView(R.id.llData)
    LinearLayout llData;
    @BindView(R.id.llNoRecordFound)
    LinearLayout llNoRecordFound;
    FilterListener filterListener = new FilterListener() {
        @Override
        public void onCheckChangeListener() {
            ArrayList<String> ids = new ArrayList<>();
            for (int i = 0; i < adapter.getAll().size(); i++) {
                if (adapter.get(i).isSelected())
                    ids.add(adapter.get(i).getID());
            }
            partyIds = TextUtils.join(",", ids);
        }
    };
    private Callback<GetPartyResponse> getPartyResponseCallback = new Callback<GetPartyResponse>() {
        @Override
        public void onResponse(Call<GetPartyResponse> call, Response<GetPartyResponse> response) {
            dismissProgress();
            if (response.isSuccessful()) {
                if (response.body().getReturnCode().equals("1")) {
                    llData.setVisibility(View.VISIBLE);
                    llNoRecordFound.setVisibility(View.GONE);
                    adapter.clear();
                    adapter.addAll(response.body().getData());
                } else if (response.body().getReturnCode().equals("21"))
                    showDialog("", response.body().getReturnMsg(), response.body().getReturnCode());
                else {
                    llData.setVisibility(View.GONE);
                    llNoRecordFound.setVisibility(View.VISIBLE);
                }
            } else {
                log.LOGE("Code: " + response.code() + " Message: " + response.message());
            }
        }

        @Override
        public void onFailure(Call<GetPartyResponse> call, Throwable t) {
            t.printStackTrace();
            log.LOGE("Failure Response");
            dismissProgress();
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // TODO: inflate a fragment view
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        unbinder = ButterKnife.bind(this, rootView);
        ButterKnife.bind(this, rootView);
        initRecyclerView();

        callGetPartyAPI();
        return rootView;

    }

    private void callGetPartyAPI() {

        if (!Utils.isInternetConnected(getActivity()))
            showNoInternetDialog();
        else {
            showProgress();
            GetPartyRequest request = new GetPartyRequest();
            request.setAPIKey(Constants.API_KEY);
            request.setToken(Utils.getToken(getActivity()));
            request.setPageIndex(-1);
            request.setSearchString("");

            requestAPI.postGetPartyRequest(request).enqueue(getPartyResponseCallback);
        }
    }

    private void initRecyclerView() {
        adapter = new FilterPartyAdapter(getActivity(), filterListener);
        rclSearchParty.setLayoutManager(new LinearLayoutManager(getActivity()));
        rclSearchParty.setHasFixedSize(true);
        rclSearchParty.setAdapter(adapter);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK)
            return;
        if (requestCode == SELECT_PARTY_CODE) {
            GetPartyResponse.Data d = (GetPartyResponse.Data) data.getSerializableExtra(Constants.GET_PARTY);
            partyIds = d.getID();
            callGetPartyAPI();
        }
        if (requestCode == RESULT_OK) {
            partyIds = data.getStringExtra(Constants.PARTY_IDS);
            data.getStringExtra(Constants.CUSTOMER_IDS);
            data.getStringExtra(Constants.SELLER_FROM_DATE);
            data.getStringExtra(Constants.SELLER_TO_DATE);
            data.getStringExtra(Constants.PAYMENT_FROM_DATE);
            data.getStringExtra(Constants.PAYMENT_TO_DATE);

        }
    }

    @Override
    protected int getFragmentLayout() {
        return R.layout.fragment_search_party_customer;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}