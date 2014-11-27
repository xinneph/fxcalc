package net.xinneph.fxcalc;

import android.app.Activity;
import android.app.Fragment;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class CalcActivity extends Activity {

    private final static String TAG = CalcActivity.class.getSimpleName();
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
            String market = fragment.getMarket();
            new StooqTask().execute(market);
        }
        return super.onOptionsItemSelected(item);
    }

    private URL getUrl(String market) {
        try {
            String str = "http://stooq.pl/q/l/?s=" + market + "&f=sd2t2c&e=txt";
            return new URL(str);
        } catch (MalformedURLException e) {
            Log.e(TAG, "", e);
            return null;
        }
    }

    private String[] parseContent(String content) {
        return content.split(",");
    }

    private String[] marketPair(String market) {
        String base = market.substring(0, 3);
        String quote = market.substring(3, 6);
        return new String[] {base, quote};
    }

    private String askFor(String market) {
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
            stream.read(buffer);
            String content = new String(buffer);
            return content;
        } catch (IOException e) {
            Log.e(TAG, "Error when connecting for URL " + url, e);
            return null;
        }
    }

    private class StooqTask extends AsyncTask<String,Void,String> {
        @Override
        protected String doInBackground(String... input) {
//            String content = askFor(input[0]);
            String[] pair = marketPair(input[0]);
            String contentBase = askFor(pair[0]+"PLN");
            String contentQuote = askFor(pair[1]+"PLN");
            return contentBase+"/"+contentQuote;
        };

        @Override
        protected void onPostExecute(String content) {
            String[] bq = content.split("/");
            String[] base = parseContent(bq[0]);
            String[] quote = parseContent(bq[1]);
            double b = Double.parseDouble(base[3]);
            double q = Double.parseDouble(quote[3]);
            fragment.calculate(b, q);
//            Toast.makeText(CalcActivity.this, content, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Fragment calculating percent out of pips.
     */
//    public static class FragmentPips extends Fragment {
//
//        private EditText balanceEdit, volumeEdit, pipsEdit;
//        private Spinner marketsSpinner;
//        private TextView percentText, profitCommissionText;
//        private ArrayAdapter<String> marketsSpinnerAdapter;
//
//        public FragmentPips() {
//        }
//
//        @Override
//        public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                Bundle savedInstanceState) {
//            View rootView = inflater.inflate(R.layout.fragment_pips, container, false);
//            balanceEdit = (EditText) rootView.findViewById(R.id.edit_balance);
//            volumeEdit = (EditText) rootView.findViewById(R.id.edit_volume);
//            pipsEdit = (EditText) rootView.findViewById(R.id.edit_pips);
//            marketsSpinner = (Spinner) rootView.findViewById(R.id.spinner_markets);
//            percentText = (TextView) rootView.findViewById(R.id.text_percent);
//            profitCommissionText = (TextView)
//                    rootView.findViewById(R.id.text_profit_over_commission);
//
//            balanceEdit.setText("243.73");
//            volumeEdit.setText("10");
//            pipsEdit.setText("4.0");
//
//            Resources r = getResources();
//            String[] markets = r.getStringArray(R.array.markets);
//            marketsSpinnerAdapter = new ArrayAdapter<String>(getActivity(),
//                    android.R.layout.simple_spinner_item, markets);
//            marketsSpinnerAdapter.setDropDownViewResource(
//                    android.R.layout.simple_spinner_dropdown_item);
//            marketsSpinner.setAdapter(marketsSpinnerAdapter);
//            return rootView;
//        }
//    }

    /**
     * Fragment calculating pips out of percent.
     */
    public static class FragmentPercent extends Fragment {
        private EditText balanceEdit, volumeEdit, percentEdit;
        private Spinner marketsSpinner;
        private TextView pipsText, commissionProfitText;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_percent, container, false);
            balanceEdit = (EditText) rootView.findViewById(R.id.edit_balance);
            volumeEdit = (EditText) rootView.findViewById(R.id.edit_volume);
            percentEdit = (EditText) rootView.findViewById(R.id.edit_percent);
            marketsSpinner = (Spinner) rootView.findViewById(R.id.spinner_markets);
            pipsText = (TextView) rootView.findViewById(R.id.text_pips);
            commissionProfitText = (TextView)
                    rootView.findViewById(R.id.text_commission_over_profit);

            balanceEdit.setText("294.93");
            volumeEdit.setText("3");
            percentEdit.setText("8");

            Resources r = getResources();
            String[] markets = r.getStringArray(R.array.markets);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                    android.R.layout.simple_spinner_item, markets);
            adapter.setDropDownViewResource(
                    android.R.layout.simple_spinner_dropdown_item);
            marketsSpinner.setAdapter(adapter);
            return rootView;
        }

        public String getMarket() {
            return (String) marketsSpinner.getSelectedItem();
        }

        public void calculate(double base, double quote) {
            String strBalance = balanceEdit.getText().toString();
            double balance = Double.parseDouble(strBalance);
            String strVolume = volumeEdit.getText().toString();
            int volume = Integer.parseInt(strVolume);
            String strPercent = percentEdit.getText().toString();
            int percent = Integer.parseInt(strPercent);

            double commission = volume * 1.3 * base;
            double profit = percent * balance / 100.0;
            double pips = (commission + profit)/(volume * quote);
            double commissionProfit = commission / profit;
            pipsText.setText(""+pips);
            commissionProfitText.setText(""+commissionProfit);
        }
    }
}
