package com.oozeetech.bizdesk.fragment.drawer.paymentoutstanding;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.oozeetech.bizdesk.BaseFragment;
import com.oozeetech.bizdesk.R;
import com.oozeetech.bizdesk.adapter.PaymentOutStandingAdapter;
import com.oozeetech.bizdesk.listener.CallListener;
import com.oozeetech.bizdesk.models.payment.GetPaymentOutstandingRequest;
import com.oozeetech.bizdesk.models.payment.GetPaymentOutstandingResponse;
import com.oozeetech.bizdesk.ui.FilterActivity;
import com.oozeetech.bizdesk.utils.Constants;
import com.oozeetech.bizdesk.utils.Utils;
import com.oozeetech.bizdesk.widget.DButton;
import com.oozeetech.bizdesk.widget.DTextView;

import java.text.DecimalFormat;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.app.Activity.RESULT_OK;
import static com.oozeetech.bizdesk.fragment.drawer.MyBizFragment.FILTER_CODE;


public class BrokerageOutStandingFragment extends BaseFragment implements View.OnClickListener {

    PaymentOutStandingAdapter adapter;
    @BindView(R.id.lstPartyPayment)
    RecyclerView lstPartyPayment;
    Unbinder unbinder;
    @BindView(R.id.txtINRAmount)
    DTextView txtINRAmount;
    @BindView(R.id.txtUSDAmount)
    DTextView txtUSDAmount;
    int callPosition = -1;
    @BindView(R.id.llNoRecordFound)
    LinearLayout llNoRecordFound;
    String partyIds = "", costomerIds = "", sellerFromDate = "", sellerToDate = "", paymentFromDate = "", paymentToDate = "";
    @BindView(R.id.llTotal)
    LinearLayout llTotal;
    Callback<GetPaymentOutstandingResponse> getPartyPaymentOutstandingCallback = new Callback<GetPaymentOutstandingResponse>() {
        @Override
        public void onResponse(Call<GetPaymentOutstandingResponse> call, Response<GetPaymentOutstandingResponse> response) {
            dismissProgress();
            if (response.isSuccessful()) {
                if (response.body().getReturnCode().equals("1")) {
                    GetPaymentOutstandingResponse.Data data = response.body().getData();
                    lstPartyPayment.setVisibility(View.VISIBLE);
                    llTotal.setVisibility(View.VISIBLE);
                    llNoRecordFound.setVisibility(View.GONE);
                    adapter.clear();
                    adapter.addAll(data.getList());
                    DecimalFormat format = new DecimalFormat("00.00");
                    double inr = Double.parseDouble(data.getRsTotalAmt());
                    txtINRAmount.setText(format.format(inr));
                    double usd = Double.parseDouble(data.getDollarTotalAmt());
                    txtUSDAmount.setText(format.format(usd));
                } else if (response.body().getReturnCode().equals("21"))
                    showDialog("", response.body().getReturnMsg(), response.body().getReturnCode());
                else {
                    llTotal.setVisibility(View.GONE);
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
    private AlertDialog dialog;
    private DTextView txtTitle;
    private DTextView ADtxtPartyName;
    private LinearLayout llPartyContact1;
    private DTextView txtpartyContact1;
    private ImageView btnPartyContact1;
    private LinearLayout llPartyContact2;
    private DTextView txtpartyContact2;
    private ImageView btnPartyContact2;
    private DTextView ADtxtCustomerName;
    private LinearLayout llCustomerContact1;
    private DTextView txtCustomerContact1;
    private ImageView btnCustomerContact1;
    private LinearLayout llCustomerContact2;
    private DTextView txtCustomerContact2;
    private ImageView btnCustomerContact2;
    private DButton btnCancel;
    CallListener callListener = new CallListener() {
        @Override
        public void onCallTapListener(int position) {
            callPosition = position;
            openDialog();
            if (adapter.get(position).getPartyContact2().equals(""))
                llPartyContact2.setVisibility(View.GONE);
            if (adapter.get(position).getCustomerContact2().equals(""))
                llCustomerContact2.setVisibility(View.GONE);
            ADtxtPartyName.setText(adapter.get(position).getPartyName());
            ADtxtCustomerName.setText(adapter.get(position).getCustomerName());
            txtpartyContact1.setText(adapter.get(position).getPartyContact1());
            txtpartyContact2.setText(adapter.get(position).getPartyContact2());
            txtCustomerContact1.setText(adapter.get(position).getCustomerContact1());
            txtCustomerContact2.setText(adapter.get(position).getCustomerContact2());
        }
    };

    public BrokerageOutStandingFragment() {
        // Required empty public constructor
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    protected int getFragmentLayout() {
        return R.layout.fragment_party_payment_outstanding;
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
        setHasOptionsMenu(true);

        callBrokeragePaymentOutstanding();
        return rootView;
    }

    private void callBrokeragePaymentOutstanding() {
        if (!Utils.isInternetConnected(getActivity()))
            showNoInternetDialog();
        else {
            showProgress();
            GetPaymentOutstandingRequest request = new GetPaymentOutstandingRequest();
            request.setAPIKey(Constants.API_KEY);
            request.setToken(Utils.getToken(getActivity()));
            request.setPaymentType(2);
            request.setPageIndex(-1);
            request.setPartyIDs(partyIds);
            request.setCustomerIDs(costomerIds);
            request.setSaleFromDate(sellerFromDate);
            request.setSaleToDate(sellerToDate);
            request.setPaymentFromDate(paymentFromDate);
            request.setPaymentToDate(paymentToDate);

            requestAPI.postGetPaymentOutstandingRequest(request).enqueue(getPartyPaymentOutstandingCallback);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK)
            return;
        if (requestCode == FILTER_CODE) {
            sellerFromDate = data.getStringExtra(Constants.SELLER_FROM_DATE);
            sellerToDate = data.getStringExtra(Constants.SELLER_TO_DATE);
            paymentFromDate = data.getStringExtra(Constants.PAYMENT_FROM_DATE);
            paymentToDate = data.getStringExtra(Constants.PAYMENT_TO_DATE);
            partyIds = data.getStringExtra(Constants.PARTY_IDS);
            costomerIds = data.getStringExtra(Constants.CUSTOMER_IDS);
            callBrokeragePaymentOutstanding();
        }
//        callBrokeragePaymentOutstanding();
    }

    private void initListView() {

        adapter = new PaymentOutStandingAdapter(getActivity(), callListener, "Brokerage");
        lstPartyPayment.setLayoutManager(new LinearLayoutManager(getActivity()));
        lstPartyPayment.setHasFixedSize(true);
        lstPartyPayment.setAdapter(adapter);

    }

    private void openDialog() {
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_call_now, null);
        dialog = Utils.customDialog(getActivity(), v);
        findViews(v);
        dialog.show();
    }

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


    private void findViews(View v) {
        txtTitle = v.findViewById(R.id.txtTitle);
        ADtxtPartyName = v.findViewById(R.id.ADtxtPartyName);
        llPartyContact1 = v.findViewById(R.id.llPartyContact1);
        txtpartyContact1 = v.findViewById(R.id.txtpartyContact1);
        btnPartyContact1 = v.findViewById(R.id.btnPartyContact1);
        llPartyContact2 = v.findViewById(R.id.llPartyContact2);
        txtpartyContact2 = v.findViewById(R.id.txtpartyContact2);
        btnPartyContact2 = v.findViewById(R.id.btnPartyContact2);
        ADtxtCustomerName = v.findViewById(R.id.ADtxtCustomerName);
        llCustomerContact1 = v.findViewById(R.id.llCustomerContact1);
        txtCustomerContact1 = v.findViewById(R.id.txtCustomerContact1);
        btnCustomerContact1 = v.findViewById(R.id.btnCustomerContact1);
        llCustomerContact2 = v.findViewById(R.id.llCustomerContact2);
        txtCustomerContact2 = v.findViewById(R.id.txtCustomerContact2);
        btnCustomerContact2 = v.findViewById(R.id.btnCustomerContact2);
        btnCancel = v.findViewById(R.id.btnCancel);

        btnCancel.setOnClickListener(this);
        btnPartyContact1.setOnClickListener(this);
        btnPartyContact2.setOnClickListener(this);
        btnCustomerContact1.setOnClickListener(this);
        btnCustomerContact2.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == btnCancel) {
            // Handle clicks for btnCancel
            dialog.dismiss();
        } else if (v == btnPartyContact1) {
            // Handle clicks for btnPartyContact1
            if (!checkPermissionForCallPhone()) {
                requestPermissionForCallPhone();
            } else {
                String partyContact1 = txtpartyContact1.getText().toString().trim();
                callOnNumber(partyContact1);

            }
        } else if (v == btnPartyContact2) {
            if (!checkPermissionForCallPhone()) {
                requestPermissionForCallPhone();
            } else {
                String partyContact2 = txtpartyContact2.getText().toString().trim();
                callOnNumber(partyContact2);
            }
        } else if (v == btnCustomerContact1) {
            if (!checkPermissionForCallPhone()) {
                requestPermissionForCallPhone();
            } else {
                String customerContact1 = txtCustomerContact1.getText().toString().trim();
                callOnNumber(customerContact1);
            }
        } else if (v == btnCustomerContact2) {
            if (!checkPermissionForCallPhone()) {
                requestPermissionForCallPhone();
            } else {
                String customerContact2 = txtpartyContact2.getText().toString().trim();
                callOnNumber(customerContact2);
            }
        }
    }

    private void callOnNumber(String number) {

        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + number));
        startActivity(callIntent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

}
