package processing.app;

import java.awt.Color;
import java.io.File;

import javax.swing.text.BadLocationException;

public class TracingHandler {
	
	private Editor editor;
	
	public TracingHandler(Editor editor) {
		this.editor = editor;
	}
	
	public void selectLine(String filePath, int line){
		//if file is open, activate him
		File file = new File(filePath);
		if(!file.exists()){
			System.out.println("Debug error: wrong file to open - " + filePath + "\n");
			//editor.console.insertString("Debug error: wrong file to open - " + filePath + "\n", null);
			//editor.console.appendText("Debug error: wrong file to open - " + filePath + "\n", true);
			return;
		}
		
		if(isFileOpen(file.getName()))
			editor.sketch.setCurrentCode(file.getName());
		else
			editor.sketch.addFile(file);
		//select line
		try {
			editor.getTextArea().addLineHighlight(line-1, Color.YELLOW);
			editor.getTextArea().setCaretPosition(editor.getTextArea().getLineStartOffset(line-1));
		} catch (BadLocationException e) {
			System.out.println("Debug error: wrong line number - " + line + "\n");
			//editor.console.insertString("Debug error: wrong line number - " + line + "\n", null);
			//editor.console.appendText("Debug error: wrong line number - " + line + "\n", true);
		}
	}
	
	public void deselectAllLines(){
		editor.getTextArea().removeAllLineHighlights();
	}
	
	private boolean isFileOpen(String name){
		for(SketchCode code : editor.sketch.getCodes()){
			if(code.getFileName().equals(name))
				return true;
		}
		return false;
	}
	
}
