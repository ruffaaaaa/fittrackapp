package com.example.fittrackapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fittrackapp.helper.FitnessFormDBHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class FitnessFormActivity extends AppCompatActivity {

    private LinearLayout questionsContainer;
    private ProgressBar progressBar;
    private FitnessFormDBHelper dbHelper;

    private Button continueButton;
    private SharedPreferences sharedPreferences;
    private JSONArray questionsArray;
    private int currentQuestionIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fitness_form);

        questionsContainer = findViewById(R.id.questionsContainer);
        continueButton = findViewById(R.id.continueButton);
        progressBar = findViewById(R.id.progressBar);
        sharedPreferences = getSharedPreferences("fitness_form_responses", MODE_PRIVATE);
        dbHelper = new FitnessFormDBHelper(this);

        try {
            String json = loadJSONFromAsset();
            if (json != null) {
                questionsArray = new JSONArray(json);
                showQuestion(currentQuestionIndex);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        continueButton.setOnClickListener(v -> {
            saveCurrentResponse();
            currentQuestionIndex++;
            if (currentQuestionIndex < questionsArray.length()) {
                showQuestion(currentQuestionIndex);
            } else {

                Intent intent = new Intent(FitnessFormActivity.this, CreateWorkoutPlanActivity.class);
                startActivity(intent);
                finish();
            }


            updateProgressBar();
        });
    }

    private void updateProgressBar() {
        int progress = (int) (((float) (currentQuestionIndex + 1) / questionsArray.length()) * 100);
        progressBar.setProgress(progress);
    }

    private String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream is = getAssets().open("fitness_form_questions.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, StandardCharsets.UTF_8);
            Log.d("JSON", json);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return json;
    }


    private void showQuestion(int index) {
        questionsContainer.removeAllViews();
        continueButton.setEnabled(false);

        try {
            if (index >= questionsArray.length()) {
                Toast.makeText(this, "All questions answered!", Toast.LENGTH_SHORT).show();
                return;
            }

            JSONObject question = questionsArray.getJSONObject(index);
            String questionText = question.getString("question");
            String key = question.getString("key");
            String type = question.getString("type");

            TextView questionTextView = new TextView(this);
            questionTextView.setText(questionText);
            questionTextView.setTextSize(25);
            questionTextView.setTextColor(Color.BLACK);
            questionTextView.setTypeface(null, Typeface.BOLD);
            questionTextView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            questionTextView.setPadding(0, 0, 0, 24);
            questionsContainer.addView(questionTextView);

            if ("text".equals(type)) {
                questionsContainer.setGravity(Gravity.TOP);

                View space = new View(this);
                space.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        0, 1));
                questionsContainer.addView(space);

                LinearLayout editTextContainer = new LinearLayout(this);
                editTextContainer.setOrientation(LinearLayout.HORIZONTAL);
                editTextContainer.setGravity(Gravity.CENTER_HORIZONTAL);

                EditText editText = new EditText(this);
                editText.setTag(key);
                editText.setTextSize(100);
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                editText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                editText.setPadding(50, 30, 50, 30);

                LinearLayout.LayoutParams editParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                editText.setLayoutParams(editParams);

                editText.addTextChangedListener(new TextWatcher() {
                    @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                    @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                        continueButton.setEnabled(!s.toString().trim().isEmpty());
                    }
                    @Override public void afterTextChanged(Editable s) {}
                });

                editTextContainer.addView(editText);
                questionsContainer.addView(editTextContainer);

                View bottomSpace = new View(this);
                bottomSpace.setLayoutParams(space.getLayoutParams());
                questionsContainer.addView(bottomSpace);

            } else if ("radio".equals(type)) {
                questionsContainer.setGravity(Gravity.TOP);

                RadioGroup radioGroup = new RadioGroup(this);
                radioGroup.setTag(key);
                JSONArray options = question.getJSONArray("options");
                for (int j = 0; j < options.length(); j++) {
                    RadioButton radioButton = new RadioButton(this);
                    radioButton.setText(options.getString(j));
                    radioButton.setButtonDrawable(null);
                    radioButton.setBackgroundResource(R.drawable.radio_checkbox_selector);
                    radioButton.setTextColor(Color.BLACK);
                    radioButton.setTextSize(18);
                    radioButton.setPadding(40, 30, 40, 30);

                    RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(
                            RadioGroup.LayoutParams.MATCH_PARENT,
                            RadioGroup.LayoutParams.WRAP_CONTENT);
                    params.setMargins(0, 5, 0, 20);
                    radioButton.setLayoutParams(params);

                    radioGroup.addView(radioButton);
                }

                radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
                    continueButton.setEnabled(true);
                });

                questionsContainer.addView(radioGroup);

            } else if ("checkbox".equals(type)) {
                questionsContainer.setGravity(Gravity.TOP);

                JSONArray options = question.getJSONArray("options");
                for (int j = 0; j < options.length(); j++) {
                    CheckBox checkBox = new CheckBox(this);
                    checkBox.setText(options.getString(j));
                    checkBox.setTag(key + "_" + j);
                    checkBox.setButtonDrawable(null);
                    checkBox.setBackgroundResource(R.drawable.radio_checkbox_selector);
                    checkBox.setTextColor(Color.BLACK);
                    checkBox.setTextSize(18);
                    checkBox.setPadding(40, 30, 40, 30);

                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);
                    params.setMargins(0, 5, 0, 10);
                    checkBox.setLayoutParams(params);

                    checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                        boolean atLeastOneChecked = false;
                        for (int k = 0; k < questionsContainer.getChildCount(); k++) {
                            View v = questionsContainer.getChildAt(k);
                            if (v instanceof CheckBox && ((CheckBox) v).isChecked()) {
                                atLeastOneChecked = true;
                                break;
                            }
                        }
                        continueButton.setEnabled(atLeastOneChecked);
                    });

                    questionsContainer.addView(checkBox);
                }


            } else if ("spinner".equals(type)) {
                Spinner spinner = new Spinner(this);
                JSONArray options = question.getJSONArray("options");
                List<String> optionList = new ArrayList<>();
                for (int j = 0; j < options.length(); j++) {
                    optionList.add(options.getString(j));
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_item, optionList);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(adapter);
                spinner.setTag(key);

                spinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                        continueButton.setEnabled(true);
                    }

                    @Override
                    public void onNothingSelected(android.widget.AdapterView<?> parent) {
                        continueButton.setEnabled(false);
                    }
                });

                questionsContainer.addView(spinner);
            }

            continueButton.setText((index == questionsArray.length() - 1) ? "Finish" : "Continue");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void saveCurrentResponse() {
        SharedPreferences.Editor editor = sharedPreferences.edit();

        for (int i = 0; i < questionsContainer.getChildCount(); i++) {
            View view = questionsContainer.getChildAt(i);
            Object tagObj = view.getTag();
            if (tagObj == null) continue;

            String tag = tagObj.toString();
            String response = "";

            if (view instanceof EditText) {
                response = ((EditText) view).getText().toString();
                editor.putString(tag, response);

            } else if (view instanceof RadioGroup) {
                int selectedId = ((RadioGroup) view).getCheckedRadioButtonId();
                if (selectedId != -1) {
                    RadioButton selected = findViewById(selectedId);
                    response = selected.getText().toString();
                    editor.putString(tag, response);
                }

            } else if (view instanceof Spinner) {
                response = ((Spinner) view).getSelectedItem().toString();
                editor.putString(tag, response);

            } else if (view instanceof CheckBox) {
                boolean isChecked = ((CheckBox) view).isChecked();
                response = isChecked ? "true" : "false";
                editor.putBoolean(tag, isChecked);
            }

            if (!response.isEmpty()) {
                dbHelper.insertResponse(tag, response);
            }
        }

        editor.apply();
    }



}
