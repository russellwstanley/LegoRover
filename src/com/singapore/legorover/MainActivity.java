/*INSERT YOUR PACKAGE NAME*/
package com.singapore.legorover;

import java.io.IOException;
import java.util.Set;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.DragShadowBuilder;
import android.view.View.OnDragListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Android SDK Tutorial - Drag and Drop Mobiletuts+
 * 
 * Sue Smith January 2013
 */

public class MainActivity extends Activity implements BTConnectable {

	private enum ListItemType {
		Move, Rotate, RotateCamera, TakePhoto
	}

	// TODO consider subclassing rather than enums
	private class ListItem {

		private ListItemType type;
		private CharSequence value;

		public ListItem(ListItemType type) {
			this.type = type;
		}

		public ListItemType getType() {
			return type;
		}

		public CharSequence getValue() {
			return value;
		}

		public TextWatcher getWatcher() {
			return new TextWatcher() {

				@Override
				public void afterTextChanged(Editable arg0) {
					// TODO Auto-generated method stub
				}

				@Override
				public void beforeTextChanged(CharSequence arg0, int arg1,
						int arg2, int arg3) {
					// TODO Auto-generated method stub
				}

				@Override
				public void onTextChanged(CharSequence arg0, int arg1,
						int arg2, int arg3) {
					value = arg0;
				}

			};
		}

	}

	public class MyArrayAdapter extends ArrayAdapter<ListItem> {

		public MyArrayAdapter(Context context) {
			super(context, android.R.layout.simple_list_item_1);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LinearLayout layout;
			ListItem item = getItem(position);
			if (item.getType() == ListItemType.TakePhoto) {
				layout = (LinearLayout) inflater.inflate(R.layout.plain_row,
						parent, false);
			} else {
				layout = (LinearLayout) inflater.inflate(R.layout.number_row,
						parent, false);
			}
			// TODO this is s**t look into subclassing layout
			TextView text = (TextView) layout
					.findViewById(R.id.list_option_text);
			text.setText(item.getType().toString());
			EditText edit = (EditText) layout.findViewById(R.id.editText1);
			if (edit != null) {
				edit.addTextChangedListener(item.getWatcher());
			}
			return layout;
		}
	}

	// text views being dragged and dropped onto
	private TextView option1, option2, option3, option4;
	private ListView dropzone;
	MyArrayAdapter adapter;
	private LayoutInflater inflater;
	public static final int REQUEST_ENABLE_BLUETOOTH = 1;
	public static final String ROVER_BLUETOOTH_NAME = "White";

	private BluetoothAdapter bluetoothAdapter;
	BTCommunicator communicator;

	private Handler btcHandler;

	private int cameraMotor = BTCommunicator.MOTOR_A;

	private int motorLeft = BTCommunicator.MOTOR_B;

	private int motorRight = BTCommunicator.MOTOR_C;

	private int directionLeft = 1;

	private int directionRight = 1;
	
	private float angleFudgeFactor = 5.88f;

	public void connect_Click(View view) {
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (bluetoothAdapter == null) {
			Toast toast = Toast.makeText(this, "bluetooth not supported", 1);
			toast.show();
			finish();
		}
		if (!bluetoothAdapter.isEnabled()) {
			Intent enableBtIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BLUETOOTH);
		}

