package sanfranmotopark.cansave.us;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by kembp on 8/4/14.
 */
public class ParkingLocationDataSource {
    private SQLiteDatabase database;
    private DataBaseHelper dbHelper;
    private String[] allColumns = {DataBaseHelper.COLUMN_ID, DataBaseHelper.COLUMN_LAT,
            DataBaseHelper.COLUMN_LONG, DataBaseHelper.COLUMN_METER_TYPE, DataBaseHelper.COLUMN_RATEAREA,
            DataBaseHelper.COLUMN_SMART_METE, DataBaseHelper.COLUMN_STREET_NUM, DataBaseHelper.COLUMN_STREET_NAME};

    public ParkingLocationDataSource(Context context) {
        dbHelper = new DataBaseHelper(context);
    }

    public void open() throws SQLException, IOException {
        dbHelper.createDataBase();
        dbHelper.openDataBase();
        database = dbHelper.getReadableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public List<ParkingLocation> getAllLocations() {
        List<ParkingLocation> locations = new ArrayList<ParkingLocation>();
        Cursor cursor = database.query(DataBaseHelper.TABLE_LOCATIONS, allColumns, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            ParkingLocation location = cursorToLocation(cursor);
            locations.add(location);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();

        return locations;
    }

    private ParkingLocation cursorToLocation(Cursor c) {
        ParkingLocation location = new ParkingLocation();
        location.setId(c.getInt(c.getColumnIndex(DataBaseHelper.COLUMN_ID)));
        location.setLatLng(c.getFloat(c.getColumnIndex(DataBaseHelper.COLUMN_LAT)), c.getFloat(c.getColumnIndex(DataBaseHelper.COLUMN_LONG)));
        location.setArea(c.getString(c.getColumnIndex(DataBaseHelper.COLUMN_RATEAREA)));
        location.setMultiSpace(c.getString(c.getColumnIndex(DataBaseHelper.COLUMN_METER_TYPE)));
        location.setSmartMeter(c.getString(c.getColumnIndex(DataBaseHelper.COLUMN_SMART_METE)));
        location.setAddress(c.getString(c.getColumnIndex(DataBaseHelper.COLUMN_STREET_NUM)), c.getString(c.getColumnIndex(DataBaseHelper.COLUMN_STREET_NAME)));

        return location;
    }

    public List<ParkingLocation> getAllLocations(double longitude, double latitude) {
        List<ParkingLocation> locations = new ArrayList<ParkingLocation>();
        return locations;
    }

    public ParkingLocation getClosestLocation(double longitude, double latitude) {
        ParkingLocation location = new ParkingLocation();
        return location;
    }

}
