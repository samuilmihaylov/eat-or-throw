package com.example.samuilmihaylov.eatorthrow.activities;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.samuilmihaylov.eatorthrow.R;
import com.example.samuilmihaylov.eatorthrow.enums.ImageCaptureActionType;
import com.example.samuilmihaylov.eatorthrow.enums.MessageType;
import com.example.samuilmihaylov.eatorthrow.models.Product;
import com.example.samuilmihaylov.eatorthrow.utils.NotificationLogger;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

import androidx.annotation.RequiresApi;

public class AddProductActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE_PRODUCT = 1;
    private static final int REQUEST_IMAGE_CAPTURE_EXPIRE_DATE = 2;

    private static final List<String> formatStrings = Arrays.asList(
            "MM/yy",
            "MM/dd/yy",
            "MM-dd-yy",
            "M yyyy",
            "MM yyyy",
            "MM/yyyy",
            "MM-dd-yyyy",
            "dd-MM-yy",
            "dd.MM.yy",
            "dd/MM/yyyy",
            "dd-MM-yyyy",
            "dd.MM.yyyy",
            "dd MM yyyy",
            "ddMMyyyy");
    String mPurchaseDate;
    String mExpiryDate;
    private ImageView mProductPhotoImage;
    private TextView mSelectedAlternativeDateTextView;
    private EditText mPurchaseDateEditTextView;
    private EditText mExpiryDateEditTextView;
    private Spinner mRecognizedDateOptionsSpinner;
    private Spinner mProductCategoryOptionsSpinner;
    private Bitmap mProductImageBitmap;

    public AddProductActivity() {
        // Required empty public constructor
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_product);

        Toolbar myToolbar = findViewById(R.id.action_toolbar);
        setSupportActionBar(myToolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mProductPhotoImage = findViewById(R.id.product_photo_image_id);
        mSelectedAlternativeDateTextView = findViewById(R.id.to_date_alternative_id);
        mRecognizedDateOptionsSpinner = findViewById(R.id.expire_date_options_id);
        mProductCategoryOptionsSpinner = findViewById(R.id.product_categories_options_id);

        this.initializeProductCategories();

        ImageButton snapProductButton = findViewById(R.id.snap_product_btn_id);
        snapProductButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent(ImageCaptureActionType.REQUEST_IMAGE_CAPTURE_PRODUCT);
            }
        });

        ImageButton snapExpireDateButton = findViewById(R.id.snap_expire_date_btn_id);
        snapExpireDateButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View view) {
//                clearTextView();
                dispatchTakePictureIntent(ImageCaptureActionType.REQUEST_IMAGE_CAPTURE_EXPIRE_DATE);
            }
        });

        final Calendar myCalendar = Calendar.getInstance();
        final DateTimeFormatter formatter = new DateTimeFormatterBuilder().appendPattern("dd/MM/yyyy").toFormatter();

        mPurchaseDateEditTextView = findViewById(R.id.from_date_id);
        final DatePickerDialog.OnDateSetListener purchaseDate = new DatePickerDialog.OnDateSetListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                mPurchaseDate = LocalDate.of(year, monthOfYear + 1, dayOfMonth).format(formatter);

                mPurchaseDateEditTextView.setText(mPurchaseDate);
            }
        };

        mPurchaseDateEditTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(AddProductActivity.this, purchaseDate, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        mExpiryDateEditTextView = findViewById(R.id.to_date_id);
        final DatePickerDialog.OnDateSetListener expiryDate = new DatePickerDialog.OnDateSetListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                mExpiryDate = LocalDate.of(year, monthOfYear + 1, dayOfMonth).format(formatter);

                mExpiryDateEditTextView.setText(mExpiryDate);
            }
        };

        mExpiryDateEditTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(AddProductActivity.this, expiryDate, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_bar_menu, menu);
        menu.findItem(R.id.action_add_new_product).setVisible(false);

        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_save_product:
                this.saveProduct();
                return true;

            case R.id.log_out:
                GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build();

                GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);
                FirebaseAuth.getInstance().signOut();
                mGoogleSignInClient.signOut();

                if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                    Intent intent = new Intent(this, LoginActivity.class);
                    startActivity(intent);
                }

                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void saveProduct() {

        EditText editProductNameView = findViewById(R.id.edit_product_name_text_id);
        EditText editAdditionalNoteView = findViewById(R.id.product_note_text_id);

        final String productName = editProductNameView.getText().toString();
        final String productCategory = mProductCategoryOptionsSpinner.getSelectedItem().toString();
//        String recognizedExpiryDateAlternative = mRecognizedDateOptionsSpinner.getSelectedItem().toString();
        final String additionalNote = editAdditionalNoteView.getText().toString();

        // TODO: Check if alternative date is chosen

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference databaseReference = database.getReference("products").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()));
        final String key = databaseReference.push().getKey();

        if (key != null) {
            FirebaseStorage storage = FirebaseStorage.getInstance();

            if (mProductImageBitmap != null) {
                final StorageReference storageRef = storage.getReference("products").child(FirebaseAuth.getInstance().getUid()).child(key + ".jpg");

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                mProductImageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                byte[] data = byteArrayOutputStream.toByteArray();

                UploadTask uploadTask = storageRef.putBytes(data);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // TODO: Handle this case
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        Product product = new Product(key, productName, productCategory, mPurchaseDate, mExpiryDate, additionalNote, storageRef.getPath());

                        databaseReference.child(key).setValue(product, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                                if (databaseError != null) {
                                    Snackbar snackbar = Snackbar.make(findViewById(R.id.add_product_layout), "Product could not be saved", Snackbar.LENGTH_SHORT);
                                    snackbar.getView().setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));
                                    snackbar.show();
                                } else {
                                    clearTextView();
                                    Snackbar snackbar = Snackbar.make(findViewById(R.id.add_product_layout), "Product saved", Snackbar.LENGTH_SHORT);
                                    snackbar.getView().setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
                                    snackbar.show();
                                }
                            }
                        });
                    }
                });
            } else {

                Product product = new Product(key, productName, productCategory, mPurchaseDate, mExpiryDate, additionalNote, null);

                databaseReference.child(key).setValue(product, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                        if (databaseError != null) {
                            Snackbar snackbar = Snackbar.make(findViewById(R.id.add_product_layout), "Product could not be saved", Snackbar.LENGTH_SHORT);
                            snackbar.getView().setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));
                            snackbar.show();
                        } else {
                            clearTextView();
                            Snackbar snackbar = Snackbar.make(findViewById(R.id.add_product_layout), "Product saved", Snackbar.LENGTH_SHORT);
                            snackbar.getView().setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
                            snackbar.show();
                        }
                    }
                });
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void dispatchTakePictureIntent(ImageCaptureActionType imageCaptureActionType) {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (imageCaptureActionType == ImageCaptureActionType.REQUEST_IMAGE_CAPTURE_PRODUCT) {
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE_PRODUCT);
            }

        } else if (imageCaptureActionType == ImageCaptureActionType.REQUEST_IMAGE_CAPTURE_EXPIRE_DATE) {
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE_EXPIRE_DATE);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE_PRODUCT && resultCode == Activity.RESULT_OK) {
            Bundle extras = data.getExtras();
            mProductImageBitmap = null;
            if (extras != null) {
                mProductImageBitmap = (Bitmap) extras.get("data");
            }

            mProductPhotoImage.setImageBitmap(mProductImageBitmap);
            mProductPhotoImage.setImageAlpha(255);

        } else if (requestCode == REQUEST_IMAGE_CAPTURE_EXPIRE_DATE && resultCode == Activity.RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = null;
            if (extras != null) {
                imageBitmap = (Bitmap) extras.get("data");
            }

            runTextRecognition(imageBitmap);
        }
    }

    private void runTextRecognition(Bitmap imageBitmap) {
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(imageBitmap);
        FirebaseVisionTextRecognizer firebaseVisionTextRecognizer = FirebaseVision.getInstance()
                .getOnDeviceTextRecognizer();
        firebaseVisionTextRecognizer.processImage(image)
                .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void onSuccess(FirebaseVisionText firebaseVisionText) {
                        processTextRecognitionResult(firebaseVisionText);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                NotificationLogger.showToast(getApplicationContext(), "Error: Could not run the text recognition", MessageType.ERROR);
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private String parseStringToDate(String dateAsString) {

        LocalDate localDate;

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

                NotificationLogger.showToast(getApplicationContext(), "Successfully recognition of the expire date", MessageType.SUCCESS);

                return localDate.format(
                        DateTimeFormatter.ofPattern(
                                "dd/MM/yyyy"));

            } catch (DateTimeParseException e) {
                Log.e("Expire date parsing", e.getParsedString());
            }
        }

        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void processTextRecognitionResult(FirebaseVisionText text) {
        List<FirebaseVisionText.TextBlock> blocks = text.getTextBlocks();

        if (blocks.isEmpty()) {
            NotificationLogger.showToast(getApplicationContext(), "No expiry date found", MessageType.ERROR);
            return;
        }

        List<String> dateOptions = new ArrayList<>();

        for (int i = 0; i < blocks.size(); i++) {
            List<FirebaseVisionText.Line> lines = blocks.get(i).getLines();
            for (int j = 0; j < lines.size(); j++) {

                String date = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    date = this.parseStringToDate(lines.get(j).getText());
                }

                if (date != null && !dateOptions.contains(date)) {
                    dateOptions.add(date);
                    mExpiryDateEditTextView.setText(date);

                    // TODO: Update datepicker
                }

                List<FirebaseVisionText.Element> elements = lines.get(j).getElements();
                for (int k = 0; k < elements.size(); k++) {

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        date = this.parseStringToDate(elements.get(k).getText());
                    }

                    if (date != null && !dateOptions.contains(date)) {
                        dateOptions.add(date);
                        mExpiryDateEditTextView.setText(date);

                        // TODO: Update datepicker
                    }
                }
            }
        }

        if (dateOptions.size() > 1) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, dateOptions);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mRecognizedDateOptionsSpinner.setAdapter(adapter);
            mSelectedAlternativeDateTextView.setText(dateOptions.get(0));
        }

        if (dateOptions.isEmpty()) {
            NotificationLogger.showToast(getApplicationContext(), "Unsuccessful recognition of the expire date", MessageType.ERROR);
        }
    }

    private void initializeProductCategories() {

        List<String> productCategories = new ArrayList<>(
                Arrays.asList(
                        "Dairy products",
                        "Fats and oils",
                        "Fruits and vegetables",
                        "Bakery wares",
                        "Meat and meat products",
                        "Fish and fish products",
                        "Eggs and egg products",
                        "Salts, spices, soups, sauces, salads, protein products",
                        "Beverages"
                ));

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, productCategories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mProductCategoryOptionsSpinner.setAdapter(adapter);
    }

    private void clearTextView() {
        ViewGroup group = findViewById(R.id.add_product_layout);
        for (int i = 0, count = group.getChildCount(); i < count; ++i) {
            View view = group.getChildAt(i);
            if (view instanceof EditText) {
                ((EditText) view).setText("");
            }
        }
    }
}
