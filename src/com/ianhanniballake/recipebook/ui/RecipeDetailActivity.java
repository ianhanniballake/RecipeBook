package com.ianhanniballake.recipebook.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.ianhanniballake.recipebook.R;

/**
 * Activity which displays only the Recipe details
 */
public class RecipeDetailActivity extends FragmentActivity
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
		setContentView(R.layout.activity_recipe_detail);
	}
}
