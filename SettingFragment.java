package com.oozeetech.bizdesk.fragment.drawer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.oozeetech.bizdesk.BaseFragment;
import com.oozeetech.bizdesk.R;
import com.oozeetech.bizdesk.models.setting.GetSettingResponse;
import com.oozeetech.bizdesk.models.setting.SetSettingRequest;
import com.oozeetech.bizdesk.models.setting.SetSettingResponse;
import com.oozeetech.bizdesk.ui.drawer.DrawerActivity;
import com.oozeetech.bizdesk.utils.Constants;
import com.oozeetech.bizdesk.utils.Utils;
import com.oozeetech.bizdesk.widget.DButton;
import com.oozeetech.bizdesk.widget.DCheckBox;
import com.oozeetech.bizdesk.widget.DEditText;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class SettingFragment extends BaseFragment {


    @BindView(R.id.edRoughBrokeragePer)
    DEditText edRoughBrokeragePer;
    @BindView(R.id.edPolishBokeragePer)
    DEditText edPolishBokeragePer;
    @BindView(R.id.edExchangeRate)
    DEditText edExchangeRate;
    @BindView(R.id.ckbNotifyPaymentDue)
    DCheckBox ckbNotifyPaymentDue;
    @BindView(R.id.ckbNotifyBizConfirm)
    DCheckBox ckbNotifyBizConfirm;
    @BindView(R.id.btnSave)
    DButton btnSave;
    @BindView(R.id.activity_main)
    LinearLayout activityMain;
    Unbinder unbinder;
    @BindView(R.id.edNotifyConfirmBizDay)
    DEditText edNotifyConfirmBizDay;
    @BindView(R.id.ckbNotifyUpdates)
    DCheckBox ckbNotifyUpdates;
    Callback<SetSettingResponse> setSettingResponseCallback = new Callback<SetSettingResponse>() {
        @Override
        public void onResponse(Call<SetSettingResponse> call, Response<SetSettingResponse> response) {
            dismissProgress();
            if (response.isSuccessful()) {
                if (response.body().getReturnCode().equals("1")) {
                    SetSettingResponse.Data data = response.body().getData();
                    setSettingResponse(data);
                    changePrefData();
                } else if (response.body().getReturnCode().equals("21"))
                    showDialog("", response.body().getReturnMsg(), response.body().getReturnCode());

            } else {
                log.LOGE("Code: " + response.code() + " Message: " + response.message());
            }
        }

        @Override
        public void onFailure(Call<SetSettingResponse> call, Throwable t) {
            t.printStackTrace();
            dismissProgress();
        }
    };
    Callback<GetSettingResponse> getSettingResponseCallback = new Callback<GetSettingResponse>() {
        @Override
        public void onResponse(Call<GetSettingResponse> call, Response<GetSettingResponse> response) {
            dismissProgress();
            if (response.isSuccessful()) {
                if (response.body().getReturnCode().equals("1")) {

                    GetSettingResponse.Data data = response.body().getData();
                    getSettingResponse(data);

                } else
                    showDialog("", response.body().getReturnMsg(), response.body().getReturnCode());
            } else {
                log.LOGE("Code: " + response.code() + " Message: " + response.message());
            }
        }

        @Override
        public void onFailure(Call<GetSettingResponse> call, Throwable t) {
            t.printStackTrace();
            dismissProgress();
        }
    };

    public SettingFragment() {
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

    private void setSettingResponse(SetSettingResponse.Data data) {

    }

    private void getSettingResponse(GetSettingResponse.Data data) {

        edRoughBrokeragePer.setText(data.getRoughBrokeragePer());
        edPolishBokeragePer.setText(data.getPolishBrokeragePer());
        edExchangeRate.setText(data.getExchangeRate());
        ckbNotifyPaymentDue.setChecked(data.isNotifyPaymentDue());
        ckbNotifyBizConfirm.setChecked(data.isNotifyBizConfirm());
        edNotifyConfirmBizDay.setText(data.getNotifyBizConfirmDay());
        ckbNotifyUpdates.setChecked(data.isNotifyUpdates());

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    protected int getFragmentLayout() {
        return R.layout.activity_setting;
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

        setHasOptionsMenu(true);
        getSetting();
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void getSetting() {

        edRoughBrokeragePer.setText(pref.getString(Constants.ROUGH_BROKERAGE));
        edPolishBokeragePer.setText(pref.getString(Constants.POLISH_BROKERAGE));
        edExchangeRate.setText(pref.getString(Constants.EXCHANGE_RATE));
        edNotifyConfirmBizDay.setText(pref.getString(Constants.NOTIFY_BIZ_CONFIRM_DAY));
        ckbNotifyBizConfirm.setChecked(pref.getBoolean(Constants.NOTIFY_BIZ_CONFIRM, false));
        ckbNotifyPaymentDue.setChecked(pref.getBoolean(Constants.NOTIFY_PAYMENT_DUE, false));
        ckbNotifyUpdates.setChecked(pref.getBoolean(Constants.NOTIFY_UPDATES, false));

//        showProgress();
//        GetSettingRequest request = new GetSettingRequest();
//        request.setAPIKey(Constants.API_KEY);
//        request.setToken(Utils.getToken(getActivity()));
//        requestAPI.postGetSettingRequest(request).enqueue(getSettingResponseCallback);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick({R.id.ckbNotifyPaymentDue, R.id.ckbNotifyBizConfirm, R.id.ckbNotifyUpdates, R.id.btnSave})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ckbNotifyPaymentDue:
                ckbNotifyPaymentDue.setChecked(ckbNotifyPaymentDue.isChecked());
                break;
            case R.id.ckbNotifyBizConfirm:
                ckbNotifyBizConfirm.setChecked(ckbNotifyBizConfirm.isChecked());
                break;
            case R.id.ckbNotifyUpdates:
                ckbNotifyUpdates.setChecked(ckbNotifyUpdates.isChecked());
                break;
            case R.id.btnSave:
                setSettingRequest();
                break;
        }
    }

    private void setSettingRequest() {

        if (edRoughBrokeragePer.getDouble() < 0)
            showToast("Enter Rough Brokerage Percentage", true);
        else if (edPolishBokeragePer.getDouble() < 0)
            showToast("Enter Polish Brokerage Percentage", true);
        else if (edExchangeRate.getDouble() < 0)
            showToast("Enter Exchange Rate", true);
        else if (edNotifyConfirmBizDay.getInt() < 0)
            showToast("Enter Confirm Biz Day", true);
        else if (!Utils.isInternetConnected(getActivity())) {
            showNoInternetDialog();
        } else {
            showProgress();
            SetSettingRequest request = new SetSettingRequest();
            request.setAPIKey(Constants.API_KEY);
            request.setToken(Utils.getToken(getActivity()));
            request.setRoughBrokeragePer(edRoughBrokeragePer.getDouble());
            request.setPolishBrokeragePer(edPolishBokeragePer.getDouble());
            request.setExchangeRate(edExchangeRate.getDouble());
            request.setNotifyPaymentDue(ckbNotifyPaymentDue.isChecked() ? 1 : 0);
            request.setNotifyBizConfirm(ckbNotifyBizConfirm.isChecked() ? 1 : 0);
            request.setNotifyBizConfirmDay(edNotifyConfirmBizDay.getInt());
            request.setNotifyUpdates(ckbNotifyUpdates.isChecked() ? 1 : 0);
            requestAPI.postSetSettingRequest(request).enqueue(setSettingResponseCallback);

        }
    }

    private void changePrefData() {
        pref.putString(Constants.ROUGH_BROKERAGE, edRoughBrokeragePer.getString());
        pref.putString(Constants.POLISH_BROKERAGE, edPolishBokeragePer.getString());
        pref.putString(Constants.EXCHANGE_RATE, edExchangeRate.getString());
        pref.putString(Constants.NOTIFY_BIZ_CONFIRM_DAY, edNotifyConfirmBizDay.getString());
        pref.putBoolean(Constants.NOTIFY_PAYMENT_DUE, ckbNotifyPaymentDue.isChecked());
        pref.putBoolean(Constants.NOTIFY_BIZ_CONFIRM, ckbNotifyBizConfirm.isChecked());
        pref.putBoolean(Constants.NOTIFY_UPDATES, ckbNotifyUpdates.isChecked());
    }
}
