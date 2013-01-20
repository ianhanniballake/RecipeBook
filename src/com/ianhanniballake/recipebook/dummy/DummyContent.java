package com.ianhanniballake.recipebook.dummy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class DummyContent
{
	/**
	 * A dummy item representing a piece of content.
	 */
	public static class DummyItem
	{
		/**
		 * Dummy content
		 */
		public String content;
		/**
		 * ID of this item
		 */
		public long id;

		/**
		 * Creates a new DummyItem
		 * 
		 * @param id
		 *            Unique ID of the item
		 * @param content
		 *            Dummy content
		 */
		public DummyItem(final long id, final String content)
		{
			this.id = id;
			this.content = content;
		}

		@Override
		public String toString()
		{
			return content;
		}
	}

	/**
	 * A map of sample (dummy) items, by ID.
	 */
	public static Map<Long, DummyItem> ITEM_MAP = new HashMap<Long, DummyItem>();
	/**
	 * An array of sample (dummy) items.
	 */
	public static List<DummyItem> ITEMS = new ArrayList<DummyItem>();
	static
	{
		// Add 3 sample items.
		addItem(new DummyItem(1L, "Item 1"));
		addItem(new DummyItem(2L, "Item 2"));
		addItem(new DummyItem(3L, "Item 3"));
	}

	private static void addItem(final DummyItem item)
	{
		ITEMS.add(item);
		ITEM_MAP.put(item.id, item);
	}
}
