package com.oozeetech.bizdesk.fragment.drawer.myparty;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.oozeetech.bizdesk.BaseFragment;
import com.oozeetech.bizdesk.R;
import com.oozeetech.bizdesk.adapter.GetPartyAdapter;
import com.oozeetech.bizdesk.fragment.drawer.HomeFragment;
import com.oozeetech.bizdesk.listener.PartyListener;
import com.oozeetech.bizdesk.models.CommonResponse;
import com.oozeetech.bizdesk.models.party.AddNewPartyRequest;
import com.oozeetech.bizdesk.models.party.GetPartyRequest;
import com.oozeetech.bizdesk.models.party.GetPartyResponse;
import com.oozeetech.bizdesk.ui.drawer.DrawerActivity;
import com.oozeetech.bizdesk.utils.Constants;
import com.oozeetech.bizdesk.utils.Utils;
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

import static android.app.Activity.RESULT_OK;

public class MyPartyFragment extends BaseFragment implements View.OnClickListener {

    final int RQS_PICK_CONTACT = 1;
    @BindView(R.id.fabAddParty)
    FloatingActionButton fabAddParty;
    AlertDialog dialog;
    GetPartyAdapter adapter;
    @BindView(R.id.rclViewGetParty)
    RecyclerView rvGetParty;
    Unbinder unbinder;
    int editPosition = -1, callPosition = -1;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.llNoRecordFound)
    LinearLayout llNoRecordFound;
    @BindView(R.id.llData)
    LinearLayout llData;
    private DTextView txtTitle;
    private ImageView imgContactBook;
    private DEditText edPartyName;
    private DEditText edCompanyName;
    private DEditText edCustomerNo1;
    private DEditText edCustomerNo2;
    private DButtonMaterial btnCancel;
    private DButtonMaterial btnAdd;
    PartyListener partyListener = new PartyListener() {
        @Override
        public void onPartyEditClickListener(int position) {
            editPosition = position;
            openDialog();
            txtTitle.setText("Edit Party");
            btnAdd.setText("Update");
            edPartyName.setText(adapter.get(position).getName());
            edCompanyName.setText(adapter.get(position).getCompanyName());
            edCustomerNo1.setText(adapter.get(position).getContact1());
            edCustomerNo2.setText(adapter.get(position).getContact2());

        }

        @Override
        public void onCallClickListener(int position) {
            if (!checkPermissionForCallPhone()) {
                requestPermissionForCallPhone();
            } else {
                callOnNumber(adapter.get(position).getContact1());
                log.LOGE("ActionCall");

            }
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
    private Callback<CommonResponse> commonResponseCallback = new Callback<CommonResponse>() {
        @Override
        public void onResponse(Call<CommonResponse> call, Response<CommonResponse> response) {
            dismissProgress();
            if (response.isSuccessful()) {
                if (response.body().getReturnCode().equals("1")) {
                    llData.setVisibility(View.VISIBLE);
                    llNoRecordFound.setVisibility(View.GONE);
                    callGetPartyAPI();
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
        public void onFailure(Call<CommonResponse> call, Throwable t) {
            t.printStackTrace();
            log.LOGE("Failure Response");
            dismissProgress();
        }
    };

    private void callOnNumber(String number) {
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + number));
        startActivity(callIntent);
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // TODO: inflate a fragment view
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
//        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
        unbinder = ButterKnife.bind(this, rootView);
//        ButterKnife.bind(this, rootView);
        toolbar.setVisibility(View.GONE);
        setHasOptionsMenu(true);
        initRecyclerView();
        callGetPartyAPI();

        return rootView;
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
    protected int getFragmentLayout() {
        return R.layout.activity_search_party;
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
        adapter = new GetPartyAdapter(getActivity(), partyListener);
        rvGetParty.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvGetParty.setHasFixedSize(true);
        rvGetParty.setAdapter(adapter);

    }

    @OnClick(R.id.fabAddParty)
    public void onViewClicked() {
        // custom dialog
        openDialog();
    }

    private void openDialog() {
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_add_new_party, null);
        dialog = Utils.customDialog(getActivity(), v);
        findViews(v);
        Utils.setDrawableTint(edPartyName, getResources().getDrawable(R.drawable.ic_myprofile_), getResources().getColor(R.color.colorTextBlack));
        Utils.setDrawableTint(edCompanyName, getResources().getDrawable(R.drawable.ic_building_), getResources().getColor(R.color.colorTextBlack));
        Utils.setDrawableTint(edCustomerNo1, getResources().getDrawable(R.drawable.ic_call_), getResources().getColor(R.color.colorTextBlack));
        Utils.setDrawableTint(edCustomerNo2, getResources().getDrawable(R.drawable.ic_call_), getResources().getColor(R.color.colorTextBlack));
        dialog.show();
    }

    private void findViews(View v) {
        txtTitle = v.findViewById(R.id.txtTitle);
        imgContactBook = v.findViewById(R.id.imgContactBook);
        edPartyName = v.findViewById(R.id.edPartyName);
        edCompanyName = v.findViewById(R.id.edCompanyName);
        edCustomerNo1 = v.findViewById(R.id.edCustomerNo1);
        edCustomerNo2 = v.findViewById(R.id.edCustomerNo2);
        btnCancel = v.findViewById(R.id.btnCancel);
        btnAdd = v.findViewById(R.id.btnAdd);

        btnCancel.setOnClickListener(this);
        btnAdd.setOnClickListener(this);
        imgContactBook.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == btnCancel) {
            // Handle clicks for btnCancel
            dialog.dismiss();
        } else if (v == btnAdd) {
            // Handle clicks for btnAdd
            if (editPosition != -1)
                callAddPartyAPI(adapter.get(editPosition).getID());
            else
                callAddPartyAPI("0");
        } else if (v == imgContactBook) {
            if (!checkPermissionForReadContacts()) {
                requestPermissionForReadContacts();
            } else {
                openContactBook();
            }
        }
    }

    private void openContactBook() {
        final Uri uriContact = ContactsContract.Contacts.CONTENT_URI;
        Intent intentPickContact = new Intent(Intent.ACTION_PICK, uriContact);
        startActivityForResult(intentPickContact, RQS_PICK_CONTACT);
    }

    private void callAddPartyAPI(String partyId) {
        String partyName = edPartyName.getString();
        String companyName = edCompanyName.getString();
        String customerNo1 = edCustomerNo1.getString();
        String customerNo2 = edCustomerNo2.getString();

        if (partyName.isEmpty())
            showToast("Please Enter Name", true);
        else if (companyName.isEmpty())
            showToast("Please Enter Company Name", true);
        else if (customerNo1.isEmpty())
            showToast("Enter Your Contact Number", true);
        else if (!Utils.isInternetConnected(getActivity()))
            showNoInternetDialog();
        else {
            dialog.dismiss();
            showProgress();
            AddNewPartyRequest request = new AddNewPartyRequest();
            request.setAPIKey(Constants.API_KEY);
            request.setToken(Utils.getToken(getActivity()));
            request.setName(partyName);
            request.setCompanyName(companyName);
            request.setContact1(customerNo1);
            request.setContact2(customerNo2);
            request.setID(partyId);

            requestAPI.postAddNewPartyRequest(request).enqueue(commonResponseCallback);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RQS_PICK_CONTACT) {
            if (resultCode == RESULT_OK) {
                Uri contactData = data.getData();

                Cursor cursor = getActivity().getContentResolver().query(contactData, null, null, null, null);
                cursor.moveToFirst();
                String contactId =
                        cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                Cursor phones = getActivity().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId, null, null);
                String contact1 = "", contact2 = "";
                while (phones.moveToNext()) {
                    String number = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    if (contact1.isEmpty())
                        contact1 = number;
                    else if (contact2.isEmpty())
                        contact2 = number;
                }
                phones.close();

                String name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                if (editPosition != -1) {
                    edPartyName.setText(name.isEmpty() ? adapter.get(editPosition).getName() : name);
                    edCustomerNo1.setText(contact1.isEmpty() ? adapter.get(editPosition).getContact1() : contact1);
                    edCustomerNo2.setText(contact2.isEmpty() ? adapter.get(editPosition).getContact2() : contact2);
                } else {
                    edPartyName.setText(name);
                    edCustomerNo1.setText(contact1);
                    edCustomerNo2.setText(contact2);
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case READ_CONTACTS_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openContactBook();
                }
                break;
            case CALL_PHONE_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    callOnNumber(adapter.get(callPosition).getContact1());
                }
                break;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}