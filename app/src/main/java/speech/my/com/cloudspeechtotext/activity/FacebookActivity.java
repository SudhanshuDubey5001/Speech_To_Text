package speech.my.com.cloudspeechtotext.activity;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.api.client.util.Base64;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.kaopiz.kprogresshud.KProgressHUD;

import java.io.IOException;
import java.io.InputStream;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.apache.commons.io.IOUtils;

import speech.my.com.cloudspeechtotext.adapter.OutputAdapter;
import speech.my.com.cloudspeechtotext.database.Architecture;
import speech.my.com.cloudspeechtotext.database.OutputSQLDatabase;
import speech.my.com.cloudspeechtotext.model.Conversion;
import speech.my.com.cloudspeechtotext.R;
import speech.my.com.cloudspeechtotext.other.LogHandler;
import speech.my.com.cloudspeechtotext.other.Output;
import speech.my.com.cloudspeechtotext.other.Trigger;

public class FacebookActivity extends AppCompatActivity implements Trigger {

    private final static int REQ_CODE = 1;
    private final static String EMAIL = "email_text";
    private final static String PASS = "pass_text";
    private static FirebaseAuth auth;

    DrawerLayout drawerLayout;  //for navigation drawer
    KProgressHUD hud;       //custom hud for waiting
    String evalue = EMAIL;  //to track the focused edit field
    AlertDialog.Builder dialog;
    OutputSQLDatabase database;
    NavigationView navigationView;
    Intent in;

    //  made object here to protect it from garbage collector--->
    MediaPlayer player = new MediaPlayer();


    boolean mode = true;
    EditText emailField;         //email field
    EditText passField;          //password field
    Output o;
    LogHandler log;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_facebooklogin);

//      setting the title------------------>
        setTitle("Login");

//      setting up log---------------------->
        log = new LogHandler(this);
        log.writeLog("[FacebookActivity started]--------------\n\n");

//      setting up database----------------------------->
        database = new OutputSQLDatabase(this);

//      setting up navigation drawer--------------->
        setupNavigationDrawer();

//      Set up Firebase auth object----------->
        auth = null;                          //signing out existing user
        auth = FirebaseAuth.getInstance();    //getting new user

//      update evalue as user touched email field------------>
        emailField = findViewById(R.id.email);
        emailField.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                evalue = EMAIL;
                return false;
            }
        });

//      update evalue as user touched password field------------>
        passField = findViewById(R.id.pass);
        passField.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                evalue = PASS;
                return false;
            }
        });

//      for sign up--------------------------------------------------------------->
        Button signUp = findViewById(R.id.signUp);
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailField.getText().toString();
                String pass = passField.getText().toString();
                log.writeLog("[SignUp button pressed]\n");
                if (validate(email, pass)) {
                    signUp(email, pass);
                }
            }
        });

//      for log in and output storage---------------------------------------------->
        if (getIntent().getSerializableExtra(OutputAdapter.OUTPUT_OBJECT) != null) {
//          for updating old entry--------------------------------------->
            Button loginThenUpdate = findViewById(R.id.login_fb);
            o = (Output) getIntent().getSerializableExtra(OutputAdapter.OUTPUT_OBJECT);
            setFields(o);
            loginThenUpdate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String email = emailField.getText().toString();
                    String pass = passField.getText().toString();
                    log.writeLog("[Login button pressed]\n");
                    if (validate(email, pass)) {
                        login(email, pass);
                        updateDatabase(email, pass, o);
                        log.writeLog("[Database updated] from--->\n"
                                + o.output_email +
                                "\n" + o.output_pass
                                + "\nto--->\n" + email
                                + "\n" + pass + "\n");
                    }
                }
            });
        } else {
//          for creating new entry------------------------------------>
            Button loginThenCreate = findViewById(R.id.login_fb);
            loginThenCreate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String email = emailField.getText().toString();
                    String pass = passField.getText().toString();
                    log.writeLog("[Login button pressed]\n");
                    if (validate(email, pass)) {
                        login(email, pass);
                        writeIntoDatabase(email, pass);
                        log.writeLog("[Database created] --->\n"
                                + email
                                + "\n" + pass + "\n");
                    }
                }
            });
        }

