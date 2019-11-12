package uni.lu.mics.mics_project.nmbd.app.service.location;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.osmdroid.util.GeoPoint;

import java.io.IOException;
import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

public class LocationUtils{
    //Permission variable
    private static final String[] LOCATION = {Manifest.permission.ACCESS_FINE_LOCATION};
    //GPS variables
    private FusedLocationProviderClient fusedLocationClient;

        public static GeoPoint getLocationFromAddress(Context context, String strAddress){

            Geocoder coder = new Geocoder(context);
            List<Address> address;
            GeoPoint p1 = null;

            try {
                address = coder.getFromLocationName(strAddress,5);
                if (address==null || address.size()==0) {
                    return null;
                }
                Address location=address.get(0);
                location.getLatitude();
                location.getLongitude();

                p1 = new GeoPoint(location.getLatitude(),
                        location.getLongitude());

                return p1;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return p1;
        }
    public static void getLastLocation(Context context, final LocationCallBack locationCallBack){
        //Will be executed if permission has already been granted
        //gets the current location
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);;
        if (hasLocationPermission(context)) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener((Activity) context, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                locationCallBack.onSuccess(location);

                            }
                        }
                    });
        }
    }

    //Permission requests Helper function
    private static boolean hasLocationPermission(Context context) {
        return EasyPermissions.hasPermissions(context, LOCATION);
    }

}
