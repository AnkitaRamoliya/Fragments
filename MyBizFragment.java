package com.oozeetech.bizdesk.fragment.drawer;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.daimajia.swipe.util.Attributes;
import com.oozeetech.bizdesk.BaseFragment;
import com.oozeetech.bizdesk.R;
import com.oozeetech.bizdesk.adapter.GetMyBizSwipeAdapter;
import com.oozeetech.bizdesk.listener.MyBizListener;
import com.oozeetech.bizdesk.models.CommonResponse;
import com.oozeetech.bizdesk.models.mybiz.ConfirmMyBizRequest;
import com.oozeetech.bizdesk.models.mybiz.DeleteMyBizRequest;
import com.oozeetech.bizdesk.models.mybiz.GetBizDetailRequest;
import com.oozeetech.bizdesk.models.mybiz.GetBizDetailResponse;
import com.oozeetech.bizdesk.models.mybiz.GetMyBizRequest;
import com.oozeetech.bizdesk.models.mybiz.GetMyBizResponse;
import com.oozeetech.bizdesk.ui.AddNewBizActivity;
import com.oozeetech.bizdesk.ui.FilterActivity;
import com.oozeetech.bizdesk.ui.drawer.DrawerActivity;
import com.oozeetech.bizdesk.utils.Constants;
import com.oozeetech.bizdesk.utils.Utils;
import com.oozeetech.bizdesk.widget.DButton;
import com.oozeetech.bizdesk.widget.DButtonMaterial;
import com.oozeetech.bizdesk.widget.DTextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import jp.wasabeef.recyclerview.animators.FadeInLeftAnimator;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.app.Activity.RESULT_OK;

public class MyBizFragment extends BaseFragment implements View.OnClickListener {

