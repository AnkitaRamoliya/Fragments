package com.oozeetech.bizdesk.fragment;

import android.os.Bundle;
import android.view.View;

import com.oozeetech.bizdesk.BaseFragment;
import com.oozeetech.bizdesk.R;


public class BlankFragment extends BaseFragment {


    public BlankFragment() {
        // Required empty public constructor
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }


    @Override
    protected int getFragmentLayout() {
        return R.layout.fragment_blank;
    }


    @Override
    public void onDetach() {
        super.onDetach();
    }

}
