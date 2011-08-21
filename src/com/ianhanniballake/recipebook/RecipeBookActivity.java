package com.ianhanniballake.recipebook;

import android.app.Activity;
import android.os.Bundle;

/**
 * Activity controlling the recipe list
 */
public class RecipeBookActivity extends Activity
{
	/**
	 * Sets the main layout
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
	}
}