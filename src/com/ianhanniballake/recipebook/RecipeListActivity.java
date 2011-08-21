package com.ianhanniballake.recipebook;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

/**
 * Activity controlling the recipe list
 */
public class RecipeListActivity extends FragmentActivity
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