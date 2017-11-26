package com.oozeetech.bizdesk.fragment.drawer;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Switch;

import com.daimajia.swipe.util.Attributes;
import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.oozeetech.bizdesk.BaseFragment;
import com.oozeetech.bizdesk.R;
import com.oozeetech.bizdesk.adapter.GetMyStockAdapter;
import com.oozeetech.bizdesk.listener.MyStockListener;
import com.oozeetech.bizdesk.models.CommonResponse;
import com.oozeetech.bizdesk.models.mystock.AddMyStockRequest;
import com.oozeetech.bizdesk.models.mystock.DeleteMyStockRequest;
import com.oozeetech.bizdesk.models.mystock.GetMyStockRequest;
import com.oozeetech.bizdesk.models.mystock.GetMyStockResponse;
import com.oozeetech.bizdesk.ui.drawer.DrawerActivity;
import com.oozeetech.bizdesk.utils.Constants;
import com.oozeetech.bizdesk.utils.Utils;
import com.oozeetech.bizdesk.widget.DButtonMaterial;
import com.oozeetech.bizdesk.widget.DCheckBox;
import com.oozeetech.bizdesk.widget.DEditText;
import com.oozeetech.bizdesk.widget.DTextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import jp.wasabeef.recyclerview.animators.FadeInLeftAnimator;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MyStockFragment extends BaseFragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    public double totalAmt = 0, weight = 0, rate = 0, finaRate = 0, shipPer = 0, shipCharge = 0, premium = 0;
    public double pltotalAmt = 0, plweight = 0, plrate = 0, plshipPer = 0, plshipCharge = 0;
    public int id;
    AlertDialog dialog;
    @BindView(R.id.rclViewGetMyStock)
    RecyclerView rclViewGetMyStock;
    @BindView(R.id.fabAddStock)
    FloatingActionButton fabAddStock;
    Unbinder unbinder;
    GetMyStockAdapter adapter;
    @BindView(R.id.llNoRecordFound)
    LinearLayout llNoRecordFound;
    @BindView(R.id.search_view)
    MaterialSearchView searchView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.search_top_bar)
    FrameLayout searchTopBar;
    private DTextView txtType;
    private Switch switchRoughToPolished;
    private Switch switch$ToINR;
    private DEditText edItemName;
    private DEditText edWeight;
    private DEditText edplWeight;
    private DEditText edCut;
    private DEditText edRate;
    private DEditText edplRate;
    private DEditText edPremium;
    private DTextView txtFinalRate;
    private DTextView txtRghTotalAmount;
    private DTextView txtplTotalAmt;
    private DCheckBox ckbIsShipping;
    private CardView cvShipPer;
    private DEditText edPer;
    private LinearLayout llShippingAmount;
    private LinearLayout llRough;
    private LinearLayout llPolished;
    private DTextView txtTotalShipAmt;
    private DButtonMaterial btnCancel;
    private DButtonMaterial btnAdd;
    private Callback<GetMyStockResponse> getMyStockResponseCallback = new Callback<GetMyStockResponse>() {
        @Override
        public void onResponse(Call<GetMyStockResponse> call, Response<GetMyStockResponse> response) {
            dismissProgress();
            if (response.isSuccessful()) {
                if (response.body().getReturnCode().equals("1")) {
                    rclViewGetMyStock.setVisibility(View.VISIBLE);
                    llNoRecordFound.setVisibility(View.GONE);
                    adapter.clear();
                    adapter.addAll(response.body().getData());
                } else if (response.body().getReturnCode().equals("21"))
                    showDialog("", response.body().getReturnMsg(), response.body().getReturnCode());
                else {
                    rclViewGetMyStock.setVisibility(View.GONE);
                    llNoRecordFound.setVisibility(View.VISIBLE);
                }
            } else {
                log.LOGE("Code: " + response.code() + " Message: " + response.message());
            }
        }

        @Override
        public void onFailure(Call<GetMyStockResponse> call, Throwable t) {
            t.printStackTrace();
            log.LOGE("Failure Response");
            dismissProgress();
        }
    };
    private Callback<CommonResponse> deleteResponseCallback = new Callback<CommonResponse>() {
        @Override
        public void onResponse(Call<CommonResponse> call, Response<CommonResponse> response) {
            dismissProgress();
            if (response.isSuccessful()) {
                if (response.body().getReturnCode().equals("1")) {
                    showDialog("Biz Desk", "Record Deleted Successfully");
                } else if (response.body().getReturnCode().equals("4")) {
                    showDialogDone("Biz Desk", "First DELETE Payment Receipt Entry..");
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
    MyStockListener myStockListener = new MyStockListener() {
        @Override
        public void onDeleteClickListener(long masterId, int i) {
            id = i;
            if (!Utils.isInternetConnected(getActivity()))
                showNoInternetDialog();
            else {
                showProgress();
                DeleteMyStockRequest request = new DeleteMyStockRequest();
                request.setAPIKey("123");
                request.setToken(Utils.getToken(getActivity()));
                request.setMyStockID(masterId);
                requestAPI.postDeleteMyStockRequest(request).enqueue(deleteResponseCallback);
            }
        }
    };
    private Callback<CommonResponse> getAddStockResponseCallback = new Callback<CommonResponse>() {
        @Override
        public void onResponse(Call<CommonResponse> call, Response<CommonResponse> response) {
            dismissProgress();
            if (response.isSuccessful()) {
                if (response.body().getReturnCode().equals("1")) {
                    dialog.dismiss();
                    rclViewGetMyStock.setVisibility(View.VISIBLE);
                    llNoRecordFound.setVisibility(View.GONE);
                    callGetMyStokeAPI();
                } else if (response.body().getReturnCode().equals("21"))
                    showDialog("", response.body().getReturnMsg(), response.body().getReturnCode());
                else {
                    rclViewGetMyStock.setVisibility(View.GONE);
                    llNoRecordFound.setVisibility(View.VISIBLE);
                }
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

    public MyStockFragment() {
        // Required empty public constructor
    }

    private void showDialog(String title, String msg) {
        AlertDialog builder = new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setMessage(msg)
                .setPositiveButton(R.string.hint_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        adapter.remove(id);
                        callGetMyStokeAPI();
                    }
                }).create();
        builder.show();
    }

    private void callGetMyStokeAPI() {
        if (!Utils.isInternetConnected(getActivity()))
            showNoInternetDialog();
        else {
            showProgress();
            GetMyStockRequest request = new GetMyStockRequest();
            request.setAPIKey(Constants.API_KEY);
            request.setToken(Utils.getToken(getActivity()));
            request.setPageIndex(-1);
            request.setSearchString("");

            requestAPI.postGetMyStockRequest(request).enqueue(getMyStockResponseCallback);
        }
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // TODO Add your menu entries here
        DrawerActivity.searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //Do some magic
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //Do some magic
                adapter.getFilter().filter(newText);
                return false;
            }
        });
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    protected int getFragmentLayout() {
        return R.layout.fragment_my_stock;
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

        toolbar.setVisibility(View.GONE);
        initRecyclerView();
        callGetMyStokeAPI();
        return rootView;
    }

    private void initRecyclerView() {
        adapter = new GetMyStockAdapter(getActivity(), myStockListener);

        rclViewGetMyStock.setLayoutManager(new LinearLayoutManager(getActivity()));
        rclViewGetMyStock.setHasFixedSize(true);
        rclViewGetMyStock.setItemAnimator(new FadeInLeftAnimator());
        adapter.setMode(Attributes.Mode.Single);
        rclViewGetMyStock.setAdapter(adapter);
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

    private void actionListeners() {

        edWeight.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                calculateNsetText();
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        edRate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                calculateNsetText();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }

        });
        edPremium.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                calculateNsetText();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        ckbIsShipping.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (ckbIsShipping.isChecked()) {
                    cvShipPer.setVisibility(View.VISIBLE);
                    llShippingAmount.setVisibility(View.VISIBLE);
                    if (edPer.equals(""))
                        showToast("Shipping Percentage can't be Blank", true);
                    else {
                        edPer.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                            }

                            @Override
                            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                                shipingChargesValueChange();
                            }

                            @Override
                            public void afterTextChanged(Editable editable) {

                            }
                        });
                    }
                } else {
                    cvShipPer.setVisibility(View.GONE);
                    llShippingAmount.setVisibility(View.GONE);

                }
            }
        });
        //adapter.addAll();
    }

    private void shipingChargesValueChange() {
        if (switchRoughToPolished.isChecked()) {
            llRough.setVisibility(View.GONE);
            llPolished.setVisibility(View.VISIBLE);
            calculateNsetTextPL();
        } else if (!switchRoughToPolished.isChecked()) {
            llRough.setVisibility(View.VISIBLE);
            llPolished.setVisibility(View.GONE);
            calculateNsetText();
        }
    }

    private void calculateNsetText() {
        weight = edWeight.getDouble();
        rate = edRate.getDouble();
        premium = edPremium.getDouble();
        shipPer = edPer.getDouble();

        finaRate = rate + (rate / 100) * premium;
        totalAmt = (rate * weight) + ((rate * weight) / 100) * premium;
        shipCharge = ((totalAmt / 100) * shipPer) + totalAmt;

        txtRghTotalAmount.setText(String.format("%s", Math.round(totalAmt * 100.00) / 100.00));
        txtFinalRate.setText(String.format("%s", Math.round(finaRate * 100.00) / 100.00));
        if (edPer.getString().toString().trim() == "") {
            txtTotalShipAmt.setText(String.format("%s", Math.round(0.0 * 100.00) / 100.00));
        } else {
            txtTotalShipAmt.setText(String.format("%s", Math.round(shipCharge * 100.00) / 100.00));
        }
    }

    private void actionListenersPL() {

        edplWeight.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                calculateNsetTextPL();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        edplRate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                calculateNsetTextPL();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        ckbIsShipping.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (ckbIsShipping.isChecked()) {
                    cvShipPer.setVisibility(View.VISIBLE);
                    llShippingAmount.setVisibility(View.VISIBLE);
                    if (edPer.equals(""))
                        showToast("Shipping Percentage can't be Blank", true);
                    else {
                        edPer.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                            }

                            @Override
                            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                                shipingChargesValueChange();
                            }

                            @Override
                            public void afterTextChanged(Editable editable) {

                            }
                        });
                    }
                } else {
                    cvShipPer.setVisibility(View.GONE);
                    llShippingAmount.setVisibility(View.GONE);

                }
            }
        });
    }

    private void calculateNsetTextPL() {
        plweight = edplWeight.getDouble();
        plrate = edplRate.getDouble();
        plshipPer = edPer.getDouble();

        pltotalAmt = plweight * plrate;
        plshipCharge = ((pltotalAmt / 100) * plshipPer) + pltotalAmt;

        txtplTotalAmt.setText(String.format("%s", Math.round(pltotalAmt * 100.00) / 100.00));
        if (edPer.getString().isEmpty()) {
            txtTotalShipAmt.setText(String.format("%s", Math.round(0.0 * 100.00) / 100.00));
        } else {
            txtTotalShipAmt.setText(String.format("%s", Math.round(plshipCharge * 100.00) / 100.00));
        }
    }

    private void openDialog() {
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_add_stock_item, null);
        dialog = Utils.customDialog(getActivity(), v);
        findViews(v);
        actionListenersPL();
        actionListeners();
        dialog.show();
    }

    private void findViews(View v) {
        txtType = v.findViewById(R.id.txtType);
        switchRoughToPolished = v.findViewById(R.id.switchRoughToPolished);
        switch$ToINR = v.findViewById(R.id.switch$ToINR);
        edItemName = v.findViewById(R.id.edItemName);
        edWeight = v.findViewById(R.id.edWeight);
        edplWeight = v.findViewById(R.id.edplWeight);
        edCut = v.findViewById(R.id.edCut);
        edRate = v.findViewById(R.id.edRate);
        edplRate = v.findViewById(R.id.edplRate);
        edPremium = v.findViewById(R.id.edPremium);
        txtFinalRate = v.findViewById(R.id.txtFinalRate);
        txtRghTotalAmount = v.findViewById(R.id.txtRghTotalAmount);
        ckbIsShipping = v.findViewById(R.id.ckbIsShipping);
        cvShipPer = v.findViewById(R.id.cvShipPer);
        edPer = v.findViewById(R.id.edPer);
        llShippingAmount = v.findViewById(R.id.llShippingAmount);
        llPolished = v.findViewById(R.id.llPolished);
        llRough = v.findViewById(R.id.llRough);
        txtTotalShipAmt = v.findViewById(R.id.txtTotalShipAmt);
        txtplTotalAmt = v.findViewById(R.id.txtplTotalAmt);
        btnCancel = v.findViewById(R.id.btnCancel);
        btnAdd = v.findViewById(R.id.btnAdd);

        btnCancel.setOnClickListener(this);
        btnAdd.setOnClickListener(this);
        switchRoughToPolished.setOnCheckedChangeListener(this);
        switch$ToINR.setOnCheckedChangeListener(this);
        ckbIsShipping.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnCancel:
                // Handle clicks for btnCancel
                dialog.dismiss();
            case R.id.btnAdd:
                // Handle clicks for btnAdd
                if (switchRoughToPolished.isChecked()) {
                    if (edItemName.getString().equals(""))
                        showToast("Enter Item Name", true);
                    else if (edplWeight.getString().equals(""))
                        showToast("Enter Weight", true);
                    else if (edplRate.getString().equals(""))
                        showToast("Enter Rate ", true);
                    else
                        requestApi(1);
                } else {
                    if (edItemName.getString().equals(""))
                        showToast("Enter Item Name", true);
                    else if (edWeight.getString().equals(""))
                        showToast("Enter Weight ", true);
                    else if (edCut.getString().equals(""))
                        showToast("Enter Cut", true);
                    else if (edRate.getString().equals(""))
                        showToast("Enter Rate ", true);
                    else if (edPremium.getString().equals(""))
                        showToast("Enter Premium", true);
                    else
                        requestApi(2);
                }

        }
    }

    private void requestApi(int stockType) {
        if (ckbIsShipping.isChecked() && edPer.getString().equals(""))
            showToast("Enter Shipping Percentage..", true);
        else if (!Utils.isInternetConnected(getActivity()))
            showNoInternetDialog();
        else {
            showProgress();
            AddMyStockRequest request = new AddMyStockRequest();
            request.setAPIKey(Constants.API_KEY);
            request.setToken(Utils.getToken(getActivity()));
            request.setID(0);
            request.setBizTypeID(stockType);
            request.setCurrencyID(switch$ToINR.isChecked() ? 1 : 2);
            request.setItemName(edItemName.getString());
            request.setCrt(switchRoughToPolished.isChecked() ? edplWeight.getDouble() : edWeight.getDouble());
            request.setPricePerCrt(switchRoughToPolished.isChecked() ? edplRate.getDouble() : edRate.getDouble());
            request.setCut(edCut.getString());
            request.setPremiumPer(edPremium.getDouble());
            request.setIsShippingForIOS(-1);
            request.setIsShipping(ckbIsShipping.isChecked());
            request.setShipPer(ckbIsShipping.isChecked() ? edPer.getDouble() : 00);

            requestAPI.postAddMyStockRequest(request).enqueue(getAddStockResponseCallback);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton c, boolean b) {
        switch (c.getId()) {
            case R.id.switch$ToINR:
                changeDollerToRupee();
                break;
            case R.id.switchRoughToPolished:
                shipingChargesValueChange();
                break;
        }

    }

    private void changeDollerToRupee() {
        if (switch$ToINR.isChecked()) {
            txtType.setText("₹");
            if (switchRoughToPolished.isChecked())
                edplRate.setHint("Rate (₹/Crt)");
            else
                edRate.setHint("Rate (₹/Crt)");
        } else if (!switch$ToINR.isChecked()) {
            txtType.setText("$");
            if (switchRoughToPolished.isChecked())
                edplRate.setHint("Rate ($/Crt)");
            else
                edRate.setHint("Rate ($/Crt)");
        }
    }

  /*  @OnClick(R.id.cancel)
    public void onClick() {
        llSearchBar.setVisibility(View.GONE);
        edSearchBar.setText("");
    }*/
}