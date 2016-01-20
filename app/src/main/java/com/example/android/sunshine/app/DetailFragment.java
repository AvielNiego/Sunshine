package com.example.android.sunshine.app;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.sunshine.app.data.WeatherContract;

public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>
{
    private static final String[] DETAIL_COLUMNS = {
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
            WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
            WeatherContract.WeatherEntry.COLUMN_DEGREES,
            WeatherContract.WeatherEntry.COLUMN_PRESSURE,
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.LocationEntry.COLUMN_COORD_LAT,
            WeatherContract.LocationEntry.COLUMN_COORD_LONG};

    static final        int    COL_WEATHER_ID             = 0;
    static final        int    COL_WEATHER_DATE           = 1;
    static final        int    COL_WEATHER_DESC           = 2;
    static final        int    COL_WEATHER_MAX_TEMP       = 3;
    static final        int    COL_WEATHER_MIN_TEMP       = 4;
    static final        int    COL_WEATHER_HUMIDITY       = 5;
    static final        int    COL_WEATHER_WIND_SPEED     = 6;
    static final        int    COL_WEATHER_WIND_DIRECTION = 7;
    static final        int    COL_WEATHER_PRESSURE       = 8;
    static final        int    COL_LOCATION_SETTING       = 9;
    static final        int    COL_WEATHER_CONDITION_ID   = 10;
    static final        int    COL_COORD_LAT              = 11;
    static final        int    COL_COORD_LONG             = 12;
    public static final String DETAIL_URI                 = "DETAIL_URI";

    private TextView  dayView;
    private TextView  dateView;
    private TextView  highView;
    private TextView  lowView;
    private TextView  humidityView;
    private TextView  windView;
    private TextView  pressureView;
    private ImageView iconView;
    private TextView  forecastView;

    private static final String LOG_TAG                = DetailFragment.class.getSimpleName();
    private static final String FORECAST_SHARE_HASHTAG = "#SunshineApp";

    private static final int DETAIL_LOADER = 0;

    private TextView            forecastDetailsTextView;
    private String              shareForecastString;
    private ShareActionProvider shareActionProvider;
    private Uri                 mUri;


    public DetailFragment()
    {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        Bundle args = getArguments();
        if (args != null)
        {
            mUri = args.getParcelable(DetailFragment.DETAIL_URI);
        }

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        dayView = ((TextView) rootView.findViewById(R.id.detail_day_textview));
        dateView = (TextView) rootView.findViewById(R.id.detail_date_textview);
        highView = (TextView) rootView.findViewById(R.id.detail_high_textview);
        lowView = (TextView) rootView.findViewById(R.id.detail_low_textview);
        humidityView = (TextView) rootView.findViewById(R.id.detail_humidity_textview);
        windView = (TextView) rootView.findViewById(R.id.detail_wind_textview);
        pressureView = (TextView) rootView.findViewById(R.id.detail_pressure_textview);
        iconView = (ImageView) rootView.findViewById(R.id.detail_icon);
        forecastView = (TextView) rootView.findViewById(R.id.detail_forecast_textview);

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    private Intent createShareForecastIntent()
    {
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(Intent.EXTRA_TEXT, shareForecastString + FORECAST_SHARE_HASHTAG);
        return sharingIntent;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);

        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.detailfragment, menu);

        MenuItem menuItem = menu.findItem(R.id.menu_item_share);

        shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        if (shareActionProvider != null && shareForecastString != null)
        {
            shareActionProvider.setShareIntent(createShareForecastIntent());
        }
        else
        {
            Log.d(LOG_TAG, "Share action provider is null");
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args)
    {
        Log.v(LOG_TAG, "In onCreateLoader");

        if (mUri == null)
        {
            return null;
        }

        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        return new CursorLoader(getActivity(), mUri, DETAIL_COLUMNS, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data)
    {
        if (data == null || !data.moveToFirst())
        {
            return;
        }


        String dayName = Utility.getDayName(getActivity(), data.getLong(COL_WEATHER_DATE));
        dayView.setText(dayName);

        String formattedMonthDay = Utility.getFormattedMonthDay(getActivity(), data.getLong(COL_WEATHER_DATE));
        dateView.setText(formattedMonthDay);

        String maxTemp = getString(R.string.format_temperature, data.getFloat(COL_WEATHER_MAX_TEMP));
        highView.setText(maxTemp);

        String minTemp = getString(R.string.format_temperature, data.getFloat(COL_WEATHER_MIN_TEMP));
        lowView.setText(minTemp);


        humidityView.setText(getString(R.string.format_humidity, data.getFloat(COL_WEATHER_HUMIDITY)));
        windView.setText(Utility.getFormattedWind(getActivity(),
                                                  data.getFloat(COL_WEATHER_WIND_SPEED),
                                                  data.getFloat(COL_WEATHER_WIND_DIRECTION)));

        pressureView.setText(getString(R.string.format_pressure, data.getFloat(COL_WEATHER_PRESSURE)));

        int weatherId = data.getInt(COL_WEATHER_CONDITION_ID);
        iconView.setImageResource(Utility.getArtResourceForWeatherCondition(weatherId));
        String forecast = data.getString(COL_WEATHER_DESC);
        forecastView.setText(forecast);


        char emojiForWeatherCondition = Utility.getEmojiForWeatherCondition(weatherId);
        shareForecastString = String.format("Forecast for %s, %s:%c%s, %s to %s", dayName, formattedMonthDay,
                                            emojiForWeatherCondition,forecast, maxTemp, minTemp);
        if (shareActionProvider != null)
        {
            shareActionProvider.setShareIntent(createShareForecastIntent());
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader)
    {

    }

    public void onLocationChanged(String newLocation)
    {
        if (mUri != null)
        {
            long date = WeatherContract.WeatherEntry.getDateFromUri(mUri);
            mUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(newLocation, date);
            getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
        }
    }
}