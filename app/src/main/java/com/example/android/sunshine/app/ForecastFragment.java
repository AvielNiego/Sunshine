package com.example.android.sunshine.app;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.android.sunshine.app.data.WeatherContract;
import com.example.android.sunshine.app.sync.SunshineSyncAdapter;

public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>
{
    private static final String[] FORECAST_COLUMNS = {
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.LocationEntry.COLUMN_COORD_LAT,
            WeatherContract.LocationEntry.COLUMN_COORD_LONG};

    static final int COL_WEATHER_ID = 0;

    static final         int COL_WEATHER_DATE         = 1;
    static final         int COL_WEATHER_DESC         = 2;
    static final         int COL_WEATHER_MAX_TEMP     = 3;
    static final         int COL_WEATHER_MIN_TEMP     = 4;
    static final         int COL_LOCATION_SETTING     = 5;
    static final         int COL_WEATHER_CONDITION_ID = 6;
    static final         int COL_COORD_LAT            = 7;
    static final         int COL_COORD_LONG           = 8;

    private static final int FORECAST_LOADER          = 0;

    private static final String SELECTED_KEY = "ITEM_SELECTED_POSITION";

    private ForecastAdapter forecastAdapter;
    private ListView        listviewForecast;
    private int itemSelectedPosition = ListView.INVALID_POSITION;
    private boolean mUseTodayLayout;

    public ForecastFragment()
    {
    }


    @Override
    public void onCreate(Bundle savesInstanceState)
    {
        super.onCreate(savesInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(FORECAST_LOADER, null, this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int selectedItemID = item.getItemId();

        switch (selectedItemID)
        {
            case R.id.action_refresh:
                updateWeather();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateWeather()
    {
        //        Intent intent = new Intent(getActivity(), SunshineService.class);
        //        intent.putExtra(SunshineService.LOCATION_QUERY_EXTRA, Utility.getPreferredLocation(getActivity()));
        //        getActivity().startService(intent);

//        Intent intent = new Intent(getActivity(), SunshineService.AlarmReceiver.class);
//        intent.putExtra(SunshineService.LOCATION_QUERY_EXTRA, Utility.getPreferredLocation(getActivity()));
//        PendingIntent alarmIntent = PendingIntent.getBroadcast(getActivity(), 0, intent, PendingIntent.FLAG_ONE_SHOT);
//
//        AlarmManager alarmManager = ((AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE));
//        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 1000 * 5, alarmIntent);

        SunshineSyncAdapter.syncImmediately(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        forecastAdapter = new ForecastAdapter(getActivity(), null, 0);
        forecastAdapter.setUseTodayLayout(mUseTodayLayout);

        listviewForecast = (ListView) rootView.findViewById(R.id.listview_forecast);
        listviewForecast.setAdapter(forecastAdapter);
        listviewForecast.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                ForecastFragment.this.itemSelectedPosition = position;

                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                if (cursor != null)
                {
                    String prefLocation = Utility.getPreferredLocation(getActivity());
                    ((Callback) getActivity()).onItemSelected(WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                            prefLocation,
                            cursor.getLong(COL_WEATHER_DATE)));
                }
            }
        });

        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY))
        {
            itemSelectedPosition = savedInstanceState.getInt(SELECTED_KEY);
        }

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        if (itemSelectedPosition != ListView.INVALID_POSITION)
        {
            outState.putInt(SELECTED_KEY, itemSelectedPosition);
        }
        super.onSaveInstanceState(outState);
    }

    public void onLocationChanged()
    {
        updateWeather();
        getLoaderManager().restartLoader(FORECAST_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args)
    {
        String prefLocation = Utility.getPreferredLocation(getActivity());
        Uri uri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(prefLocation, System.currentTimeMillis());
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";

        return new CursorLoader(getActivity(), uri, FORECAST_COLUMNS, null, null, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data)
    {
        forecastAdapter.swapCursor(data);
        if (itemSelectedPosition != ListView.INVALID_POSITION)
        {
            listviewForecast.smoothScrollToPosition(itemSelectedPosition);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader)
    {
        forecastAdapter.swapCursor(null);
    }

    public void setUseTodayLayout(boolean useTodayLayout)
    {
        mUseTodayLayout = useTodayLayout;
        if (forecastAdapter != null)
        {
            forecastAdapter.setUseTodayLayout(useTodayLayout);
        }
    }

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback
    {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(Uri dateUri);
    }
}