package com.japanzai.koroshiya.interfaces;

/**
 * Interface containing methods a class would need to be applicable for a ConfirmDialog
 * */
public interface ModalReturn {
	
	/**
	 * Represents confirmation on a ConfirmDialog
	 * */
	public abstract void accept();
	
	/**
	 * Called when the user declines a ConfirmDialog
	 * */
	public abstract void decline();
	
}
