package com.oozeetech.bizdesk.fragment.drawer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.AlertDialog;
import android.text.method.KeyListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.oozeetech.bizdesk.BaseFragment;
import com.oozeetech.bizdesk.R;
import com.oozeetech.bizdesk.models.CommonResponse;
import com.oozeetech.bizdesk.models.changepassword.ChangePasswordRequest;
import com.oozeetech.bizdesk.models.login.LoginRegisterResponse;
import com.oozeetech.bizdesk.models.updatemobile.UpdateMobileRequest;
import com.oozeetech.bizdesk.models.updateprofile.UpdateProfileRequest;
import com.oozeetech.bizdesk.ui.drawer.DrawerActivity;
import com.oozeetech.bizdesk.utils.Constants;
import com.oozeetech.bizdesk.utils.Utils;
import com.oozeetech.bizdesk.widget.DButton;
import com.oozeetech.bizdesk.widget.DButtonMaterial;
import com.oozeetech.bizdesk.widget.DEditText;
import com.oozeetech.bizdesk.widget.DTextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MyProfileFragment extends BaseFragment implements View.OnClickListener {

    AlertDialog dialog;
    Unbinder unbinder;
    @BindView(R.id.edFullName)
    DEditText edFullName;
    @BindView(R.id.imgEditFullName)
    ImageView imgEditFullName;
    @BindView(R.id.txtEmailId)
    DTextView txtEmailId;
    @BindView(R.id.edMobileNumber)
    DEditText edMobileNumber;
    @BindView(R.id.imgEditMobileNumber)
    ImageView imgEditMobileNumber;
    @BindView(R.id.btnChangePassword)
    LinearLayout btnChangePassword;
    @BindView(R.id.btnLogOut)
    DButtonMaterial btnLogOut;
    Callback<CommonResponse> updateProfileResponseCallback = new Callback<CommonResponse>() {
        @Override
        public void onResponse(Call<CommonResponse> call, Response<CommonResponse> response) {
            dismissProgress();
            if (response.isSuccessful()) {
                if (response.body().getReturnCode().equals("1")) {
                    LoginRegisterResponse data = Utils.getLoginDetail(getActivity());
                    data.getData().setFirstName(edFullName.getString());
                    pref.putString(Constants.LOGIN_REGISTER_RESPONSE, gsonUtils.toJson(data));
                    pref.putBoolean(Constants.IS_LOGIN, true);

                    showDialogDone("Biz Desk", "Name Updated Successfully");
                } else if (response.body().getReturnCode().equals("21"))
                    showDialog("", response.body().getReturnMsg(), response.body().getReturnCode());
                else {
                    showDialog("Biz Desk", response.body().getReturnMsg(), response.body().getReturnCode());
                }
//                    pref.putString(Constants.LOGIN_REGISTER_RESPONSE, gsonUtils.toJson(response.body()));
//                    log.LOGE(gsonUtils.toJson(response.body()));
//                    Intent intent = new Intent(Constants.ACTION_UPDATE_PROFILE);
//                    getActivity().sendBroadcast(intent);
            } else {
                log.LOGE("Code: " + response.code() + " Message: " + response.message());
            }
        }

        @Override
        public void onFailure(Call<CommonResponse> call, Throwable t) {
            t.printStackTrace();
            dismissProgress();
        }
    };
    Callback<CommonResponse> changePasswordResponseCallback = new Callback<CommonResponse>() {
        @Override
        public void onResponse(Call<CommonResponse> call, Response<CommonResponse> response) {
            dismissProgress();
            if (response.isSuccessful()) {
                if (response.body().getReturnCode().equals("1"))
                    showDialogDone("Biz Desk", "Password Change Successfully");
                else if (response.body().getReturnCode().equals("21"))
                    showDialog("", response.body().getReturnMsg(), response.body().getReturnCode());
            } else {
                log.LOGE("Code: " + response.code() + " Message: " + response.message());
            }
        }

        @Override
        public void onFailure(Call<CommonResponse> call, Throwable t) {
            t.printStackTrace();
            dismissProgress();
        }
    };
    Callback<CommonResponse> updateMobileNumberResponseCallback = new Callback<CommonResponse>() {
        @Override
        public void onResponse(Call<CommonResponse> call, Response<CommonResponse> response) {
            dismissProgress();
            if (response.isSuccessful()) {
                if (response.body().getReturnCode().equals("1")) {
                    LoginRegisterResponse data = Utils.getLoginDetail(getActivity());
                    data.getData().setMobileNumber(edMobileNumber.getString());
                    pref.putString(Constants.LOGIN_REGISTER_RESPONSE, gsonUtils.toJson(data));
                    pref.putBoolean(Constants.IS_LOGIN, true);
                    showDialogDone("Biz Desk", "Mobile Number Updated Successfully");
                } else if (response.body().getReturnCode().equals("11")) {
                    LoginRegisterResponse data = Utils.getLoginDetail(getActivity());
                    data.getData().setMobileNumber(edMobileNumber.getString());
                    pref.putString(Constants.LOGIN_REGISTER_RESPONSE, gsonUtils.toJson(data));
                    pref.putBoolean(Constants.IS_LOGIN, true);
                    showDialogDone("Biz Desk", response.body().getReturnMsg());
                } else if (response.body().getReturnCode().equals("21"))
                    showDialog("", response.body().getReturnMsg(), response.body().getReturnCode());
                else {
                    showDialog("Biz Desk", response.body().getReturnMsg(), response.body().getReturnCode());
                }
            } else {
                log.LOGE("Code: " + response.code() + " Message: " + response.message());
            }
        }

        @Override
        public void onFailure(Call<CommonResponse> call, Throwable t) {
            t.printStackTrace();
            dismissProgress();
        }
    };
    private DEditText edOldPassword;
    private DEditText edNewPassword;
    private DEditText edConfirmPassword;
    private DButton btnUpdatePassword;
    private DButton btnCancel;

    public MyProfileFragment() {
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
    }

    @Override
    protected int getFragmentLayout() {
        return R.layout.fragment_my_profile;
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
        if (pref.getBoolean(Constants.IS_SOCIAL_LOGIN, false)) {
            edMobileNumber.setVisibility(View.GONE);
            imgEditMobileNumber.setVisibility(View.GONE);
        }
        setProfile();
        setEditable();
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void setEditable() {
        edFullName.setTag(edFullName.getKeyListener());
        edMobileNumber.setTag(edMobileNumber.getKeyListener());
        edFullName.setKeyListener(null);
        edMobileNumber.setKeyListener(null);
    }

    private void setProfile() {
        LoginRegisterResponse.Data data = Utils.getLoginDetail(getContext()).getData();
        edFullName.setText(data.getFirstName());
        txtEmailId.setText(data.getEmailID());
        edMobileNumber.setText(data.getMobileNumber());
    }

    private void updateProfile() {

        if (!Utils.isInternetConnected(getActivity())) {
            showNoInternetDialog();
        } else {
            showProgress();
            UpdateProfileRequest request = new UpdateProfileRequest();
            request.setAPIKey(Constants.API_KEY);
            request.setToken(Utils.getToken(getActivity()));
            request.setFirstName(edFullName.getString());
            request.setMobileNumber(edMobileNumber.getString());
            requestAPI.postUpdateProfileRequest(request).enqueue(updateProfileResponseCallback);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }


    @OnClick({R.id.imgEditFullName, R.id.imgEditMobileNumber, R.id.btnChangePassword, R.id.btnLogOut})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.imgEditFullName:
                if (edFullName.getKeyListener() == null) {
                    imgEditFullName.setImageResource(R.drawable.ic_check);
                    edFullName.setKeyListener((KeyListener) edFullName.getTag());
                    edFullName.requestFocus();
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(edFullName, InputMethodManager.SHOW_IMPLICIT);
                } else {
                    if (edFullName.getString().isEmpty())
                        showToast("Please Enter Your Name", true);
                    else if (!Utils.isInternetConnected(getActivity()))
                        showNoInternetDialog();
                    else {
                        updateProfile();
                        imgEditFullName.setImageResource(R.drawable.ic_pencil);
                        edFullName.setTag(edFullName.getKeyListener());
                        edFullName.setKeyListener(null);
                    }
                }
                break;

            case R.id.imgEditMobileNumber:
                if (edMobileNumber.getKeyListener() == null) {
                    imgEditMobileNumber.setImageResource(R.drawable.ic_check);
                    edMobileNumber.setKeyListener((KeyListener) edMobileNumber.getTag());
                    edMobileNumber.requestFocus();
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(edMobileNumber, InputMethodManager.SHOW_IMPLICIT);
                } else {
                    if (edMobileNumber.getString().isEmpty())
                        showToast("Please Enter Your Mobile Number", true);
                    else if (!Utils.isInternetConnected(getActivity())) {
                        showNoInternetDialog();
                    } else {
                        updateMobileNumber();
                        imgEditMobileNumber.setImageResource(R.drawable.ic_pencil);
                        edMobileNumber.setTag(edMobileNumber.getKeyListener());
                        edMobileNumber.setKeyListener(null);
                    }
                }
                break;
            case R.id.btnChangePassword:
                changePassword();
                break;
            case R.id.btnLogOut:
                confirmLogout();
                break;
        }
    }

    private void updateMobileNumber() {
        if (!Utils.isInternetConnected(getActivity()))
            showNoInternetDialog();
        else {
            showProgress();
            UpdateMobileRequest request = new UpdateMobileRequest();
            request.setAPIKey(Constants.API_KEY);
            request.setToken(Utils.getToken(getActivity()));
            request.setMobileNumber(edMobileNumber.getString());

            requestAPI.postUpdateMobileRequest(request).enqueue(updateMobileNumberResponseCallback);
        }
    }

    private void changePassword() {
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_change_password, null);
        dialog = Utils.customDialog(getActivity(), v);
        findViews(v);
        dialog.show();
    }

    private void findViews(View v) {
        edOldPassword = v.findViewById(R.id.edOldPassword);
        edNewPassword = v.findViewById(R.id.edNewPassword);
        edConfirmPassword = v.findViewById(R.id.edConfirmPassword);
        btnCancel = v.findViewById(R.id.btnCancel);
        btnUpdatePassword = v.findViewById(R.id.btnUpdatePassword);

        btnCancel.setOnClickListener(this);
        btnUpdatePassword.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == btnCancel) {
            // Handle clicks for btnCancel
            dialog.dismiss();
        } else if (v == btnUpdatePassword) {
            String oldPassword = edOldPassword.getString();
            String newPassword = edNewPassword.getString();
            String conPassword = edConfirmPassword.getString();

            if (oldPassword.isEmpty())
                showToast("Please Enter Old Password", true);
            else if (newPassword.isEmpty())
                showToast("Please Enter New Password", true);
            else if (!conPassword.equals(newPassword))
                showToast("Password Does not Match", true);
            else if (!Utils.isInternetConnected(getActivity())) {
                showNoInternetDialog();
            } else {
                showProgress();
                ChangePasswordRequest request = new ChangePasswordRequest();
                request.setAPIKey(Constants.API_KEY);
                request.setToken(Utils.getToken(getActivity()));
                request.setOldPassword(oldPassword);
                request.setNewPassword(newPassword);

                requestAPI.postChangePasswordRequest(request).enqueue(changePasswordResponseCallback);
            }
        }
    }
}