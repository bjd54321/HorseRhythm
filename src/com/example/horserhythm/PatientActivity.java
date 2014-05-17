package com.example.horserhythm;

/*import pntanasis.android.metronome.Beats;
import pntanasis.android.metronome.Metronome;
import pntanasis.android.metronome.NoteValues;
import pntanasis.android.metronome.R;
import pntanasis.android.metronome.MetronomeActivity.MetronomeAsyncTask;*/


import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;


public class PatientActivity extends Activity implements SensorEventListener {

	private final short minBpm = 40;
	private final short maxBpm = 208;

	private short targetBPM = 100;
	private short noteValue = 4;
	private short beats = 1;
	private short volume;
	private short initialVolume;
	private double beatSound = 2440;//2
	private double sound = 6440;//6
	private AudioManager audio;
	private MetronomeAsyncTask metroTask;

	private Button plusButton;
	private Button minusButton;
	private TextView currentBeat;

	private Handler mHandler;

	private TextView measuredBPMTextView;
	private TextView BPMdifferenceTextView;

	private SensorManager mSensorManager;

	private Sensor mStepCounterSensor;

	private Sensor mStepDetectorSensor;
	private long timer = 0;
	private double measuredBPM = 0;


	// have in mind that: http://stackoverflow.com/questions/11407943/this-handler-class-should-be-static-or-leaks-might-occur-incominghandler
	// in this case we should be fine as no delayed messages are queued
	private Handler getHandler() {
		return new Handler() {
			@Override
			public void handleMessage(Message msg) {
				String message = (String)msg.obj;
				if(message.equals("1"))
					currentBeat.setTextColor(Color.GREEN);
				else
					currentBeat.setTextColor(getResources().getColor(R.color.yellow));
				currentBeat.setText(message);
			}
		};
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {    	
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_patient);
		metroTask = new MetronomeAsyncTask();
		/* Set values and listeners to buttons and stuff */

		TextView bpmText = (TextView) findViewById(R.id.bps);
		bpmText.setText(""+targetBPM);

		TextView timeSignatureText = (TextView) findViewById(R.id.timesignature);
		timeSignatureText.setText(""+beats+"/"+noteValue);

		plusButton = (Button) findViewById(R.id.plus);
		plusButton.setOnLongClickListener(plusListener);

		minusButton = (Button) findViewById(R.id.minus);
		minusButton.setOnLongClickListener(minusListener);

		currentBeat = (TextView) findViewById(R.id.currentBeat);
		currentBeat.setTextColor(Color.GREEN);

		Spinner beatSpinner = (Spinner) findViewById(R.id.beatspinner);
		ArrayAdapter<Beats> arrayBeats =
				new ArrayAdapter<Beats>(this,
						android.R.layout.simple_spinner_item, Beats.values());
		beatSpinner.setAdapter(arrayBeats);
		beatSpinner.setSelection(Beats.one.ordinal());
		arrayBeats.setDropDownViewResource(R.layout.spinner_dropdown);
		beatSpinner.setOnItemSelectedListener(beatsSpinnerListener);

		Spinner noteValuesdSpinner = (Spinner) findViewById(R.id.notespinner);
		ArrayAdapter<NoteValues> noteValues =
				new ArrayAdapter<NoteValues>(this,
						android.R.layout.simple_spinner_item, NoteValues.values());
		noteValuesdSpinner.setAdapter(noteValues);
		noteValues.setDropDownViewResource(R.layout.spinner_dropdown);
		noteValuesdSpinner.setOnItemSelectedListener(noteValueSpinnerListener);

		audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

		initialVolume = (short) audio.getStreamVolume(AudioManager.STREAM_MUSIC);
		volume = initialVolume;

		SeekBar volumebar = (SeekBar) findViewById(R.id.volumebar);
		volumebar.setMax(audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
		volumebar.setProgress(volume);
		volumebar.setOnSeekBarChangeListener(volumeListener);

		measuredBPMTextView = (TextView) findViewById(R.id.measuredBPM);
		BPMdifferenceTextView = (TextView) findViewById(R.id.BPMdiffTextView);

		mSensorManager = (SensorManager)        
				getSystemService(Context.SENSOR_SERVICE);
		mStepCounterSensor = mSensorManager
				.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
		mStepDetectorSensor = mSensorManager
				.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public synchronized void onStartStopClick(View view) {
		Button button = (Button) view;
		String buttonText = button.getText().toString();
		if(buttonText.equalsIgnoreCase("start")) {
			button.setText(R.string.stop);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
				metroTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[])null);
			else
				metroTask.execute();    		
		} else {
			button.setText(R.string.start);    	
			metroTask.stop();
			metroTask = new MetronomeAsyncTask();
			Runtime.getRuntime().gc();
		}
	}

	private void maxBpmGuard() {
		if(targetBPM >= maxBpm) {
			plusButton.setEnabled(false);
			plusButton.setPressed(false);
		} else if(!minusButton.isEnabled() && targetBPM>minBpm) {
			minusButton.setEnabled(true);
		}    	
	}

	public void onPlusClick(View view) {
		targetBPM++;
		TextView bpmText = (TextView) findViewById(R.id.bps);
		bpmText.setText(""+targetBPM);
		metroTask.setBpm(targetBPM);
		maxBpmGuard();
	}

	private OnLongClickListener plusListener = new OnLongClickListener() {

		@Override
		public boolean onLongClick(View v) {
			// TODO Auto-generated method stub
			targetBPM+=20;
			if(targetBPM >= maxBpm)
				targetBPM = maxBpm;
			TextView bpmText = (TextView) findViewById(R.id.bps);
			bpmText.setText(""+targetBPM);
			metroTask.setBpm(targetBPM);
			maxBpmGuard();
			return true;
		}

	};