		Set<BluetoothDevice> pairedDevices = bluetoothAdapter
				.getBondedDevices();
		// If there are paired devices
		boolean pairedDeviceFound = false;
		String macAddress = null;
		if (pairedDevices.size() > 0) {
			// Loop through paired devices
			for (BluetoothDevice device : pairedDevices) {
				if (device.getName().equals(ROVER_BLUETOOTH_NAME)) {
					Log.e(this.getClass().toString(), "Rover Already Paired!");
					pairedDeviceFound = true;
					try {
						macAddress = device.getAddress();
					} catch (Throwable e) {
						Log.e(this.getClass().toString(), e.getMessage());
					}
				}
			}
		}
		if (macAddress == null) {
			Toast.makeText(this, "no paired device found", 2);
		} else {
			startBTCommunicator(macAddress);
		}

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		destroyBTCommunicator();
	}

	@Override
	public void onPause() {
		destroyBTCommunicator();
		super.onStop();
	}

	public void destroyBTCommunicator() {

		if (communicator != null) {
			sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DISCONNECT,
					0, 0);
			communicator = null;
		}

	}

	/**
	 * Sends the motor control values to the communcation thread.
	 * 
	 * @param left
	 *            The power of the left motor from 0 to 100.
	 * @param rigth
	 *            The power of the right motor from 0 to 100.
	 */
	public void updateMotorControl(int left, int right) {

		if (communicator != null) {

			// send messages via the handler
			sendBTCmessage(BTCommunicator.NO_DELAY, cameraMotor, left
					* directionLeft, 0);
			sendBTCmessage(BTCommunicator.NO_DELAY, motorLeft, right
					* directionRight, 0);
		}
	}

	/**
	 * Sends the message via the BTCommuncator to the robot.
	 * 
	 * @param delay
	 *            time to wait before sending the message.
	 * @param message
	 *            the message type (as defined in BTCommucator)
	 * @param value1
	 *            first parameter
	 * @param value2
	 *            second parameter
	 */
	void sendBTCmessage(int delay, int message, int value1, int value2) {
		Bundle myBundle = new Bundle();
		myBundle.putInt("message", message);
		myBundle.putInt("value1", value1);
		myBundle.putInt("value2", value2);
		Message myMessage = btcHandler.obtainMessage();
		myMessage.setData(myBundle);

		if (delay == 0)
			btcHandler.sendMessage(myMessage);

		else
			btcHandler.sendMessageDelayed(myMessage, delay);
	}

	/**
	 * Sends the message via the BTCommuncator to the robot.
	 * 
	 * @param delay
	 *            time to wait before sending the message.
	 * @param message
	 *            the message type (as defined in BTCommucator)
	 * @param String
	 *            a String parameter
	 */
	void sendBTCmessage(int delay, int message, String name) {
		Bundle myBundle = new Bundle();
		myBundle.putInt("message", message);
		myBundle.putString("name", name);
		Message myMessage = btcHandler.obtainMessage();
		myMessage.setData(myBundle);

		if (delay == 0)
			btcHandler.sendMessage(myMessage);
		else
			btcHandler.sendMessageDelayed(myMessage, delay);
	}

	private void startBTCommunicator(String mac_address) {

		if (communicator != null) {
			try {
				communicator.destroyNXTconnection();
			} catch (IOException e) {
			}
		}
		createBTCommunicator();
		communicator.setMACAddress(mac_address);
		communicator.start();

	}

	/**
	 * Creates a new object for communication to the NXT robot via bluetooth and
	 * fetches the corresponding handler.
	 */
	private void createBTCommunicator() {
		// interestingly BT adapter needs to be obtained by the UI thread - so
		// we pass it in in the constructor
		communicator = new BTCommunicator(this, null,
				BluetoothAdapter.getDefaultAdapter(), getResources());
		btcHandler = communicator.getHandler();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// views to drag
		option1 = (TextView) findViewById(R.id.option_1);
		option2 = (TextView) findViewById(R.id.option_2);
		option3 = (TextView) findViewById(R.id.option_3);
		option4 = (TextView) findViewById(R.id.option_4);

		// tag the options
		option1.setTag(ListItemType.Rotate);
		option2.setTag(ListItemType.Move);
		option3.setTag(ListItemType.RotateCamera);
		option4.setTag(ListItemType.TakePhoto);

		dropzone = (ListView) findViewById(R.id.dropzone);
		adapter = new MyArrayAdapter(this);
		dropzone.setAdapter(adapter);

		// set touch listeners
		option1.setOnTouchListener(new ChoiceTouchListener());
		option2.setOnTouchListener(new ChoiceTouchListener());
		option3.setOnTouchListener(new ChoiceTouchListener());
		option4.setOnTouchListener(new ChoiceTouchListener());

		// set drag listeners
		dropzone.setOnDragListener(new ChoiceDragListener());

		inflater = (LayoutInflater) this
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

	}

	/**
	 * ChoiceTouchListener will handle touch events on draggable views
	 * 
	 */
	private final class ChoiceTouchListener implements OnTouchListener {
		public boolean onTouch(View view, MotionEvent motionEvent) {
			if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
				/*
				 * Drag details: we only need default behavior - clip data could
				 * be set to pass data as part of drag - shadow can be tailored
				 */
				ClipData data = ClipData.newPlainText("", "");
				DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(
						view);
				// start dragging the item touched
				view.startDrag(data, shadowBuilder, view, 0);
				return true;
			} else {
				return false;
			}
		}
	}

	public void listButtonClick(View view) {
		Button b = (Button) view;
		b.setText("Click");
	}

	/**
	 * DragListener will handle dragged views being dropped on the drop area -
	 * only the drop action will have processing added to it as we are not -
	 * amending the default behavior for other parts of the drag process
	 * 
	 */
	private class ChoiceDragListener implements OnDragListener {

		@Override
		public boolean onDrag(View v, DragEvent event) {
			switch (event.getAction()) {
			case DragEvent.ACTION_DRAG_STARTED:
				// no action necessary
				break;
			case DragEvent.ACTION_DRAG_ENTERED:
				// no action necessary
				break;
			case DragEvent.ACTION_DRAG_EXITED:
				// no action necessary
				break;
			case DragEvent.ACTION_DROP:

				// get the original text view
				TextView view = (TextView) event.getLocalState();
				int position = dropzone.pointToPosition((int) event.getX(),
						(int) event.getY());
				ListItem item = new ListItem((ListItemType) view.getTag());
				try {
					adapter.insert(item, position);
				} catch (IndexOutOfBoundsException e) {
					adapter.add(item);
				}
				adapter.notifyDataSetChanged();
				break;
			case DragEvent.ACTION_DRAG_ENDED:
				// no action necessary
				break;
			default:
				break;
			}
			return true;
		}
	}

	private void spinCamera(int speed, int delay) {

		if (communicator != null) {

			// send messages via the handler
			sendBTCmessage(delay, cameraMotor, speed * directionLeft, 0);

		}
	}

	private void spinMotorLeft(int speed, int delay) {

		if (communicator != null) {

			// send messages via the handler
			sendBTCmessage(delay, motorLeft, speed * directionRight, 0);

		}
	}

	private void spinMotorRight(int speed, int delay) {
		if (communicator != null) {

			// send messages via the handler
			sendBTCmessage(delay, motorRight, speed * directionLeft, 0);

		}
	}

	private void forward(int distance) {
		int power = 100;
		if (distance < 0) {
			power = -100;
		}
		spinMotorLeft(power, 0);
		spinMotorRight(power, 0);
		spinMotorLeft(0, distance);
		spinMotorRight(0, distance);

	}

	private void rotateCamera(int degrees) {
		if (degrees > 0) {
			spinCamera(100, 0);
			spinCamera(0, (int)(degrees * angleFudgeFactor));
		} else {
			spinCamera(-100, 0);
			spinCamera(0, (int)(degrees * angleFudgeFactor));
		}

	}

	private void rotate(int degrees) {
		if (degrees > 0) {
			spinMotorLeft(100, 0);
			spinMotorRight(-100, 0);
			spinMotorLeft(0, (int)(degrees * angleFudgeFactor));
			spinMotorRight(0, (int)(degrees * angleFudgeFactor));
		} else {
			spinMotorLeft(-100, 0);
			spinMotorRight(100, 0);
			spinMotorLeft(0, (int)(degrees * angleFudgeFactor));
			spinMotorRight(0, (int)(degrees * angleFudgeFactor));
		}
	}

	public void sendButton_Click(View view) {
		// communicator.changeMotorSpeed(1, 50);
		// TODO

		for (int i = 0; i < dropzone.getCount(); i++) {

			ListItem item = (ListItem) dropzone.getItemAtPosition(i);
			String action = item.getType().toString();
			String value = "";
			Log.e(this.getClass().toString(), "Action: " + action + " Value: "
					+ item.getValue());
			if (item.getType() == ListItemType.Move) {
				forward(Integer.parseInt(item.getValue().toString())); // TODO
																		// list
																		// item
																		// value
																		// should
																		// be an
																		// int
			}
			if (item.getType() == ListItemType.Rotate) {
				rotate(Integer.parseInt(item.getValue().toString()));
			}
			if (item.getType() == ListItemType.RotateCamera) {
				rotateCamera(Integer.parseInt(item.getValue().toString()));
			}

			// String msg = "hello";
			// try {
			// communicator.sendMessage(msg.getBytes());
			// } catch (IOException e) {
			// Log.e(this.getClass().toString(), "Unable to send msg to NXT");
			// }
		}

		adapter.clear();
		adapter.notifyDataSetChanged();
		// spinMotorLeft(50); //turns camera
		// spinMotorLeft(50); //spins left track

		//rotate(1000);
		// final ArrayList<ListItem> tempItems = new ArrayList<ListItem>();
		// for(int i = 0; i < adapter.getCount() ; i++)
		// {
		// tempItems.add(adapter.getItem(i));
		// }
		// Thread removeThread = new Thread(new Runnable(){
		//
		//
		// @Override
		// public void run() {
		// for(ListItem item : tempItems)
		// {
		// adapter.remove(item);
		// adapter.notifyDataSetChanged();
		// try {
		// Thread.sleep(500);
		// } catch (InterruptedException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// }
		//
		// }
		//
		// });
		// removeThread.start();

	}

	@Override
	public boolean isPairing() {
		// TODO Auto-generated method stub
		return false;
	}

}
