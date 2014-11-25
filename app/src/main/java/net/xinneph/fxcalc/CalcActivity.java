package net.xinneph.fxcalc;

import android.app.Activity;
import android.app.Fragment;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;


public class CalcActivity extends Activity {

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

    /**
     * Fragment calculating percent out of pips.
     */
    public static class FragmentPips extends Fragment {

        private EditText balanceEdit, volumeEdit, pipsEdit;
        private Spinner marketsSpinner;
        private TextView percentText, profitCommissionText;
        private ArrayAdapter<String> marketsSpinnerAdapter;

        public FragmentPips() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_pips, container, false);
            balanceEdit = (EditText) rootView.findViewById(R.id.edit_balance);
            volumeEdit = (EditText) rootView.findViewById(R.id.edit_volume);
            pipsEdit = (EditText) rootView.findViewById(R.id.edit_pips);
            marketsSpinner = (Spinner) rootView.findViewById(R.id.spinner_markets);
            percentText = (TextView) rootView.findViewById(R.id.text_percent);
            profitCommissionText = (TextView)
                    rootView.findViewById(R.id.text_profit_over_commission);

            balanceEdit.setText("243.73");
            volumeEdit.setText("10");
            pipsEdit.setText("4.0");

            Resources r = getResources();
            String[] markets = r.getStringArray(R.array.markets);
            marketsSpinnerAdapter = new ArrayAdapter<String>(getActivity(),
                    android.R.layout.simple_spinner_item, markets);
            marketsSpinnerAdapter.setDropDownViewResource(
                    android.R.layout.simple_spinner_dropdown_item);
            marketsSpinner.setAdapter(marketsSpinnerAdapter);
            return rootView;
        }
    }

    /**
     * Fragment calculating pips out of percent.
     */
    public static class FragmentPercent extends Fragment {
        private EditText balanceEdit, volumeEdit, percentEdit;
        private Spinner marketsSpinner;
        private TextView pipsText, profitCommissionText;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_percent, container, false);
            balanceEdit = (EditText) rootView.findViewById(R.id.edit_balance);
            volumeEdit = (EditText) rootView.findViewById(R.id.edit_volume);
            percentEdit = (EditText) rootView.findViewById(R.id.edit_percent);
            marketsSpinner = (Spinner) rootView.findViewById(R.id.spinner_markets);
            pipsText = (TextView) rootView.findViewById(R.id.text_pips);
            profitCommissionText = (TextView)
                    rootView.findViewById(R.id.text_profit_over_commission);

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
    }
}
