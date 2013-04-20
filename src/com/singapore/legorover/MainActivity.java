/*INSERT YOUR PACKAGE NAME*/
package com.singapore.legorover;

import android.app.Activity;
import android.content.ClipData;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.DragShadowBuilder;
import android.view.View.OnDragListener;
import android.view.View.OnTouchListener;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Android SDK Tutorial - Drag and Drop
 * Mobiletuts+
 * 
 * Sue Smith January 2013
 */

public class MainActivity extends Activity {

	//text views being dragged and dropped onto
	private TextView option1, option2, option3;
	private LinearLayout dropzone;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		//get both sets of text views

		//views to drag
		option1 = (TextView)findViewById(R.id.option_1);
		option2 = (TextView)findViewById(R.id.option_2);
		option3 = (TextView)findViewById(R.id.option_3);

		dropzone = (LinearLayout)findViewById(R.id.dropzone);

		//set touch listeners
		option1.setOnTouchListener(new ChoiceTouchListener());
		option2.setOnTouchListener(new ChoiceTouchListener());
		option3.setOnTouchListener(new ChoiceTouchListener());

		//set drag listeners
		dropzone.setOnDragListener(new ChoiceDragListener());
	}

	/**
	 * ChoiceTouchListener will handle touch events on draggable views
	 *
	 */
	private final class ChoiceTouchListener implements OnTouchListener {
		public boolean onTouch(View view, MotionEvent motionEvent) {
			if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
				/*
				 * Drag details: we only need default behavior
				 * - clip data could be set to pass data as part of drag
				 * - shadow can be tailored
				 */
				ClipData data = ClipData.newPlainText("", "");
				DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
				//start dragging the item touched
				view.startDrag(data, shadowBuilder, view, 0);
				return true;
			} else {
				return false;
			}
		}
	} 

	/**
	 * DragListener will handle dragged views being dropped on the drop area
	 * - only the drop action will have processing added to it as we are not
	 * - amending the default behavior for other parts of the drag process
	 *
	 */
	private class ChoiceDragListener implements OnDragListener {

		@Override
		public boolean onDrag(View v, DragEvent event) {
			switch (event.getAction()) {
			case DragEvent.ACTION_DRAG_STARTED:
				//no action necessary
				break;
			case DragEvent.ACTION_DRAG_ENTERED:
				//no action necessary
				break;
			case DragEvent.ACTION_DRAG_EXITED:        
				//no action necessary
				break;
			case DragEvent.ACTION_DROP:
				//handle the dragged view being dropped over a drop view
				TextView view = (TextView) event.getLocalState();
				//view dragged item is being dropped on
				LinearLayout dropTarget = (LinearLayout) v;
				TextView dropped = new TextView(MainActivity.this);
				dropped.setText(view.getText());
				
				//update the text in the target view to reflect the data being dropped
				dropTarget.addView(dropped);
				break;
			case DragEvent.ACTION_DRAG_ENDED:
				//no action necessary
				break;
			default:
				break;
			}
			return true;
		}
	} 
}
