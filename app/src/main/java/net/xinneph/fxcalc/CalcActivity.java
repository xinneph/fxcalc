package net.xinneph.fxcalc;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class CalcActivity extends Activity {

    private static final String TAG = CalcActivity.class.getSimpleName();
    private static final String DATA = "SharedPrefsData";
    private static final String DATA_BALANCE = "data_balance";
    private static final String DATA_VOLUME = "data_volume";
    private static final String DATA_MARKET = "data_market";
    private static final String DATA_PERCENT_TP = "data_percent_tp";
    private static final String DATA_PERCENT_SL = "data_percent_sl";
    private FragmentPercent fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calc);
        if (savedInstanceState == null) {
            fragment = new FragmentPercent();
            getFragmentManager().beginTransaction()
                    .add(R.id.container, fragment)
                    .commit();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences prefs = getSharedPreferences(DATA, Context.MODE_PRIVATE);
        fragment.setBalance(prefs.getString(DATA_BALANCE, "0.00"));
        fragment.setVolume(prefs.getString(DATA_VOLUME, "0"));
        fragment.setMarket(prefs.getString(DATA_MARKET, ""));
        fragment.setPercentTp(prefs.getString(DATA_PERCENT_TP, "0"));
        fragment.setPercentSl(prefs.getString(DATA_PERCENT_SL, "0"));
    }

    @Override
    protected void onStop() {
        super.onStop();
        SharedPreferences prefs = getSharedPreferences(DATA, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(DATA_BALANCE, fragment.getBalance());
        editor.putString(DATA_VOLUME, fragment.getVolume());
        editor.putString(DATA_MARKET, fragment.getMarket());
        editor.putString(DATA_PERCENT_TP, fragment.getPercentTp());
        editor.putString(DATA_PERCENT_SL, fragment.getPercentSl());
        editor.apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fragment.registerListeners();
    }

    @Override
    protected void onPause() {
        super.onPause();
        fragment.unregisterListeners();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.calc, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        else if (id == R.id.action_stooq) {
            fragment.refresh();
        }
        return super.onOptionsItemSelected(item);
    }

    private static URL getUrl(String market) {
        try {
            String str = "http://stooq.pl/q/l/?s=" + market + "&f=sd2t2c&e=txt";
            return new URL(str);
        } catch (MalformedURLException e) {
            Log.e(TAG, "", e);
            return null;
        }
    }

    private static String[] parseResponse(String content) {
        return content.split(",");
    }

    private static String[] marketPair(String market) {
        String base = market.substring(0, 3);
        String quote = market.substring(3, 6);
        return new String[] {base, quote};
    }

    private static String askFor(String market) {
        URL url = getUrl(market);
        try {
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(3000);
            conn.setConnectTimeout(4000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.connect();
            InputStream stream = conn.getInputStream();
            byte[] buffer = new byte[128];
            if (stream.read(buffer) <= 0) {
                throw new IOException("InputStream is closed or empty");
            }
            return new String(buffer);
        } catch (IOException e) {
            Log.e(TAG, "Error when connecting for URL " + url, e);
            return null;
        }
    }

    /**
     * Fragment calculating pips out of percent.
     */
    public static class FragmentPercent extends Fragment {
        private EditText balanceEdit, volumeEdit, tpPercentEdit, slPercentEdit;
        private Spinner marketsSpinner;
        private TextView tpPipsText, slPipsText, commissionProfitText;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_percent, container, false);
            balanceEdit = (EditText) rootView.findViewById(R.id.edit_balance);
            volumeEdit = (EditText) rootView.findViewById(R.id.edit_volume);
            tpPercentEdit = (EditText) rootView.findViewById(R.id.edit_percent_tp);
            slPercentEdit = (EditText) rootView.findViewById(R.id.edit_percent_sl);
            marketsSpinner = (Spinner) rootView.findViewById(R.id.spinner_markets);
            tpPipsText = (TextView) rootView.findViewById(R.id.text_pips_tp);
            slPipsText = (TextView) rootView.findViewById(R.id.text_pips_sl);
            commissionProfitText = (TextView)
                    rootView.findViewById(R.id.text_commission_over_profit);

            Resources r = getResources();
            String[] markets = r.getStringArray(R.array.markets);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                    android.R.layout.simple_spinner_item, markets);
            adapter.setDropDownViewResource(
                    android.R.layout.simple_spinner_dropdown_item);
            marketsSpinner.setAdapter(adapter);
            return rootView;
        }

        @Override
        public void onStart() {
            super.onStart();
            refresh();
        }

        public void setBalance(String balance) {
            balanceEdit.setText(balance);
        }

        public void setVolume(String volume) {
            volumeEdit.setText(volume);
        }

        public void setMarket(String market) {
            String[] markets = getResources().getStringArray(R.array.markets);
            int position = 0;
            for (String m: markets) {
                if (m.equals(market)) {
                    marketsSpinner.setSelection(position);
                    return;
                }
                position++;
            }
            marketsSpinner.setSelection(0);
        }

        public void setPercentTp(String percent) {
            tpPercentEdit.setText(percent);
        }

        public void setPercentSl(String percent) {
            slPercentEdit.setText(percent);
        }

        public String getBalance() {
            return balanceEdit.getText().toString();
        }

        public String getVolume() {
            return volumeEdit.getText().toString();
        }

        public String getMarket() {
            return (String) marketsSpinner.getSelectedItem();
        }

        public String getPercentTp() {
            return tpPercentEdit.getText().toString();
        }

        public String getPercentSl() {
            return slPercentEdit.getText().toString();
        }

        public void refresh() {
            new StooqTask().execute(getMarket());
        }

        public void calculate(double base, double quote) {
            String strBalance = balanceEdit.getText().toString();
            double balance = strBalance.isEmpty() ? 0 : Double.parseDouble(strBalance);
            String strVolume = volumeEdit.getText().toString();
            int volume = strVolume.isEmpty() ? 1 : Integer.parseInt(strVolume);
            String strPercentTp = tpPercentEdit.getText().toString();
            int percentTp = strPercentTp.isEmpty() ? 0 : Integer.parseInt(strPercentTp);
            int percentSl = getPercentSl().isEmpty() ? 0 : Integer.parseInt(getPercentSl());

            double commission = volume * 1.3 * base;
            double profit = percentTp * balance / 100.0;
            double pipsTp = (commission + profit)/(volume * quote);
            double commissionProfit = commission / profit;
            tpPipsText.setText(String.format("%.2f", pipsTp));
            commissionProfitText.setText(String.format("%.2f",commissionProfit));
            double loss = percentSl * balance / 100.0;
            double pipsSl = (loss - commission) / (volume * quote);
            slPipsText.setText(String.format("%.2f",pipsSl));
        }

        private TextWatcher watcher = new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // nothing
            }

            @Override
            public void afterTextChanged(Editable s) {
                refresh();
            }
        };

        private AdapterView.OnItemSelectedListener onSelected =
                new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                refresh();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // empty
            }
        };

        public void registerListeners() {
            balanceEdit.addTextChangedListener(watcher);
            volumeEdit.addTextChangedListener(watcher);
            tpPercentEdit.addTextChangedListener(watcher);
            slPercentEdit.addTextChangedListener(watcher);
            marketsSpinner.setOnItemSelectedListener(onSelected);
        }

        public void unregisterListeners() {
            balanceEdit.removeTextChangedListener(watcher);
            volumeEdit.removeTextChangedListener(watcher);
            tpPercentEdit.removeTextChangedListener(watcher);
            slPercentEdit.removeTextChangedListener(watcher);
            marketsSpinner.setOnItemSelectedListener(null);
        }

        private class StooqTask extends AsyncTask<String,Void,String> {

            /*
            TODO enhance the algorithm to work good for markets like ***PLN
            (where quote currency is PLN)
            */
            @Override
            protected String doInBackground(String... input) {
                String[] pair = marketPair(input[0]);
                String contentBase = askFor(pair[0]+"PLN");
                String contentQuote = askFor(pair[1]+"PLN");
                return contentBase+"/"+contentQuote;
            }

            @Override
            protected void onPostExecute(String content) {
                String[] bq = content.split("/");
                String[] base = parseResponse(bq[0]);
                String[] quote = parseResponse(bq[1]);
                double b = Double.parseDouble(base[3]);
                double q = Double.parseDouble(quote[3]);
                calculate(b, q);
            }
        }
    }
}
