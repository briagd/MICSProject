package uni.lu.mics.mics_project.nmbd.app.service.location;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import org.osmdroid.util.GeoPoint;

import java.io.IOException;
import java.util.List;

public class LocationUtils{

        public static GeoPoint getLocationFromAddress(Context context, String strAddress){

            Geocoder coder = new Geocoder(context);
            List<Address> address;
            GeoPoint p1 = null;

            try {
                address = coder.getFromLocationName(strAddress,5);
                if (address==null) {
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


}
