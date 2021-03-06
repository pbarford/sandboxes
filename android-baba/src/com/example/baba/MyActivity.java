package com.example.baba;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class MyActivity extends Activity {
    private Random rand = new Random();
    private Handler handler = new Handler();

    private TextView speech;
    private TextView firstLetter;
    private TextView secondLetter;
    private TextView thirdLetter;
    private TableLayout keys;

    private int currentChoice = 1;
    private String currentLetterSequence ="";
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        firstLetter = (TextView) findViewById(R.id.firstLetter);
        secondLetter = (TextView) findViewById(R.id.secondLetter);
        thirdLetter = (TextView) findViewById(R.id.thirdLetter);
        keys = (TableLayout) findViewById(R.id.keys);
        speech = (TextView) findViewById(R.id.speech);
        speech.setText("Hi there Alexandra!\nClick 'Go' to get started");
    }

    public void generateLetters(View view) {
        speech.setText("Generating ....");
        keys.setVisibility(View.INVISIBLE);
        currentLetterSequence = "";
        handler.post(new LetterRunner(firstLetter, 1000));
        handler.post(new LetterRunner(secondLetter, 1750));
        handler.post(new LetterRunner(thirdLetter, 2500));
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mixUpKeys();
            }
        }, 4750);
    }

    public void clickKey(View view) {
        switch (currentChoice) {
            case 1:
                firstLetter.setText(((Button) view).getText());
                currentChoice++;
                break;
            case 2:
                secondLetter.setText(((Button) view).getText());
                currentChoice++;
                break;
            case 3:
                thirdLetter.setText(((Button) view).getText());
                findViewById(R.id.keys).setVisibility(View.INVISIBLE);
                currentChoice = 1;
                checkGuess();
                break;
        }
    }

    private void checkGuess() {
        //int duration = Toast.LENGTH_LONG;
        //Context context = getApplicationContext();
        String guess = firstLetter.getText() + "" +  secondLetter.getText() + "" + thirdLetter.getText();
        if(guess.equalsIgnoreCase(currentLetterSequence)) {
            //Toast toast = Toast.makeText(context, "correct", duration);
            //toast.show();
            speech.setText("Correct!\nTry Again!");
        } else {
            //Toast toast = Toast.makeText(context, "incorrect, should be " + currentLetterSequence, duration);
            //toast.show();
            speech.setText("Incorrect, should be " + currentLetterSequence+ "\nTry Again!");
        }
    }

    private void mixUpKeys() {

        List<Integer> allocatedLetters = new ArrayList<Integer>();
        for(int i=1; i <= 26; i++) {
            Button key = (Button) keys.findViewWithTag("key" + i);
            Integer letter = nextKey(allocatedLetters);
            allocatedLetters.add(letter);
            key.setText(String.valueOf((char) (letter.intValue() + 'A')));
        }

        speech.setText("Enter your answer");
        keys.setVisibility(View.VISIBLE);
    }

    private Integer nextKey(List<Integer> allocatedLetters) {
        Integer letter = rand.nextInt(26);
        while(allocatedLetters.contains(letter))
            letter = rand.nextInt(26);
        return letter;
    }

    private String generateLetter() {
        char c = (char) (rand.nextInt(26) + 'A');
        return String.valueOf(c);
    }

    private class HideLetter implements  Runnable {
        private final TextView textView;
        HideLetter(TextView textView) {
            this.textView = textView;
        }

        @Override
        public void run() {
            textView.setText("?");
        }
    }

    private class LetterRunner implements Runnable {

        private final TextView textView;
        private final long start = System.currentTimeMillis();
        private final long dura;

        LetterRunner(TextView textView, long dura) {
            this.textView = textView;
            this.dura = dura;
        }

        @Override
        public void run() {
            String letter = generateLetter();
            textView.setText(letter);
            if(System.currentTimeMillis() < start + dura)
                handler.postDelayed(this, 50);
            else {
                currentLetterSequence = currentLetterSequence + String.valueOf(letter);
                handler.postDelayed(new HideLetter(textView), 4500 - dura);
            }
        }
    }
}



