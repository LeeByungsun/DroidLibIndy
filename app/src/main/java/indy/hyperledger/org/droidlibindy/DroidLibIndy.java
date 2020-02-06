package indy.hyperledger.org.droidlibindy;

import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Handler;
import android.os.HandlerThread;
import android.system.ErrnoException;
import android.system.Os;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import org.hyperledger.indy.sdk.IndyException;
import org.hyperledger.indy.sdk.LibIndy;
import org.hyperledger.indy.sdk.anoncreds.Anoncreds;
import org.hyperledger.indy.sdk.did.Did;
import org.hyperledger.indy.sdk.did.DidResults;
import org.hyperledger.indy.sdk.wallet.Wallet;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.concurrent.ExecutionException;

import indy.hyperledger.org.droidlibindy.util.PrintLog;

import static indy.hyperledger.org.droidlibindy.AnoncredsIntegration.issuer1GvtCredOffer;
import static indy.hyperledger.org.droidlibindy.AnoncredsIntegration.issuer1GvtCredReq;

public class DroidLibIndy extends AppCompatActivity{

    private View contentView = null;
    private Wallet wallet = null;
    private AnoncredsIntegration integrationTest;
    private String TAG = "DroidLibIndy";

    private HandlerThread handlerThread;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        File dataDir = getApplicationContext().getDataDir();
        Log.e(TAG, "datadir=" + dataDir.getAbsolutePath());
        File externalFilesDir = getExternalFilesDir(null);
        String path = externalFilesDir.getAbsolutePath();
        Log.e(TAG, "axel externalFilesDir=" + path);

        try{
            Os.setenv("EXTERNAL_STORAGE", path, true);
        }catch(ErrnoException e){
            e.printStackTrace();
        }

        File[] files = externalFilesDir.listFiles();
        for(int i = 0; i < files.length; ++i){
            File file = files[i];
            if(file.isDirectory()){
                Log.e(TAG, "axel directory:" + file.getName());
                if(".indy_client".equals(file.getName())){
                    String[] children = file.list();
                    for(int j = 0; j < children.length; j++){
                        Log.e(TAG, "axel deleting:" + children[j]);
                        new File(file, children[j]).delete();
                    }
                }
            }else{
                Log.e(TAG, "axel file     :" + file.getName());
            }
        }

        LibIndy.init();

        setContentView(R.layout.activity_droid_lib_indy);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        contentView = (View) findViewById(R.id.content_view);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                Wallet wallet = null;

                try{
                    final String WALLET = "Wallet1";
                    final String TYPE = "default";
                    final String WALLET_CREDENTIALS =
                            new JSONObject()
                                    .put("key", "key")
                                    .toString();
                    final String WALLET_CONFIG =
                            new JSONObject()
                                    .put("id", WALLET)
                                    .put("storage_type", TYPE)
                                    .toString();
                    try{
                        Wallet.createWallet(WALLET_CONFIG, WALLET_CREDENTIALS).get();
                    }catch(ExecutionException e){
                        Log.e(TAG, e.getMessage());
                        if(e.getMessage().indexOf("WalletExistsException") >= 0){
                            // ignore
                        }else{
                            throw new RuntimeException(e);
                        }
                    }
                    wallet = Wallet.openWallet(WALLET_CONFIG, WALLET_CREDENTIALS).get();
                    Log.e(TAG, "===================> wallet:" + wallet);

                    DidResults.CreateAndStoreMyDidResult myDidResult = Did.createAndStoreMyDid(wallet, "{}").get();
                    String myDid = myDidResult.getDid();
                    Snackbar.make(view, "My DID:" + myDid, Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();

                    String key = Did.keyForLocalDid(wallet, myDid).get();
                    Snackbar.make(view, "keyForLocalDid:" + key, Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    Log.e(TAG, "keyForLocalDid:" + key);
                }catch(IndyException e){
                    e.printStackTrace();
                }catch(JSONException e){
                    e.printStackTrace();
                }catch(InterruptedException e){
                    e.printStackTrace();
                }catch(ExecutionException e){
                    e.printStackTrace();
                }finally{
                    if(wallet != null){
                        try{
                            wallet.closeWallet().get();
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                    }
                }

            }
        });

        initUi();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_droid_lib_indy, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if(id == R.id.action_settings){
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initUi(){
        Button integration = contentView.findViewById(R.id.integration);
        Button createCredential = contentView.findViewById(R.id.createCredential);
        Button createProof = contentView.findViewById(R.id.creatProof);
        Button verifyProof = contentView.findViewById(R.id.verifyProof);
        Button anoncred = contentView.findViewById(R.id.anoncreds);


        integration.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                anoncredsIntegration();
            }
        });

        createCredential.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                createCredential();
            }
        });

        createProof.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                createProof();
            }
        });

        verifyProof.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                verifyProof();
            }
        });

        anoncred.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                handlerThread = new HandlerThread("Test");
                handlerThread.start();
                new Handler(handlerThread.getLooper()).post(new Runnable(){
                    @Override
                    public void run(){
                        try{
                            AnoncredsTest.demo();
                        }catch(Exception e){
                            PrintLog.e("exception = " + e.getMessage());
                            e.printStackTrace();

                        }
                    }
                });
            }
        });

    }


    private void anoncredsIntegration(){
        try{
            integrationTest = new AnoncredsIntegration();
        }catch(JSONException e){
            e.printStackTrace();
            Log.e(TAG, "AnoncredsIntegration error = " + e.getMessage());
        }
        try{
            integrationTest.initCommonWallet();
        }catch(Exception e){
            e.printStackTrace();
            Log.e(TAG, "initCommonWallet error = " + e.getMessage());
        }

    }

    private void createCredential(){
        String credValues = "{" +
                "        \"sex\":\"male\",\n" +
                "        \"age\":\"28\"" +
                "       }";

        try{
            Anoncreds.issuerCreateCredential(integrationTest.wallet, integrationTest.issuer1GvtCredOffer, integrationTest.issuer1GvtCredReq, credValues, null, -1).get();
        }catch(ExecutionException e){
            e.printStackTrace();
            Log.e(TAG, "issuerCreateCredential error = " + e.getMessage());
        }catch(InterruptedException e){
            e.printStackTrace();
            Log.e(TAG, "issuerCreateCredential error = " + e.getMessage());
        }catch(IndyException e){
            e.printStackTrace();
            Log.e(TAG, "issuerCreateCredential error = " + e.getMessage());
        }
    }

    private void verifyProof(){
        try{
            new VerifierVerifyProof();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void createProof(){
        try{
            new ProverCreateProof();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public static long getCurrentTime(){
        long time = System.currentTimeMillis();
        return time;
    }


}