    public static final int FILTER_CODE = 101;
    @BindView(R.id.rclViewGetMyBiz)
    RecyclerView rclViewGetMyBiz;
    @BindView(R.id.fabAddStock)
    FloatingActionButton fabAddStock;
    Unbinder unbinder;
    GetMyBizSwipeAdapter adapter;
    @BindView(R.id.llNoRecordFound)
    LinearLayout llNoRecordFound;
    String partyIds = "", customerIds = "", sellerFromDate = "", sellerToDate = "", paymentFromDate = "", paymentToDate = "";
    private DTextView txtTitle;
    private DTextView txtRough;
    private DTextView txtPolished;
    private DButton btnCancel;
    private DButtonMaterial btnADCancel;
    private DButtonMaterial btnDelete;
    private AlertDialog dialog;
    private GetBizDetailResponse.Data data;
    private Callback<GetBizDetailResponse> getBizDetailResponseCallback = new Callback<GetBizDetailResponse>() {
        @Override
        public void onResponse(Call<GetBizDetailResponse> call, Response<GetBizDetailResponse> response) {
            dismissProgress();
            if (response.isSuccessful()) {
                if (response.body().getReturnCode().equals("1")) {
                    data = response.body().getData();
                    Intent i = new Intent(getActivity(), AddNewBizActivity.class);
                    i.putExtra(Constants.TYPE, "Update");
                    i.putExtra(Constants.UPDATE_BIZ, data);
                    startActivity(i);
                } else if (response.body().getReturnCode().equals("21"))
                    showDialog("", response.body().getReturnMsg(), response.body().getReturnCode());
                else
                    showDialog("", response.body().getReturnMsg(), response.body().getReturnCode());
            } else {
                log.LOGE("Code: " + response.code() + " Message: " + response.message());
            }
        }

        @Override
        public void onFailure(Call<GetBizDetailResponse> call, Throwable t) {
            log.LOGE("Failure Response");
            dismissProgress();
        }
    };
    private Callback<GetMyBizResponse> getMyBizResponseCallback = new Callback<GetMyBizResponse>() {
        @Override
        public void onResponse(Call<GetMyBizResponse> call, Response<GetMyBizResponse> response) {
            dismissProgress();
            if (response.isSuccessful()) {
                if (response.body().getReturnCode().equals("1")) {
                    rclViewGetMyBiz.setVisibility(View.VISIBLE);
                    llNoRecordFound.setVisibility(View.GONE);
                    adapter.clear();
                    adapter.addAll(response.body().getData());
                } else if (response.body().getReturnCode().equals("21"))
                    showDialog("", response.body().getReturnMsg(), response.body().getReturnCode());
                else {
                    rclViewGetMyBiz.setVisibility(View.GONE);
                    llNoRecordFound.setVisibility(View.VISIBLE);
                }
            } else {
                log.LOGE("Code: " + response.code() + " Message: " + response.message());
            }
        }

        @Override
        public void onFailure(Call<GetMyBizResponse> call, Throwable t) {
            t.printStackTrace();
            log.LOGE("Failure Response");
            dismissProgress();
        }
    };
    private Callback<CommonResponse> deleteResponseCallback = new Callback<CommonResponse>() {
        @Override
        public void onResponse(Call<CommonResponse> call, Response<CommonResponse> response) {
            dialog.dismiss();
            dismissProgress();
            if (response.isSuccessful()) {
                if (response.body().getReturnCode().equals("1")) {
//                    showDialogDone("Biz Desk", "Record Deleted Successfully");
                    showToast("Record Deleted Successfully", true);
                    callGetMyBizAPI();
                } else if (response.body().getReturnCode().equals("4")) {
//                    showDialogDone("Biz Desk", "First DELETE Payment Receipt Entry..");
                    showToast("First DELETE Payment Receipt Entry..", true);
                } else if (response.body().getReturnCode().equals("21"))
                    showDialog("", response.body().getReturnMsg(), response.body().getReturnCode());
            } else {
                log.LOGE("Code: " + response.code() + " Message: " + response.message());
            }
        }

        @Override
        public void onFailure(Call<CommonResponse> call, Throwable t) {
            t.printStackTrace();
            log.LOGE("Failure Response");
            dismissProgress();
        }
    };
    private AlertDialog builder;
    private Callback<CommonResponse> confirmResponseCallback = new Callback<CommonResponse>() {
        @Override
        public void onResponse(Call<CommonResponse> call, Response<CommonResponse> response) {
            dismissProgress();
            if (response.isSuccessful()) {
                if (response.body().getReturnCode().equals("1")) {
                    builder.dismiss();
                    callGetMyBizAPI();
//                    notifyAll();
                } else if (response.body().getReturnCode().equals("21"))
                    showDialog("", response.body().getReturnMsg(), response.body().getReturnCode());
            } else {
                log.LOGE("Code: " + response.code() + " Message: " + response.message());
            }
        }

        @Override
        public void onFailure(Call<CommonResponse> call, Throwable t) {
            t.printStackTrace();
            log.LOGE("Failure Response");
            dismissProgress();
        }
    };
    private String bizMasterId;
    MyBizListener myBizListener = new MyBizListener() {
        @Override
        public void onEditClickListener(String masterId) {
            if (!Utils.isInternetConnected(getActivity()))
                showNoInternetDialog();
            else {
                showProgress();
                GetBizDetailRequest request = new GetBizDetailRequest();
                request.setAPIKey(Constants.API_KEY);
                request.setToken(Utils.getToken(getActivity()));
                request.setBizMasterID(masterId);

                requestAPI.postGetBizDetailRequest(request).enqueue(getBizDetailResponseCallback);
            }
        }

        @Override
        public void onConfirmClickListener(String masterId) {
            bizMasterId = masterId;
            openConfirmDialog();
        }

        @Override
        public void onDeleteClickListener(final String masterId) {

            View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_delete_record, null);
            dialog = Utils.customDialog(getActivity(), v);
            txtTitle = v.findViewById(R.id.txtTitle);
            btnADCancel = v.findViewById(R.id.btnCancel);
            btnDelete = v.findViewById(R.id.btnDelete);

            dialog.show();
            btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!Utils.isInternetConnected(getActivity()))
                        showNoInternetDialog();
                    else {
                        showProgress();
                        DeleteMyBizRequest request = new DeleteMyBizRequest();
                        request.setAPIKey("123");
                        request.setToken(Utils.getToken(getActivity()));
                        request.setID(Double.parseDouble(masterId));
                        requestAPI.postDeleteMyBizRequest(request).enqueue(deleteResponseCallback);
                    }
                }
            });
            btnADCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                }
            });
        }
    };

    public MyBizFragment() {
        // Required empty public constructor
    }

    private void openConfirmDialog() {
        builder = new AlertDialog.Builder(getActivity())
                .setTitle("Confirm Biz")
                .setMessage("Do you Want to Confirm Biz ..?")
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!Utils.isInternetConnected(getActivity()))
                            showNoInternetDialog();
                        else {
                            showProgress();
                            ConfirmMyBizRequest request = new ConfirmMyBizRequest();

                            request.setAPIKey("123");
                            request.setToken(Utils.getToken(getActivity()));
                            request.setID(Double.parseDouble(bizMasterId));
                            requestAPI.postConfirmMyBizRequest(request).enqueue(confirmResponseCallback);
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        builder.dismiss();
                    }
                }).create();

        builder.show();

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
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
        switch (item.getItemId()) {
            case R.id.action_filter:
                // Do Activity menu item stuff here
                Intent i = new Intent(getActivity(), FilterActivity.class);
                i.putExtra(Constants.SELLER_FROM_DATE, sellerFromDate);
                i.putExtra(Constants.SELLER_TO_DATE, sellerToDate);
                i.putExtra(Constants.PAYMENT_FROM_DATE, paymentFromDate);
                i.putExtra(Constants.PAYMENT_TO_DATE, paymentToDate);
                i.putExtra(Constants.PARTY_IDS, partyIds);
                i.putExtra(Constants.CUSTOMER_IDS, customerIds);
                startActivityForResult(i, FILTER_CODE);
                return true;
            default:
                break;
        }
        return false;
    }

    private void callGetMyBizAPI() {
        if (!Utils.isInternetConnected(getActivity())) {
            showNoInternetDialog();
        } else {
            showProgress();
            GetMyBizRequest request = new GetMyBizRequest();
            request.setAPIKey(Constants.API_KEY);
            request.setToken(Utils.getToken(getActivity()));
            request.setPageIndex(-1);
            request.setPartyIDs(partyIds);
            request.setCustomerIDs(customerIds);
            request.setSaleFromDate(sellerFromDate);
            request.setSaleToDate(sellerToDate);
            request.setPaymentFromDate(paymentFromDate);
            request.setPaymentToDate(paymentToDate);

            requestAPI.postGetMyBizRequest(request).enqueue(getMyBizResponseCallback);
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    protected int getFragmentLayout() {
        return R.layout.fragment_my_biz;
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

        initRecyclerView();
        callGetMyBizAPI();
        return rootView;
    }

    private void initRecyclerView() {
        adapter = new GetMyBizSwipeAdapter(getActivity(), myBizListener);

        rclViewGetMyBiz.setLayoutManager(new LinearLayoutManager(getActivity()));
        rclViewGetMyBiz.setHasFixedSize(true);
        rclViewGetMyBiz.setItemAnimator(new FadeInLeftAnimator());
        adapter.setMode(Attributes.Mode.Single);

        rclViewGetMyBiz.setAdapter(adapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick(R.id.fabAddStock)
    public void onViewClicked() {
        openDialog();
    }

    private void openDialog() {
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_biz_type, null);
        dialog = Utils.customDialog(getActivity(), v);
        findViews(v);
        dialog.show();
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
            customerIds = data.getStringExtra(Constants.CUSTOMER_IDS);
            callGetMyBizAPI();
        }
//        callGetMyBizAPI();
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
}