package com.ianhanniballake.recipebook;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.ianhanniballake.recipebook.dummy.DummyContent;

/**
 * A list fragment representing a list of Recipes. This fragment also supports tablet devices by allowing list items to
 * be given an 'activated' state upon selection. This helps indicate which item is currently being viewed in a
 * {@link RecipeDetailFragment}.
 * <p>
 * Activities containing this fragment MUST implement the {@link Callbacks} interface.
 */
public class RecipeListFragment extends ListFragment
{
	/**
	 * A callback interface that all activities containing this fragment must implement. This mechanism allows
	 * activities to be notified of item selections.
	 */
	public interface Callbacks
	{
		/**
		 * Callback for when an item has been selected.
		 */
		public void onItemSelected(String id);
	}

	/**
	 * A dummy implementation of the {@link Callbacks} interface that does nothing. Used only when this fragment is not
	 * attached to an activity.
	 */
	private static Callbacks sDummyCallbacks = new Callbacks()
	{
		@Override
		public void onItemSelected(final String id)
		{
		}
	};
	/**
	 * The serialization (saved instance state) Bundle key representing the activated item position. Only used on
	 * tablets.
	 */
	private static final String STATE_ACTIVATED_POSITION = "activated_position";
	/**
	 * The current activated item position. Only used on tablets.
	 */
	private int mActivatedPosition = AdapterView.INVALID_POSITION;
	/**
	 * The fragment's current callback object, which is notified of list item clicks.
	 */
	private Callbacks mCallbacks = sDummyCallbacks;

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the fragment (e.g. upon screen orientation
	 * changes).
	 */
	public RecipeListFragment()
	{
	}

	@Override
	public void onAttach(final Activity activity)
	{
		super.onAttach(activity);
		// Activities containing this fragment must implement its callbacks.
		if (!(activity instanceof Callbacks))
			throw new IllegalStateException("Activity must implement fragment's callbacks.");
		mCallbacks = (Callbacks) activity;
	}

	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		// TODO: replace with a real list adapter.
		setListAdapter(new ArrayAdapter<DummyContent.DummyItem>(getActivity(),
				android.R.layout.simple_list_item_activated_1, android.R.id.text1, DummyContent.ITEMS));
	}

	@Override
	public void onDetach()
	{
		super.onDetach();
		// Reset the active callbacks interface to the dummy implementation.
		mCallbacks = sDummyCallbacks;
	}

	@Override
	public void onListItemClick(final ListView listView, final View view, final int position, final long id)
	{
		super.onListItemClick(listView, view, position, id);
		// Notify the active callbacks interface (the activity, if the
		// fragment is attached to one) that an item has been selected.
		mCallbacks.onItemSelected(DummyContent.ITEMS.get(position).id);
	}

	@Override
	public void onSaveInstanceState(final Bundle outState)
	{
		super.onSaveInstanceState(outState);
		if (mActivatedPosition != AdapterView.INVALID_POSITION)
			// Serialize and persist the activated item position.
			outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
	}

	@Override
	public void onViewCreated(final View view, final Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);
		// Restore the previously serialized activated item position.
		if (savedInstanceState != null && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION))
			setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
	}

	private void setActivatedPosition(final int position)
	{
		if (position == AdapterView.INVALID_POSITION)
			getListView().setItemChecked(mActivatedPosition, false);
		else
			getListView().setItemChecked(position, true);
		mActivatedPosition = position;
	}

	/**
	 * Turns on activate-on-click mode. When this mode is on, list items will be given the 'activated' state when
	 * touched.
	 */
	public void setActivateOnItemClick(final boolean activateOnItemClick)
	{
		// When setting CHOICE_MODE_SINGLE, ListView will automatically
		// give items the 'activated' state when touched.
		getListView()
				.setChoiceMode(activateOnItemClick ? AbsListView.CHOICE_MODE_SINGLE : AbsListView.CHOICE_MODE_NONE);
	}
}
