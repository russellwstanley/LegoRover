/*INSERT YOUR PACKAGE NAME*/
package com.singapore.legorover;

import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.os.Bundle;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.TextView;

/**
 * Android SDK Tutorial - Drag and Drop Mobiletuts+
 * 
 * Sue Smith January 2013
 */

public class MainActivity extends Activity {
	
	private enum ListItemType {
		Move, Rotate, RotateCamera, TakePhoto
	}

	public class MyArrayAdapter extends ArrayAdapter<ListItemType> {

		public MyArrayAdapter(Context context) {
			super(context, android.R.layout.simple_list_item_1);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LinearLayout layout;
			ListItemType type = getItem(position);
			if(type==ListItemType.TakePhoto)
			{
				 layout =  (LinearLayout)inflater.inflate(R.layout.plain_row, parent,
						false);
			}
			else
			{
			 layout =  (LinearLayout)inflater.inflate(R.layout.number_row, parent,
					false);
			}
			//TODO this is s**t look into subclassing layout
			TextView text = (TextView)layout.findViewById(R.id.list_option_text);
			text.setText(type.toString());
			layout.setTag(type);
			return layout;
		}
	}

	// text views being dragged and dropped onto
	private TextView option1, option2, option3, option4;
	private ListView dropzone;
	MyArrayAdapter adapter;
	private LayoutInflater inflater;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// views to drag
		option1 = (TextView) findViewById(R.id.option_1);
		option2 = (TextView) findViewById(R.id.option_2);
		option3 = (TextView) findViewById(R.id.option_3);
		option4 = (TextView) findViewById(R.id.option_4);
		
		//tag the options
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
	
	public void listButtonClick(View view)
	{
		Button b = (Button)view;
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
				ListItemType type = (ListItemType)view.getTag();
				try {
					adapter.insert(type, position);
				} catch (IndexOutOfBoundsException e) {
					adapter.add(type);
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

	
}
