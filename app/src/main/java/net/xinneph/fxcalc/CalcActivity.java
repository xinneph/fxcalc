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
import android.widget.Button;
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
    private static final String DATA_LEVERAGE = "data_leverage";
    private static final String DATA_COMMISSION = "data_commission";
    private static final int MIN_VOLUME = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calc);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new FragmentPercent())
                    .commit();
        }
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
        return super.onOptionsItemSelected(item);
    }

    private static URL createUrlForMarket(String market) {
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

    private static String[] marketPairs(String market) {
        String base = market.substring(0, 3);
        String quote = market.substring(3, 6);
        return new String[] {base, quote};
    }

    private static String askFor(String market) {
        URL url = createUrlForMarket(market);
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
        private EditText balanceEdit, volumeEdit, tpPercentEdit, slPercentEdit, leverageEdit;
        private Spinner marketsSpinner;
        private TextView tpPipsText, slPipsText, commissionProfitText, depositText, depositPercent;
        private EditText commissionEdit;
        private TextView commissionAmountText, commissionPercentText;
        private double basePln, quotePln;
        private Activity activityAttach;
        private Button addVolume, oddVolume;

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            activityAttach = activity;
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            SharedPreferences prefs = activityAttach.getSharedPreferences(DATA, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(DATA_BALANCE, getBalance());
            editor.putString(DATA_VOLUME, getVolume());
            editor.putString(DATA_MARKET, getMarket());
            editor.putString(DATA_PERCENT_TP, getPercentTp());
            editor.putString(DATA_PERCENT_SL, getPercentSl());
            editor.putString(DATA_LEVERAGE, getLeverage());
            editor.putString(DATA_COMMISSION, getCommission());
            editor.apply();
        }

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
            leverageEdit = (EditText) rootView.findViewById(R.id.edit_leverage);
            depositText = (TextView) rootView.findViewById(R.id.text_deposit);
            depositPercent = (TextView) rootView.findViewById(R.id.text_deposit_percent);
            addVolume = (Button) rootView.findViewById(R.id.button_add_volume);
            oddVolume = (Button) rootView.findViewById(R.id.button_odd_volume);
            commissionEdit = (EditText) rootView.findViewById(R.id.edit_commission);
            commissionAmountText = (TextView) rootView.findViewById(R.id.text_commission_amount);
            commissionPercentText = (TextView) rootView.findViewById(R.id.text_commission_percent);

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
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            SharedPreferences prefs = activityAttach.getSharedPreferences(DATA, Context.MODE_PRIVATE);
            setBalance(prefs.getString(DATA_BALANCE, "0.00"));
            setVolume(prefs.getString(DATA_VOLUME, "0"));
            setMarket(prefs.getString(DATA_MARKET, ""));
            setPercentTp(prefs.getString(DATA_PERCENT_TP, "0"));
            setPercentSl(prefs.getString(DATA_PERCENT_SL, "0"));
            setLeverage(prefs.getString(DATA_LEVERAGE, "10"));
            setCommission(prefs.getString(DATA_COMMISSION, "0"));
        }

        @Override
        public void onStart() {
            super.onStart();
            registerListeners();
            addVolume.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String strVolume = getVolume();
                    if (strVolume.isEmpty()) {
                        setVolume("10");
                    }
                    else {
                        Integer volume = Integer.parseInt(strVolume);
                        volume += 10;
                        setVolume(volume.toString());
                    }
                }
            });
            oddVolume.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String strVolume = getVolume();
                    if (strVolume.isEmpty() || "0".equals(strVolume)) {
                        return;
                    }
                    else {
                        Integer volume = Integer.parseInt(strVolume);
                        volume -= 10;
                        setVolume(volume.toString());
                    }
                }
            });
        }

        @Override
        public void onStop() {
            super.onStop();
            unregisterListeners();
        }

        private void setBalance(String balance) {
            balanceEdit.setText(balance);
        }

        private void setVolume(String volume) {
            volumeEdit.setText(volume);
        }

        private void setMarket(String market) {
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

        private void setPercentTp(String percent) {
            tpPercentEdit.setText(percent);
        }

        private void setPercentSl(String percent) {
            slPercentEdit.setText(percent);
        }

        private void setLeverage(String leverage) {
            leverageEdit.setText(leverage);
        }

        private void setCommission(String commission) {
            commissionEdit.setText(commission);
        }

        private String getBalance() {
            return balanceEdit.getText().toString();
        }

        private String getVolume() {
            return volumeEdit.getText().toString();
        }

        private String getMarket() {
            return (String) marketsSpinner.getSelectedItem();
        }

        private String getPercentTp() {
            return tpPercentEdit.getText().toString();
        }

        private String getPercentSl() {
            return slPercentEdit.getText().toString();
        }

        private String getLeverage() {
            return leverageEdit.getText().toString();
        }

        private String getCommission() {
            return commissionEdit.getText().toString();
        }

        public void refresh(String market) {
            new StooqTask().execute(market);
        }

        private void calculate(double base, double quote) {
            double balance = getBalance().isEmpty() ? 0.0 : Double.parseDouble(getBalance());
            String strVolume = volumeEdit.getText().toString();
            int volume = strVolume.isEmpty() ? 0 : Integer.parseInt(strVolume);
            String strPercentTp = tpPercentEdit.getText().toString();
            int percentTp = strPercentTp.isEmpty() ? 0 : Integer.parseInt(strPercentTp);
            int percentSl = getPercentSl().isEmpty() ? 0 : Integer.parseInt(getPercentSl());
            double commissionPerLot = getCommission().isEmpty() ? 0.0 : Double.parseDouble(getCommission());
            double commission = volume * (commissionPerLot / 100.0) * base;
            commissionAmountText.setText(String.format("%.2f", commission));
            double commissionPercent = commission / balance * 100.0;
            commissionPercentText.setText(String.format("%.2f", commissionPercent)+" %");

            double profit = percentTp * balance / 100.0;
            double pipsTp = (profit + commission)/(volume * quote / 10.0);
            double commissionProfit = commission / profit;
            tpPipsText.setText(String.format("%.2f", pipsTp));
            commissionProfitText.setText(String.format("%.2f",commissionProfit));
            double loss = percentSl * balance / 100.0;
            double pipsSl = (loss - commission) / (volume * quote / 10.0);
            slPipsText.setText(String.format("%.2f", pipsSl));
        }

        private void calculateDeposit() {
            int leverage = getLeverage().isEmpty() ? 0 : Integer.parseInt(getLeverage());
            int volume = getVolume().isEmpty() ? 0 : Integer.parseInt(getVolume());
            double depositPln = basePln * (volume * 1000) / leverage;
            depositText.setText(String.format("%.2f", depositPln));
            double balance = getBalance().isEmpty() ? 0.0 : Double.parseDouble(getBalance());
            depositPercent.setText(String.format("%.2f", depositPln / balance * 100.0)+" %");
        }

        private final TextWatcher watcher = new TextWatcher() {

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
                calculate(basePln, quotePln);
            }
        };

        private final TextWatcher depositCalculator = new TextWatcher() {
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
                calculateDeposit();
            }
        };

        private final AdapterView.OnItemSelectedListener onSelected =
                new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                refresh(getMarket());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // empty
            }
        };

        public void registerListeners() {
            balanceEdit.addTextChangedListener(watcher);
            volumeEdit.addTextChangedListener(watcher);
            volumeEdit.addTextChangedListener(depositCalculator);
            tpPercentEdit.addTextChangedListener(watcher);
            slPercentEdit.addTextChangedListener(watcher);
            marketsSpinner.setOnItemSelectedListener(onSelected);
            leverageEdit.addTextChangedListener(depositCalculator);
            commissionEdit.addTextChangedListener(watcher);
        }

        public void unregisterListeners() {
            balanceEdit.removeTextChangedListener(watcher);
            volumeEdit.removeTextChangedListener(watcher);
            volumeEdit.removeTextChangedListener(depositCalculator);
            tpPercentEdit.removeTextChangedListener(watcher);
            slPercentEdit.removeTextChangedListener(watcher);
            marketsSpinner.setOnItemSelectedListener(null);
            leverageEdit.removeTextChangedListener(depositCalculator);
            commissionEdit.removeTextChangedListener(watcher);
        }

        private class StooqTask extends AsyncTask<String,Void,String> {

            /*
            TODO enhance the algorithm to work good for markets like ***PLN
            (where quote currency is PLN)
            */
            @Override
            protected String doInBackground(String... input) {
                String[] pair = marketPairs(input[0]);
                String contentBase = askFor(pair[0]+"PLN");
                String contentQuote = askFor(pair[1]+"PLN");
                return contentBase+"/"+contentQuote;
            }

            @Override
            protected void onPostExecute(String content) {
                String[] bq = content.split("/");
                String[] base = parseResponse(bq[0]);
                String[] quote = parseResponse(bq[1]);
                basePln = Double.parseDouble(base[3]);
                quotePln = Double.parseDouble(quote[3]);
                calculateDeposit();
                calculate(basePln, quotePln);
            }
        }
    }
}
