package com.example.samuilmihaylov.eatorthrow;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;

    private ImageView mImageView;
    private EditText mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseApp.initializeApp(MainActivity.this);

        setContentView(R.layout.activity_main);

        mImageView = findViewById(R.id.photo_image_id);
        mTextView = findViewById(R.id.edit_text_view_id);

        Button mSnapButton = findViewById(R.id.snap_btn_id);
        mSnapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearTextView();
                dispatchTakePictureIntent();
            }
        });
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = null;
            if (extras != null) {
                imageBitmap = (Bitmap) extras.get("data");
            }
            mImageView.setImageBitmap(imageBitmap);

            runTextRecognition(imageBitmap);
        }
    }

    private void runTextRecognition(Bitmap imageBitMap) {
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(imageBitMap);
        FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance().getOnDeviceTextRecognizer();
        detector.processImage(image).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
            @Override
            public void onSuccess(FirebaseVisionText firebaseVisionText) {
                processTextRecognitionResult(firebaseVisionText);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    private LocalDate parseStringToDate(String dateAsString) {

        LocalDate localDate = null;

        List<String> formatStrings = Arrays.asList(
                "M/y",
                "M/d/y",
                "M-d-y",
                "M yyyy",
                "MM yyyy",
                "MM/yyyy",
                "MM-dd-yyyy",
                "dd/MM/yyyy",
                "dd-MM-yyyy",
                "dd.MM.yyyy",
                "dd MM yyyy",
                "ddMMyyyy");

        for (String formatString : formatStrings) {
            try {

                DateTimeFormatter formatter;

                // We check if the date format pattern contains days
                // If true, we have to define a default value for the day because LocalDate needs the day, month and year to be built.
                if (!formatString.contains("d")) {
                    formatter = new DateTimeFormatterBuilder()
                            .appendPattern(formatString)
                            .parseDefaulting(ChronoField.DAY_OF_MONTH, 1)
                            .toFormatter();

                } else {
                    formatter = DateTimeFormatter.ofPattern(formatString);
                }

                localDate = LocalDate.parse(dateAsString, formatter);
                showToast("Successfully recognition of the expire date");

                return localDate;
            } catch (DateTimeParseException e) {
                // TODO: Show input filed where user can manually add the expire date

                Log.e("Expire date parsing", e.getParsedString());
            }
        }

        return localDate;
    }

    private void processTextRecognitionResult(FirebaseVisionText text) {
        List<FirebaseVisionText.TextBlock> blocks = text.getTextBlocks();

        if (blocks.size() == 0) {
            showToast("No text found");
            return;
        }

        StringBuilder recognisedWords = new StringBuilder();

        for (int i = 0; i < blocks.size(); i++) {
            List<FirebaseVisionText.Line> lines = blocks.get(i).getLines();
            for (int j = 0; j < lines.size(); j++) {

                LocalDate dateAsString = this.parseStringToDate(lines.get(j).getText());

                if (dateAsString != null) {
                    recognisedWords.append("LINE: ")
                            .append(lines.get(j).getText());
                    recognisedWords.append(" | PARSED: ")
                            .append(dateAsString)
                            .append(" ");
                    recognisedWords
                            .append(System.getProperty("line.separator"));
                }

                List<FirebaseVisionText.Element> elements = lines.get(j).getElements();
                for (int k = 0; k < elements.size(); k++) {

                    dateAsString = this.parseStringToDate(elements.get(k).getText());

                    if (dateAsString != null) {
                        recognisedWords
                                .append("ELEMENT: ")
                                .append(elements.get(k).getText());
                        recognisedWords
                                .append(" | PARSED: ")
                                .append(dateAsString)
                                .append(" ");
                        recognisedWords
                                .append(System.getProperty("line.separator"));
                    }
                }
                recognisedWords.append(System.getProperty("line.separator"));
            }
            recognisedWords.append(System.getProperty("line.separator"));
        }

        mTextView.setText(recognisedWords);
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void clearTextView() {
        mTextView.setText("");
    }
}
