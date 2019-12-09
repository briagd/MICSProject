package uni.lu.mics.mics_project.nmbd.app.service.location;

import android.content.Context;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.osmdroid.util.GeoPoint;

import static org.junit.Assert.*;

public class LocationUtilsTest {
    Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();

    @Test
    public void getLocationFromAddress() {
        //Test retrieval of geo coordinate
        String address = "Maison du Nombre, 6 Avenue de la Fonte, 4364 Esch-sur-Alzette, Luxembourg";
        //49.503925,5.947803

        GeoPoint geoPoint = LocationUtils.getLocationFromAddress(context, address);
        assertEquals("lat", geoPoint.getLatitude(), 49.503925, 0.001);
        assertEquals("lat", geoPoint.getLongitude(), 5.947803, 0.001);

        //test vague address
        address = "Paris, France";
        //48.8546,2.34771
        geoPoint = LocationUtils.getLocationFromAddress(context, address);
        assertEquals("lat", geoPoint.getLatitude(), 48.8546, 0.01);
        assertEquals("lat", geoPoint.getLongitude(), 2.34771,0.01);

        //test for unknown location
        address = "fwbiubfiwqbiebifeqibewqf";
        geoPoint = LocationUtils.getLocationFromAddress(context, address);
        assertNull(geoPoint);

        //test for address in japanese
        address = "〒600-8008 京都府京都市下京区長刀鉾町";
        geoPoint = LocationUtils.getLocationFromAddress(context, address);
        assertEquals("lat", geoPoint.getLatitude(), 35.0037, 0.01);
        assertEquals("lat", geoPoint.getLongitude(), 135.76,0.01);

    }
}