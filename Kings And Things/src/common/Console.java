package common;

import java.util.Date;

import java.awt.Color;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.swing.JTextPane;
import javax.swing.text.Document;
import javax.swing.text.AttributeSet;
import javax.swing.text.StyleContext;
import javax.swing.text.StyleConstants;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.BadLocationException;

import static common.Constants.Level;

/**
 * a basic GUI object to hold text information,
 * it is able to add text with different Levels for color print
 */
@SuppressWarnings("serial")
public class Console extends JTextPane {
	
	private static DateFormat dateFormat = new SimpleDateFormat("[HH:mm:ss]>> ");
	private StyleContext styleContext;
	private AttributeSet attribute;
	
	public Console(){
		super();
	}
	
	/**
	 * append a text with specific Level
	 * @param message - string to be added
	 * @param level - effect the color of the text, must be Error, Warning, Notice or Plain from Level
	 */
	public void add( String message, Level level){
		if( message!=null){
			switch( level){
				case Warning:
					add( Constants.COLOR_WARNNING, message);break;
				case Error:
					add( Constants.COLOR_ERROR, message);break;
				case Notice:
					add( Constants.COLOR_NOTICE, message);break;
				case Plain:
				default:
					add( Constants.COLOR_PLAIN, message);
			}
		}
	}
	
	/**
	 * for internal use, append a string with color
	 * @param color - color to be used for printing of the text
	 * @param message - text to be appended
	 */
	private synchronized void add( Color color, String message){
		styleContext = StyleContext.getDefaultStyleContext();
		attribute = styleContext.addAttribute( SimpleAttributeSet.EMPTY, StyleConstants.Foreground, color);
		Document doc = getDocument();
		try {
			doc.insertString( doc.getLength(), dateFormat.format( new Date()) + message+"\r\n", attribute);
			setCaretPosition( doc.getLength());
		} catch (BadLocationException e) {}
	}
}
