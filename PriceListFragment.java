package com.oozeetech.bizdesk.fragment.drawer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.oozeetech.bizdesk.BaseFragment;
import com.oozeetech.bizdesk.R;
import com.oozeetech.bizdesk.adapter.GetPriceListAdapter;
import com.oozeetech.bizdesk.models.pricelist.GetPriceListRequest;
import com.oozeetech.bizdesk.models.pricelist.GetPriceListResponse;
import com.oozeetech.bizdesk.ui.drawer.DrawerActivity;
import com.oozeetech.bizdesk.utils.Constants;
import com.oozeetech.bizdesk.utils.Utils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class PriceListFragment extends BaseFragment {

    Unbinder unbinder;
    GetPriceListAdapter adapter;
    @BindView(R.id.rclViewGetPriceList)
    RecyclerView rclViewGetPriceList;
    @BindView(R.id.llNoRecordFound)
    LinearLayout llNoRecordFound;

    private Callback<GetPriceListResponse> getPriceListResponseCallback = new Callback<GetPriceListResponse>() {
        @Override
        public void onResponse(Call<GetPriceListResponse> call, Response<GetPriceListResponse> response) {
            dismissProgress();
            if (response.isSuccessful()) {
                if (response.body().getReturnCode().equals("1")) {
                    rclViewGetPriceList.setVisibility(View.VISIBLE);
                    llNoRecordFound.setVisibility(View.GONE);
                    adapter.clear();
                    adapter.addAll(response.body().getData());
                } else if (response.body().getReturnCode().equals("21"))
                    showDialog("", response.body().getReturnMsg(), response.body().getReturnCode());
                else {
                    rclViewGetPriceList.setVisibility(View.GONE);
                    llNoRecordFound.setVisibility(View.VISIBLE);
                }
            } else {
                log.LOGE("Code: " + response.code() + " Message: " + response.message());
            }
        }

        @Override
        public void onFailure(Call<GetPriceListResponse> call, Throwable t) {
            t.printStackTrace();
            log.LOGE("Failure Response");
            dismissProgress();
        }
    };

    public PriceListFragment() {
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

    private void callGetPriceListAPI() {
        if (!Utils.isInternetConnected(getActivity()))
            showNoInternetDialog();
        else {
            GetPriceListRequest request = new GetPriceListRequest();
            request.setAPIKey(Constants.API_KEY);
            request.setToken(Utils.getToken(getActivity()));
            request.setPageIndex(-1);
            request.setSearchString("");

            requestAPI.postGetPriceListRequest(request).enqueue(getPriceListResponseCallback);
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    protected int getFragmentLayout() {
        return R.layout.fragment_price_list;
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
        initRecylerView();
        callGetPriceListAPI();
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void initRecylerView() {
        adapter = new GetPriceListAdapter(getActivity());
        rclViewGetPriceList.setLayoutManager(new LinearLayoutManager(getActivity()));
        rclViewGetPriceList.setHasFixedSize(true);
        rclViewGetPriceList.setAdapter(adapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

}