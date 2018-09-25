package com.example.samuilmihaylov.eatorthrow;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE_EXPIRE_DATE = 1;
    private static final int REQUEST_IMAGE_CAPTURE_BARCODE = 2;

    private static final List<String> formatStrings = Arrays.asList(
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

    private ImageView mImageView;
    private EditText mTextView;
    private Spinner mSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseApp.initializeApp(MainActivity.this);

        setContentView(R.layout.activity_main);

        mImageView = findViewById(R.id.photo_image_id);
        mTextView = findViewById(R.id.edit_text_view_id);

        Button mSnapExpireDateButton = findViewById(R.id.snap_expire_date_btn_id);
        mSnapExpireDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearTextView();
                dispatchTakePictureIntent(ImageCaptureActionType.EXPIRE_DATE);
            }
        });

        Button mSnapBarcodeButton = findViewById(R.id.snap_barcode_btn_id);
        mSnapBarcodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent(ImageCaptureActionType.BARCODE);
            }
        });
    }

    private void dispatchTakePictureIntent(ImageCaptureActionType imageCaptureActionType) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (imageCaptureActionType == ImageCaptureActionType.EXPIRE_DATE) {
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE_EXPIRE_DATE);
            }

        } else if (imageCaptureActionType == ImageCaptureActionType.BARCODE) {
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE_BARCODE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE_EXPIRE_DATE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = null;
            if (extras != null) {
                imageBitmap = (Bitmap) extras.get("data");
            }
            mImageView.setImageBitmap(imageBitmap);

            runTextRecognition(imageBitmap);
        }

        if (requestCode == REQUEST_IMAGE_CAPTURE_BARCODE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = null;
            if (extras != null) {
                imageBitmap = (Bitmap) extras.get("data");
            }

            runBarcodeRecognition(imageBitmap);
        }
    }

    private void runBarcodeRecognition(Bitmap imageBitmap) {
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(imageBitmap);
        FirebaseVisionBarcodeDetector firebaseVisionBarcodeDetector = FirebaseVision.getInstance()
                .getVisionBarcodeDetector();

        firebaseVisionBarcodeDetector.detectInImage(image)
                .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionBarcode>>() {
                    @Override
                    public void onSuccess(List<FirebaseVisionBarcode> barcodes) {
                        // Task completed successfully
                        for (FirebaseVisionBarcode barcode : barcodes) {
                            String rawValue = barcode.getRawValue();
                            showToast(rawValue, MessageType.SUCCESS);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Task failed with an exception
                        showToast("Error: Could not run the barcode recognition", MessageType.ERROR);
                    }
                });
    }

    private void runTextRecognition(Bitmap imageBitmap) {
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(imageBitmap);
        FirebaseVisionTextRecognizer firebaseVisionTextRecognizer = FirebaseVision.getInstance()
                .getOnDeviceTextRecognizer();
        firebaseVisionTextRecognizer.processImage(image)
                .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                    @Override
                    public void onSuccess(FirebaseVisionText firebaseVisionText) {
                        processTextRecognitionResult(firebaseVisionText);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                showToast("Error: Could not run the text recognition", MessageType.ERROR);
            }
        });
    }

    private LocalDate parseStringToDate(String dateAsString) {

        LocalDate localDate = null;

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
                showToast("Successfully recognition of the expire date", MessageType.SUCCESS);

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

        if (blocks.isEmpty()) {
            showToast("No text found", MessageType.ERROR);
            return;
        }

        StringBuilder recognisedWords = new StringBuilder();
        List<LocalDate> dateOptions = new ArrayList<>();

        for (int i = 0; i < blocks.size(); i++) {
            List<FirebaseVisionText.Line> lines = blocks.get(i).getLines();
            for (int j = 0; j < lines.size(); j++) {

                LocalDate date = this.parseStringToDate(lines.get(j).getText());

                if (date != null) {
                    recognisedWords.append("LINE: ")
                            .append(lines.get(j).getText());
                    recognisedWords.append(" | PARSED: ")
                            .append(date)
                            .append(" ");
                    recognisedWords
                            .append(System.getProperty("line.separator"));

                    dateOptions.add(date);
                }

                List<FirebaseVisionText.Element> elements = lines.get(j).getElements();
                for (int k = 0; k < elements.size(); k++) {

                    date = this.parseStringToDate(elements.get(k).getText());

                    if (date != null) {
                        recognisedWords
                                .append("ELEMENT: ")
                                .append(elements.get(k).getText());
                        recognisedWords
                                .append(" | PARSED: ")
                                .append(date)
                                .append(" ");
                        recognisedWords
                                .append(System.getProperty("line.separator"));

                        dateOptions.add(date);
                    }
                }

                recognisedWords.append(System.getProperty("line.separator"));
            }

            recognisedWords.append(System.getProperty("line.separator"));
        }

        if (dateOptions.isEmpty()) {
            showToast("Unsuccessful recognition of the expire date", MessageType.ERROR);

            mSpinner = findViewById(R.id.expire_date_options_id);
            // Create an ArrayAdapter using the string array and a default spinner layout
            ArrayAdapter<LocalDate> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, dateOptions);
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            // Apply the adapter to the spinner
            mSpinner.setAdapter(adapter);

            mTextView.setText(recognisedWords);
        }
    }

    private void showToast(String message, MessageType messageType) {
        Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
        View view = toast.getView();

        view.getBackground().setColorFilter(messageType == MessageType.ERROR ? Color.RED : Color.GREEN, PorterDuff.Mode.SRC_IN);

        toast.show();
    }

    private void clearTextView() {
        mTextView.setText("");

        if (mSpinner != null) {
            mSpinner.setAdapter(null);
        }
    }
}
