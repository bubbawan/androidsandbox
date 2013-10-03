package com.example.testit;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

@SuppressLint("NewApi")
public class MainActivity extends Activity {

	private File file;
	
	static final String[] FRUITS = new String[] { "Apple", "Avocado", "Banana",
		"Blueberry", "Coconut", "Durian", "Guava", "Kiwifruit",
		"Jackfruit", "Mango", "Olive", "Pear", "Sugar-apple" };
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.
        	    ThreadPolicy.Builder().permitAll().build();
        	    StrictMode.setThreadPolicy(policy); 
        setContentView(R.layout.activity_main);
        Button button = (Button) findViewById(R.id.button1);
        button.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Calendar cal = Calendar.getInstance();
				file = new File(Environment.getExternalStorageDirectory(),  ("test.jpg"));
			    if(!file.exists()){
			    try {
			        file.createNewFile();
			    } catch (IOException e) {
			    // TODO Auto-generated catch block
			        e.printStackTrace();
			    }
			    }else{
			       file.delete();
			    try {
			       file.createNewFile();
			    } catch (IOException e) {
			    // TODO Auto-generated catch block
			        e.printStackTrace();
			    }
			    }
			    
			    Uri capturedImageUri = Uri.fromFile(file);
			    Intent i = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
			    i.putExtra(MediaStore.EXTRA_OUTPUT, capturedImageUri);
			    startActivityForResult(i, 100);
			    
			/*	TextView text = (TextView) findViewById(R.id.textView2);
				text.setText(String.valueOf(System.currentTimeMillis()));
				Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			    startActivityForResult(takePictureIntent, 100); */

			}
		});
        
        ListView list = (ListView) findViewById(R.id.listView1);
        list.setAdapter(new ArrayAdapter<String>(this, R.layout.list_fruit,FRUITS));
        list.setTextFilterEnabled(true);
        
        list.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
			    // When clicked, show a toast with the TextView text
				
				TelephonyManager tMgr =(TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
				String mPhoneNumber = tMgr.getLine1Number();
				String deviceId = Secure.getString(getContentResolver(),
		                Secure.ANDROID_ID);
			    Toast.makeText(getApplicationContext(),
				deviceId, Toast.LENGTH_SHORT).show();
			}
		});
        
        button = (Button) findViewById(R.id.button2);
        button.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				 Toast.makeText(getApplicationContext(),
							readJSON(), Toast.LENGTH_SHORT).show();
			}
		});
    }
    
    public String readJSON() {
        StringBuilder builder = new StringBuilder();
        HttpClient client = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet("http://192.168.178.21:5000/user/check");
        try {
          HttpResponse response = client.execute(httpGet);
          StatusLine statusLine = response.getStatusLine();
          int statusCode = statusLine.getStatusCode();
          if (statusCode == 200) {
            HttpEntity entity = response.getEntity();
            InputStream content = entity.getContent();
            BufferedReader reader = new BufferedReader(new InputStreamReader(content));
            String line;
            while ((line = reader.readLine()) != null) {
              builder.append(line);
            }
          } else {
            Log.e(MainActivity.class.toString(), "Failed to download file");
          }
        } catch (ClientProtocolException e) {
          e.printStackTrace();
        } catch (IOException e) {
          e.printStackTrace();
        }
        return builder.toString();
      }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	new Uploader().execute();
    	
    }
    
    private class Uploader extends AsyncTask<Void, Void,Void>{

		@Override
		protected Void doInBackground(Void... params) {
			file = new File(Environment.getExternalStorageDirectory(),  ("test.jpg"));
	    	 HttpClient httpclient = new DefaultHttpClient();
			 HttpPost httppost = new HttpPost("http://192.168.178.21:5000/upload");
			  
			 try {
			   MultipartEntity entity = new MultipartEntity();
			  
			   entity.addPart("type", new StringBody("photo"));
			   entity.addPart("file", new FileBody(file));
			   httppost.setEntity(entity);
			   HttpResponse response = httpclient.execute(httppost);
			 } catch (ClientProtocolException e) {
			 } catch (IOException e) {
			 }
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			Toast.makeText(getApplicationContext(),
					"Image Uploaded!", Toast.LENGTH_SHORT).show();
		}
    	
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
}
