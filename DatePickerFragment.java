package com.oozeetech.bizdesk.fragment.drawer.filterfragment;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.oozeetech.bizdesk.BaseFragment;
import com.oozeetech.bizdesk.R;
import com.oozeetech.bizdesk.utils.Constants;
import com.oozeetech.bizdesk.widget.DTextView;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static android.app.Activity.RESULT_OK;

public class DatePickerFragment extends BaseFragment implements DatePickerDialog.OnDateSetListener {
    public static String sellerFromDate = "", sellerToDate = "", paymentFromDate = "", paymentToDate = "";
    @BindView(R.id.txtSellerFromDate)
    DTextView txtSellerFromDate;
    @BindView(R.id.txtSellerToDate)
    DTextView txtSellerToDate;
    @BindView(R.id.txtPaymentFromDate)
    DTextView txtPaymentFromDate;
    @BindView(R.id.txtPaymentToDate)
    DTextView txtPaymentToDate;
    Unbinder unbinder;
    Calendar now = Calendar.getInstance();

    DatePickerDialog dpd = DatePickerDialog.newInstance(this,
            now.get(Calendar.YEAR),
            now.get(Calendar.MONTH),
            now.get(Calendar.DAY_OF_MONTH)
    );

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // TODO: inflate a fragment view
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        unbinder = ButterKnife.bind(this, rootView);
        Drawable drawable = DrawableCompat.wrap(getResources().getDrawable(R.drawable.ic_date_));
        DrawableCompat.setTint(drawable, getResources().getColor(R.color.colorTextBlack));
        txtPaymentFromDate.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null);
        txtPaymentToDate.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null);
        txtSellerFromDate.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null);
        txtSellerToDate.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null);
        return rootView;
    }

    @Override
    protected int getFragmentLayout() {
        return R.layout.fragment_date_picker;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick({R.id.txtSellerFromDate, R.id.txtSellerToDate, R.id.txtPaymentFromDate, R.id.txtPaymentToDate})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.txtSellerFromDate:
                if (!dpd.isAdded())
                    dpd.show(getActivity().getFragmentManager(), "SellerFromDateDialog");
                break;
            case R.id.txtSellerToDate:
                if (!dpd.isAdded())
                    dpd.show(getActivity().getFragmentManager(), "SellerToDateDialog");
                break;
            case R.id.txtPaymentFromDate:
                if (!dpd.isAdded())
                    dpd.show(getActivity().getFragmentManager(), "PaymentFromDateDialog");
                break;
            case R.id.txtPaymentToDate:
                if (!dpd.isAdded())
                    dpd.show(getActivity().getFragmentManager(), "PaymentToDateDialog");
                break;

        }
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {

        if (view.getTag().equals("SellerFromDateDialog")) {
            sellerFromDate = String.format("%02d/%02d/%d", dayOfMonth, monthOfYear + 1, year);
            txtSellerFromDate.setText(sellerFromDate);
        } else if (view.getTag().equals("SellerToDateDialog")) {
            sellerToDate = String.format("%02d/%02d/%d", dayOfMonth, monthOfYear + 1, year);
            txtSellerToDate.setText(sellerToDate);
        } else if (view.getTag().equals("PaymentFromDateDialog")) {
            paymentFromDate = String.format("%02d/%02d/%d", dayOfMonth, monthOfYear + 1, year);
            txtPaymentFromDate.setText(paymentFromDate);
        } else if (view.getTag().equals("PaymentToDateDialog")) {
            paymentToDate = String.format("%02d/%02d/%d", dayOfMonth, monthOfYear + 1, year);
            txtPaymentToDate.setText(paymentToDate);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK)
            return;
        if (requestCode == RESULT_OK) {
            data.getStringExtra(Constants.PARTY_IDS);
            data.getStringExtra(Constants.CUSTOMER_IDS);
            sellerFromDate = data.getStringExtra(Constants.SELLER_FROM_DATE);
            sellerToDate = data.getStringExtra(Constants.SELLER_TO_DATE);
            paymentFromDate = data.getStringExtra(Constants.PAYMENT_FROM_DATE);
            paymentToDate = data.getStringExtra(Constants.PAYMENT_TO_DATE);
        }
    }
}