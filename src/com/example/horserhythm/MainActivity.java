package com.example.horserhythm;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;


import android.app.Activity;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements SensorEventListener {
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private CharSequence dataAsCharSeq="";
    private int counter=1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
        // Get an instance of the SensorManager
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        
        mAccelerometer=mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	//Called when the user clicks the Start button
	public void startAccelerometer (View view)
	{
		//Star the accelerometer
		 mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
	}
	
	public void stopAccelerometer (View view)
	{
		//Stop the accelerometer
		mSensorManager.unregisterListener(this);
	}
	
	public void saveToFile (View view)
	{
		
		try {
			File myFile = new File(Environment.getExternalStorageDirectory().getPath()+"/myHorseFile.txt"); // the external storage directory is the sdcard folder
			myFile.createNewFile();
			FileOutputStream fOut = new FileOutputStream(myFile);
			OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
			myOutWriter.append(dataAsCharSeq);
			myOutWriter.close();
			fOut.close();
			Toast.makeText(getBaseContext(),
					"Done writing SD 'myHorseFile.txt'",
					Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			Toast.makeText(getBaseContext(), e.getMessage(),
					Toast.LENGTH_SHORT).show();
		}
	}
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	// DON'T DO ANYTHING HERE
	}
	
	@Override
    public void onSensorChanged(SensorEvent event)
	{
		TextView xAccel= (TextView)findViewById(R.id.x_acceleration_text_view);
		TextView yAccel= (TextView)findViewById(R.id.y_acceleration_text_view);
		TextView zAccel= (TextView)findViewById(R.id.z_acceleration_text_view);
		xAccel.setText(""+event.values[0]);
		yAccel.setText(""+event.values[1]);
		zAccel.setText(""+event.values[2]);
		dataAsCharSeq=dataAsCharSeq+""+counter+".\t"+event.values[0]+"\t"+event.values[1]+"\t"+event.values[2]+"\n";
		counter++;
	}
	
	 public void selectInstructorMode(View view)
	    {
	    	//Do something in response to button
	    	Intent intent = new Intent(this, InstructorActivity.class);
	    	startActivity(intent);
	    }
	 
	 public void selectPatientMode(View view)
	    {
	    	//Do something in response to button
	    	Intent intent = new Intent(this, PatientActivity.class);
	    	startActivity(intent);
	    }
}
