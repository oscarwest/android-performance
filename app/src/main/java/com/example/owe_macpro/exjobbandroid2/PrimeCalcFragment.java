package com.example.owe_macpro.exjobbandroid2;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.ListView;


import java.lang.reflect.Array;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;


public class PrimeCalcFragment extends Fragment implements View.OnClickListener {

    private double generatePrimesExecutionTime;
    private ArrayList<Integer> primes;
    ListView primeNumberListView;
    TextView primeNumberResultTextView;
    private int testCount = 0;
    Handler handler;
    Runnable runnable;
    Boolean runnableActive = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_prime_calc, container, false);

        // Initialize arraylist
        primes = new ArrayList<>();

        // Grab views
        primeNumberListView = (ListView) v.findViewById(R.id.primeNumberListView);
        primeNumberResultTextView = (TextView) v.findViewById(R.id.resultsTextView);

        // Initialize view components
        primeNumberListView.setAdapter(new PrimeCalcArrayAdapter(getActivity().getApplicationContext(), primes));
        primeNumberResultTextView.setText("0ms");

        // Add listeners to buttons
        final Button calculatePrimesButton = (Button) v.findViewById(R.id.run_calc_btn);
        Button resetPrimesButton = (Button) v.findViewById(R.id.reset_calc_btn);

        calculatePrimesButton.setOnClickListener(this);
        resetPrimesButton.setOnClickListener(this);

        // Automate button clicking for data collection

        handler = new Handler();
        runnable = new Runnable() {
            public void run() {
                calculatePrimesButton.performClick();
                testCount++;
                handler.postDelayed(this, 700);
            }
        };


        return v;
    }

    public static PrimeCalcFragment newInstance() {

        PrimeCalcFragment f = new PrimeCalcFragment();
        Bundle b = new Bundle();
        b.putInt("max_prime_number", 1000);

        f.setArguments(b);

        return f;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.run_calc_btn:
                Integer primeCount = 100;
                primes = generatePrimes(primeCount);
                //primeNumberListView.setAdapter(new PrimeCalcArrayAdapter(getActivity().getApplicationContext(), primes));
                primeNumberResultTextView.setText(Double.toString(generatePrimesExecutionTime) + "ms");
                // Add to db
                SimpleHttpPost postClass = new SimpleHttpPost();
                postClass.setPostParameters("primes="+Integer.toString(primeCount)+"&app_type=android&app_function=prime&exec_time="+Double.toString(generatePrimesExecutionTime));
                postClass.httpPost();

                // Automate data collection, initialize runnable only the first time we click it
                if (runnableActive == false) {
                    runnableActive = true;
                    handler.postDelayed(runnable, 1000);
                }

                break;
            case R.id.reset_calc_btn:
                Log.d("RESETBTN", "Resetting primes");
                primes.clear();
                primeNumberListView.setAdapter(new PrimeCalcArrayAdapter(getActivity().getApplicationContext(), primes));
                primeNumberResultTextView.setText("0ms");
                handler.removeCallbacksAndMessages(runnable);
                runnableActive = false;
                break;

        }

    }

    private ArrayList<Integer> generatePrimes(int limit) {
        // sieve of eratosthenes
        final int numPrimes = countPrimesUpperBound(limit);
        ArrayList<Integer> primes = new ArrayList<Integer>(numPrimes);
        boolean [] isComposite    = new boolean [limit];   // all false
        final int sqrtLimit       = (int)Math.sqrt(limit); // floor
        long lStartTime = System.nanoTime();

        for (int i = 2; i <= sqrtLimit; i++) {
            if (!isComposite [i]) {
                primes.add(i);
                for (int j = i*i; j < limit; j += i) // `j+=i` can overflow
                    isComposite [j] = true;
            }
        }
        for (int i = sqrtLimit + 1; i < limit; i++)
            if (!isComposite [i])
                primes.add(i);

        long lEndTime = System.nanoTime();
        long temp = lEndTime - lStartTime;
        generatePrimesExecutionTime = (double)temp / 1000000;
        Log.d("CREATION", "Prime execution time: "+String.valueOf(generatePrimesExecutionTime) + "ms");

        return primes;
    }

    private static int countPrimesUpperBound(int max) {
        return max > 1 ? (int)(1.25506 * max / Math.log((double)max)) : 0;
    }

}
