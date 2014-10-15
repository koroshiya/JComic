package com.japanzai.koroshiya.interfaces;

/**
 * Interface which any Setting with multiple states should inherit from.
 * eg. A checkbox can have two states and thus a CheckSetting should inherit
 * from this interface.
 * */
public interface StateBasedSetting {
	
	/**
	 * @return Index of the current state
	 * */
	public abstract int getState();
	
	/**
	 * @param state Index to set for the current state
	 * */
	public abstract void setState(int state);
	
}