//      setting up hud------------->
        hud = KProgressHUD.create(this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setAnimationSpeed(3)
                .setLabel("Uploading")
                .setCancellable(true);

        //      File picker-------------------->
        Button select = findViewById(R.id.select_file);
        select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                log.writeLog("[Upload file button pressed]\n");

                dialog = new AlertDialog.Builder(FacebookActivity.this);
                dialog.setTitle("Alert")
                        .setMessage("File must be .flac or .wav with only 1 channel or " +
                                "try the samples from navigation pane")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent filepick = new Intent(Intent.ACTION_GET_CONTENT);
                                filepick.setType("audio/*");
                                startActivityForResult(filepick, REQ_CODE);
                            }
                        }).show();
            }
        });

//      running sample-------------------------------------->
        int num = getIntent().getIntExtra(SampleActivity.RES_ID, 0);
        if (num != 0) {
            playAudio(null, num);
            InputStream stream = getResources().openRawResource(num);
            try {
                convertAndStartProcess(stream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //  validate for email and password function----------------------->
    private boolean validate(String email, String pass) {
        AlertDialog.Builder errorDialog = new AlertDialog.Builder(this);
        errorDialog.setTitle("Error")
                .setMessage("Cannot leave blank")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        if (email.equals("") || pass.equals("")) {
            log.writeLog("[Validation error]\n");
            errorDialog.show();
            return false;
        }
        log.writeLog("[Validation successful]\n");
        return true;
    }

    //   sign up function------------------------------------->
    private void signUp(String email, String pass) {
        hud.setLabel("Signing up").show();
        dialog = new AlertDialog.Builder(this);
        auth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            log.writeLog("SignUp successful!!!\n");
                            dialog.setTitle("Success")
                                    .setMessage("You successfully signed up in")
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            hud.dismiss();
                                        }
                                    }).show();
                        } else {
                            log.writeLog("Signup failed :(:(\n");
                            dialog.setTitle("Failed")
                                    .setMessage(task.getException().getMessage())
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            hud.dismiss();
                                        }
                                    }).show();
                        }
                    }
                });

//      clearing the edit fields once login process started---------------->
        EditText e = findViewById(R.id.email);
        e.setText(null);
        EditText p = findViewById(R.id.pass);
        p.setText(null);
    }

    //  login function----------------------------------->
    private void login(String email, String pass) {
        hud.setLabel("Logging in").show();
        dialog = new AlertDialog.Builder(this);
        auth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        log.writeLog("Login process started....\n");
                        if (task.isSuccessful()) {
                            log.writeLog("Login successful!!!\n");
                            dialog.setTitle("Success")
                                    .setMessage("You successfully logged in")
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            hud.dismiss();
                                        }
                                    }).show();
                        } else {
                            log.writeLog("Login failed :(\n");
                            dialog.setTitle("Failed")
                                    .setMessage(task.getException().getMessage())
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            hud.dismiss();
                                        }
                                    }).show();
                        }
                    }
                });