	private void minBpmGuard() {
		if(targetBPM <= minBpm) {
			minusButton.setEnabled(false);
			minusButton.setPressed(false);
		} else if(!plusButton.isEnabled() && targetBPM<maxBpm) {
			plusButton.setEnabled(true);
		}    	
	}

	public void onMinusClick(View view) {
		targetBPM--;
		TextView bpmText = (TextView) findViewById(R.id.bps);
		bpmText.setText(""+targetBPM);
		metroTask.setBpm(targetBPM);
		minBpmGuard();
	}

	private OnLongClickListener minusListener = new OnLongClickListener() {

		@Override
		public boolean onLongClick(View v) {
			// TODO Auto-generated method stub
			targetBPM-=20;
			if(targetBPM <= minBpm)
				targetBPM = minBpm;
			TextView bpmText = (TextView) findViewById(R.id.bps);
			bpmText.setText(""+targetBPM);
			metroTask.setBpm(targetBPM);
			minBpmGuard();
			return true;
		}

	};

	private OnSeekBarChangeListener volumeListener = new OnSeekBarChangeListener() {

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			// TODO Auto-generated method stub
			volume = (short) progress;
			audio.setStreamVolume(AudioManager.STREAM_MUSIC, progress, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
		}   	

	};

	private OnItemSelectedListener beatsSpinnerListener = new OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			// TODO Auto-generated method stub
			Beats beat = (Beats) arg0.getItemAtPosition(arg2);
			TextView timeSignature = (TextView) findViewById(R.id.timesignature);
			timeSignature.setText(""+beat+"/"+noteValue);
			metroTask.setBeat(beat.getNum());
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub

		}

	};

	private OnItemSelectedListener noteValueSpinnerListener = new OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			// TODO Auto-generated method stub
			NoteValues noteValue = (NoteValues) arg0.getItemAtPosition(arg2);
			TextView timeSignature = (TextView) findViewById(R.id.timesignature);
			timeSignature.setText(""+beats+"/"+noteValue);
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub

		}

	};

	@Override
	public boolean onKeyUp(int keycode, KeyEvent e) {
		SeekBar volumebar = (SeekBar) findViewById(R.id.volumebar);
		volume = (short) audio.getStreamVolume(AudioManager.STREAM_MUSIC);
		switch(keycode) {
		case KeyEvent.KEYCODE_VOLUME_UP:
		case KeyEvent.KEYCODE_VOLUME_DOWN: 
			volumebar.setProgress(volume);
			break;                
		}

		return super.onKeyUp(keycode, e);
	}

	public void onBackPressed() {
		metroTask.stop();
		//    	metroTask = new MetronomeAsyncTask();
		Runtime.getRuntime().gc();
		audio.setStreamVolume(AudioManager.STREAM_MUSIC, initialVolume, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
		finish();    
	}

	private class MetronomeAsyncTask extends AsyncTask<Void,Void,String> {
		Metronome metronome;

		MetronomeAsyncTask() {
			mHandler = getHandler();
			metronome = new Metronome(mHandler);
		}

		protected String doInBackground(Void... params) {
			metronome.setBeat(beats);
			metronome.setNoteValue(noteValue);
			metronome.setBpm(targetBPM);
			metronome.setBeatSound(beatSound);
			metronome.setSound(sound);

			metronome.play();

			return null;			
		}

		public void stop() {
			metronome.stop();
			metronome = null;
		}

		public void setBpm(short bpm) {
			metronome.setBpm(bpm);
			metronome.calcSilence();
		}

		public void setBeat(short beat) {
			if(metronome != null)
				metronome.setBeat(beat);
		}

	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		long timerdiff=0;
		int BPMdifference=0;
		int lastVal=0;
		int stepdiff=0;
		Sensor sensor = event.sensor;
		float[] values = event.values;
		int value=-1;

		if (values.length > 0) {
			value = (int) values[0];
//			stepdiff= value-lastVal;
//			lastVal=value;
		}

		if (sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
			measuredBPMTextView.setText("Step Counter Detected : " + value);
			if(timer==0)
				timer=event.timestamp;
			else
			{
				timerdiff=event.timestamp-timer;
				timer=event.timestamp;
			}
			if(timerdiff!=0)
				// Make sure that the sensor counts only 1 step per sensor change
				// Otherwise, the bpm should be calculated by multiplying by the number of steps in that event
				measuredBPM = 1.0 / (timerdiff*1.0 * (1.0/1000000000) * (1.0/60)); // dividing by a a billion because the time is in nanoseconds, dividing by 60 to get the time in minutes
			measuredBPMTextView.append("BPM: "+ (int) measuredBPM);
			BPMdifference=(int)(measuredBPM-targetBPM);
			BPMdifferenceTextView.setText(""+BPMdifference );
			if(Math.abs(BPMdifference)<=20){
				BPMdifferenceTextView.setTextColor(Color.GREEN);	
			}else{
				BPMdifferenceTextView.setTextColor(Color.RED);
			}
			
		} else if (sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
			// For test only. Only allowed value is 1.0 i.e. for step taken
			measuredBPMTextView.setText("Step Detector Detected : " + value);
		}

	}

	protected void onResume() {

		super.onResume();

		mSensorManager.registerListener(this, mStepCounterSensor,

				SensorManager.SENSOR_DELAY_FASTEST);      
		mSensorManager.registerListener(this, mStepDetectorSensor,

				SensorManager.SENSOR_DELAY_FASTEST);

	}

	protected void onStop() {
		super.onStop();
		mSensorManager.unregisterListener(this, mStepCounterSensor);
		mSensorManager.unregisterListener(this, mStepDetectorSensor);
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub

	}
	
	public void WiFiStamFunction(){
		
	}


}
