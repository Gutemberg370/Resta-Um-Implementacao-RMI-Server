package application;


import java.io.Serializable;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

// Classe que representa os ladrilhos que compõe o tabuleiro
public class Tile extends Rectangle implements Serializable{

	private Piece piece;
	private boolean isPartOfBoard;
	
	public boolean hasPiece() {
		return piece != null;
	}
	
	public Piece getPiece() {
		return piece;
	}
	
	public void setPiece(Piece piece) {
		this.piece = piece;
	}
	
	public boolean getIsPartOfBoard() {
		return isPartOfBoard;
	}
	
	public Tile(boolean isPartOfBoard, int x, int y) {
		this.isPartOfBoard = isPartOfBoard;
		
		setWidth(Server.TILE_SIZE);
		setHeight(Server.TILE_SIZE);
		relocate(x * Server.TILE_SIZE,y * Server.TILE_SIZE);
		
		setFill(isPartOfBoard ? Color.valueOf("#cd3b0e"): javafx.scene.paint.Color.CYAN);
	}
}
