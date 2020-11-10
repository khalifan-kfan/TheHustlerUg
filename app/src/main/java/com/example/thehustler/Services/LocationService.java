package com.example.thehustler.Services;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.text.TextUtils;


import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class LocationService  extends IntentService {
    ResultReceiver rs;


    public  LocationService(){
        super("LocationService");
    }



    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        if(intent != null){
            String error="";
            rs=intent.getParcelableExtra(Constants.RECEIVER);
            Location location = intent.getParcelableExtra(Constants.LOCATION_DATA_EXTRA);
            if(location == null){
                return;
            }
            Geocoder geo = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = null;
            try{
                addresses = geo.getFromLocation(location.getLatitude(),location.getLongitude(),1);
            }catch (Exception e){
                error = e.getMessage();
            }
            if(addresses == null || addresses.isEmpty()){
                deliverResultToReciever(Constants.FAILURE_RESULT,error,null);
            }else {
                Address address = addresses.get(0);
                ArrayList<String > adressssFragments = new ArrayList<>();
               // for(int i =0; i<= address.getMaxAddressLineIndex();i++ ){
                 //   adressssFragments.add(address.getAddressLine(i));
                //}
                adressssFragments.add(address.getCountryName());
                adressssFragments.add(address.getLocality());
                adressssFragments.add(address.getCountryCode());
                deliverResultToReciever(Constants.SUCCESS_RESULT,null,adressssFragments);

            }
        }
    }

    private void deliverResultToReciever(int resultCode, String e,ArrayList<String> addressMessage){
        Bundle bundle = new Bundle();
        bundle.putString(Constants.RESULT_ERROR, e);
        bundle.putStringArrayList(Constants.RESULT_DATA_KEY,addressMessage);
        rs.send(resultCode,bundle);
    }
}
