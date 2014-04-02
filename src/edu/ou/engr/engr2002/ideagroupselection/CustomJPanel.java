package edu.ou.engr.engr2002.ideagroupselection;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LayoutManager;

import javax.swing.JPanel;

/**
 * This highly creatively named class is a panel with a GridBagLayout that 
 * lays components out in either one row or one column. It might not work
 * in resizable windows, and it's probably incomplete or incorrect in
 * other ways too.
 */
@SuppressWarnings("serial")
public class CustomJPanel extends JPanel {

	public static enum Orientation { HORIZONTAL, VERTICAL };
	public static final Orientation HORIZONTAL = Orientation.HORIZONTAL;
	public static final Orientation VERTICAL = Orientation.VERTICAL;
	
	private GridBagLayout layout;
	private Orientation orientation;
	private int nextIndex;
	private int defaultSpace;
	
	/**
	 * Creates a panel with a vertical layout and default component spacing
	 * of 0.
	 */
	public CustomJPanel() {
		this(VERTICAL, 0);
	}
	
	/**
	 * Creates a panel with the specified orientation and default component 
	 * spacing of 0.
	 * @param orientation	HORIZONTAL or VERTICAL
	 */
	public CustomJPanel(Orientation o) {
		this(o, 0);
	}
	
	/**
	 * Creates a panel with the specified orientation and default component
	 * spacing.
	 * @param orientation	HORIZONTAL or VERTICAL
	 * @param defaultSpace	Default component spacing
	 */
	public CustomJPanel(Orientation o, int defaultSpace) {
		if (defaultSpace < 0)
			throw new IllegalArgumentException("Cannot have negative " +
					"component spacing.");
		
		this.orientation = o;
		nextIndex = 0;
		this.defaultSpace = defaultSpace;
		
		layout = new GridBagLayout();
		// this class's setLayout() has been overridden to do nothing
		super.setLayout(layout);
	}
	
	/**
	 * Get the orientation used to lay out components in the panel
	 * @return The panel's orientation
	 */
	public Orientation getOrientation() {
		return orientation;
	}
	
	/**
	 * Get the default component spacing.
	 * @return The default spacing
	 */
	public int getDefaultSpacing() {
		return defaultSpace;
	}
	
	/**
	 * Set the default component spacing. This will only affect components
	 * added to the container after the change is made.
	 * @param defaultSpace The new default spacing
	 */
	public void setDefaultSpacing(int defaultSpace) {
		if (defaultSpace < 0)
			throw new IllegalArgumentException("Cannot have negative " +
					"component spacing.");
		this.defaultSpace = defaultSpace;
	}
	
	/**
	 * Set the anchor of the specified component, if something other than the
	 * default of NORTHWEST is desired.
	 * @param comp	 The component to change the anchor of
	 * @param anchor One of the anchor constants defined in GridBagConstraints
	 * @see GridBagConstraints
	 */
	public void setComponentAnchor(Component comp, int anchor) {
		GridBagConstraints c = layout.getConstraints(comp);
		c.anchor = anchor;
		layout.setConstraints(comp, c);
	}
	
	/**
	 * Set the fill of the specified component, if something other than the
	 * default (HORIZONTAL for vertical layouts and NONE for horizontal 
	 * layouts) is desired.
	 * @param comp	The component to change the fill of
	 * @param fill	One of the fill constants defined in GridBagConstraints
	 * @see GridBagConstraints
	 */
	public void setComponentFill(Component comp, int fill) {
		GridBagConstraints c = layout.getConstraints(comp);
		c.fill = fill;
		layout.setConstraints(comp, c);
	}
	
	/**
	 * Get the constraints of a component (probably to customize them).
	 * @param comp The component to get the constraints of
	 * @return A copy of the component's constraints
	 */
	public GridBagConstraints getConstraints(Component comp) {
		return layout.getConstraints(comp);
	}

	/**
	 * Set the constraints of the specified component.
	 * @param comp The component to set the constraints of
	 * @param c The new constraints
	 */
	public void setConstraints(Component comp, GridBagConstraints c) {
		layout.setConstraints(comp, c);
	}

	/**
	 * Add the specified component with the default spacing at the end of the
	 * panel.
	 * @param comp The component to add
	 * @return The component argument
	 */
    @Override
	public Component add(Component comp) {
		return add(defaultSpace, comp, nextIndex++);
	}
	
	/**
	 * Add the specified component with the specified amount of space after it.
	 * Due to the fact that space and index are both ints, and the fact that
	 * add(Component, int) generally means add at index, I had to use 
	 * add(int, Component) to allow the user to specify only space.
	 * @param space		The amount of space to put after the component
	 * @param comp		The component to add
	 * @return The component argument
	 */
	public Component add(int space, Component comp) {
		return add(space, comp, nextIndex++);
	}
	
	/**
	 * Add the specified component at the specified index with default spacing.
	 * @param comp The component to add
	 * @param index The index to add comp at
	 * @return The component argument
	 */
    @Override
	public Component add(Component comp, int index) {
		return add(defaultSpace, comp, index);
	}
	
	/**
	 * Add the specified component with the specified spacing at the specified
	 * index. To be consistent with add(int space, Component comp), space comes
	 * before the other parameters even though it would traditionally be after.
	 * @param space	The amount of space to put after the component
	 * @param comp	The component to add
	 * @param index	The index to add comp at
	 * @return The component argument
	 */
	public Component add(int space, Component comp, int index) {
		GridBagConstraints c = new GridBagConstraints();
		
		c.anchor = GridBagConstraints.NORTHWEST;
		
		if (orientation == HORIZONTAL) {
			c.gridx = index;
			c.gridy = 0;
			c.insets = new Insets(0, 0, 0, space);
			c.fill = GridBagConstraints.NONE;
		} else {
			c.gridx = 0;
			c.gridy = index;
			c.insets = new Insets(0, 0, space, 0);
			c.fill = GridBagConstraints.HORIZONTAL;
		}
		
		super.add(comp, c);
		return comp;
	}
	
	/**
	 * Remove the specified component from the panel.
	 * @param comp The component to remove
	 */
    @Override
	public void remove(Component comp) {
		// If we're removing the last component added to the panel,
		// decrement the index at which to add the next component.
		if (this.isAncestorOf(comp)) {
			GridBagConstraints c = layout.getConstraints(comp);
			
			if ((orientation == HORIZONTAL && c.gridx == nextIndex - 1)
					|| orientation == VERTICAL && c.gridy == nextIndex - 1)
				--nextIndex;
		}
		
		super.remove(comp);
	}
	
    /**
     * Remove the component at the specified index from the panel.
     * @param index The index to remove the component at
     */
    @Override
	public void remove(int index) {
		// If we're removing the last component added to the panel,  
		// decrement the index at which to add the next component.
		if (index == nextIndex - 1)
			--nextIndex;
		
		super.remove(index);
	}
	
    /**
     * Remove all components from the panel.
     */
    @Override
	public void removeAll() {
		nextIndex = 0;
		super.removeAll();
	}

	/**
	 * This specialized panel ignores commands to set the layout manager.
	 * @param mgr does nothing
	 */
    @Override
	public void setLayout(LayoutManager mgr) {
		// do nothing
	}
}