//      clearing the edit fields once login process started---------------->
        EditText e = findViewById(R.id.email);
        e.setText(null);
        EditText p = findViewById(R.id.pass);
        p.setText(null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
//          get the audio file------------->
            Uri audioURI = data.getData();

//          play the audio file------------------>
            playAudio(audioURI, 0);

//          converting to base64 and starting the conversion process in different thread------------>
            try {
                InputStream stream;
                stream = getContentResolver().openInputStream(audioURI);
                convertAndStartProcess(stream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
//      Speech----->TEXT ------------------------->
//        AsyncTask.execute(new Runnable() {
//            @Override
//            public void run() {
////                Log.d("my", "chal raha hai");
//////              get the whole address location of audio file------------>
////                InputStream stream;
////                String base64SOUND;
////                try (SpeechClient speechClient = SpeechClient.create()) {
////                    stream = getContentResolver().openInputStream(audioURI);
////                    byte[] audioBytes = IOUtils.toByteArray(stream);
////                    ByteString audioString = ByteString.copyFrom(audioBytes);
////                    stream.close();
////                    Log.d("my", "1");
////
//////                  change to base64 format------->
//////                    base64SOUND = Base64.encodeToString(audioBytes, 1);
////
//////              setting up speech object with GCP key---------------->
//////                    Speech speechService = new Speech.Builder(
//////                            AndroidHttp.newCompatibleTransport(),
//////                            new AndroidJsonFactory(),
//////                            null
//////                    ).setSpeechRequestInitializer(
//////                            new SpeechRequestInitializer(GCP_KEY))
//////                            .build();
////                    Log.d("my", "2");
////
//////              1. Setting up the language of audio file--------->
////                    RecognitionConfig config = RecognitionConfig.newBuilder()
////                            .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
////                            .setSampleRateHertz(16000)
////                            .setLanguageCode("en-US")
////                            .build();
////
//////              2. Wrapping audioString object file in RecognitionAudio object----->
////                    RecognitionAudio audio = RecognitionAudio.newBuilder()
////                            .setContent(audioString).build();
////                    Log.d("my", "3");
////
//////              Creating SyncRecognize Request object 1 and 2----------->
//////                    SyncRecognizeRequest request = new SyncRecognizeRequest();
//////                    request.setAudio(audio);
//////                    request.setConfig(config);
//////                    Log.d("my","4");
////
//////                  Generating Response-------->
////                    RecognizeResponse audioResponse = speechClient.recognize(config, audio);
////                    List<SpeechRecognitionResult> response = audioResponse.getResultsList();
////                    Log.d("my", "5");
////
//////                  Catching result----------->
////                    for (SpeechRecognitionResult result : response) {
////                        transcribedTEXT = result.getAlternativesList().get(0).getTranscript();
////                    }
////                    Log.d("my", "6");
////
//////                  setting up text------------>
////                    setText(transcribedTEXT);
////                    hud.dismiss();
////                    Log.d("my", "7");
////                } catch (IOException e) {
////                    e.printStackTrace();
////                }
//
//                hud.show();
//                try (SpeechClient speechClient = SpeechClient.create()) {
//
//                    // The path to the audio file to transcribe
////                    String fileName = "./resources/audio.raw";
//
//                    // Reads the audio file into memory
////                    Path path = Paths.get(fileName);
////                    byte[] data = Files.readAllBytes(path);
//
//                    InputStream stream = getContentResolver().openInputStream(audioURI);
//                    assert stream != null;
//                    byte[] audioBytes = IOUtils.toByteArray(stream);
//                    ByteString audioString = ByteString.copyFrom(audioBytes);
//
//                    // Builds the sync recognize request
//                    RecognitionConfig config = RecognitionConfig.newBuilder()
//                            .setEncoding(AudioEncoding.LINEAR16)
//                            .setSampleRateHertz(16000)
//                            .setLanguageCode("en-US")
//                            .build();
//                    RecognitionAudio audio = RecognitionAudio.newBuilder()
//                            .setContent(audioString)
//                            .build();
//
//                    // Performs speech recognition on the audio file
//                    RecognizeResponse response = speechClient.recognize(config, audio);
//                    List<SpeechRecognitionResult> results = response.getResultsList();
//
//                    for (SpeechRecognitionResult result : results) {
//                        // There can be several alternative transcripts for a given chunk of speech. Just use the
//                        // first (most likely) one here.
//                        SpeechRecognitionAlternative alternative = result.getAlternativesList().get(0);
////                        System.out.printf("Transcription: %s%n", alternative.getTranscript());
//                        transcribedTEXT=alternative.getTranscript();
//                    }
//
////                    setting text------>
//                    setText(transcribedTEXT);
//                    hud.dismiss();
//
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//
//            }
//        });
    }

    //  setting up transcribed text----------->
    public void setText(String text) {
        hud.dismiss();
        if (evalue.equals(EMAIL)) {
            emailField.setText(text);
        } else {
            passField.setText(text);
        }
    }

    //  catching the trigger----------------->
    @Override
    public void sendData(String text) {
        setText(text);
        log.writeLog("[----------DATA RECEIVED---------]\n"
                + "Data:\n" + text + "\n");
    }

    //  navigation drawer------------------------------------------>
    private void setupNavigationDrawer() {
        drawerLayout = findViewById(R.id.drawer_layout);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.menu_icon);

        navigationView = findViewById(R.id.nav);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                // Handle navigation view item clicks here.
                int id = item.getItemId();
                Intent in = null;
                if (id == R.id.dashboard) {
                    log.writeLog("Dashboard activity starting.....\n");
                    in = new Intent(FacebookActivity.this, DashboardActivity.class);
                } else if (id == R.id.output) {
                    log.writeLog("OutputActivity starting......\n");
                    in = new Intent(FacebookActivity.this, OutputActivity.class);
                } else if (id == R.id.sample) {
                    log.writeLog("SampleActivity starting......\n");
                    in = new Intent(FacebookActivity.this, SampleActivity.class);
                } else if (id == R.id.Log) {
                    log.writeLog("LogActivity starting.......\n");
                    in = new Intent(FacebookActivity.this, LogActivity.class);
                }
                startActivity(in);
                FacebookActivity.this.finish();
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });
    }

    //  close the drawer when back is pressed----------------------->
    @Override
    public void onBackPressed() {
        log.writeLog("[Back pressed]\n[Terminating App]\n");
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    //  open the drawer when button is tapped------------------->
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            drawerLayout.openDrawer(GravityCompat.START);
        }
        return super.onOptionsItemSelected(item);
    }

    //   output writing in database function------------------->
    private void writeIntoDatabase(String email, String pass) {

        SQLiteDatabase db = database.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(Architecture.COL_EMAIl, email);
        values.put(Architecture.COL_PASS, pass);

        db.insert(Architecture.TABLE_NAME, null, values);
        db.close();
    }

    //   for updating fields----------------------->
    public void setFields(Output o) {
        EditText e = findViewById(R.id.email);
        e.setText(o.output_email);
        EditText p = findViewById(R.id.pass);
        p.setText(o.output_pass);
    }

    //   update the database---------------------->
    private void updateDatabase(String email, String pass, Output o) {
        SQLiteDatabase db = database.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Architecture.COL_EMAIl, email);
        values.put(Architecture.COL_PASS, pass);
        String[] identifier = {String.valueOf(o.output_number)};
        db.update(Architecture.TABLE_NAME, values, Architecture._ID + "=?", identifier);
        db.close();
    }

    //  converting and sending for transcribtion--------------->
    private void convertAndStartProcess(InputStream stream) throws IOException {
        hud.show();
        byte[] audioBytes = IOUtils.toByteArray(stream);
        stream.close();

//      converting audio file to base64---------------------->
        String base64Audio = Base64.encodeBase64String(audioBytes);

        Conversion c = new Conversion();
        c.setter(base64Audio, this);
        c.execute();
    }

    //  play the audio--------->
    private void playAudio(Uri audioURI, int resId) {
        if (resId == 0) {
            try {
                player.setDataSource(FacebookActivity.this, audioURI);
                player.prepare();
                player.start();
                log.writeLog("[Audio started playing]\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            player = MediaPlayer.create(this, resId);
            player.start();
            log.writeLog("[Audio started playing]\n");
        }

        //release the player object on completion----------->
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.release();
                player = new MediaPlayer();
                log.writeLog("[Audio complete]\n");
            }
        });
    }
}
