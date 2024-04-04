package application;


import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import javafx.scene.control.TextArea;

// Implementação da interface servidor
public class Server extends UnicastRemoteObject implements ServerInterface {
	
	public static final int TILE_SIZE = 64;
	public static final int WIDTH = 17;
    public static final int HEIGHT = 11;
	
	public Player player1;
	public Player player2;
    public MoveResultData moveResultData;
    public TextArea chat = new TextArea();
    public GameResult gameResult = GameResult.STILLAVALIABLEMOVES;
    
    // Ações que um jogador pode fazer
    public Action player1Registered;
    public Action player2Registered;
    public Action moveMade;
    public Action messageSent;
    public Action gameReseted;
    public Action gameEnded;

	protected Server() throws RemoteException {
		super();
	}
	
	// Checar se algum jogador realizou alguma ação
	@Override
	public TypesOfChanges checkIfSomethingChanged(Player player) throws RemoteException {
		
		// Jogador 1 foi registrado e é o jogador 2 realizando esta função?
		if(player1Registered != null && player1 != null && player1Registered.wasMade && player.getNumber() == 2) {
			return TypesOfChanges.PLAYERREGISTERED;
		}
		
		// Jogador 2 foi registrado e é o jogador 1 realizando esta função?
		if(player2Registered != null && player2 != null && player2Registered.wasMade && player.getNumber() == 1) {
			return TypesOfChanges.PLAYERREGISTERED;
		}
		
		// Um movimento foi feito?
		if(moveMade != null && moveMade.wasMade && !moveMade.player.getName().equals(player.getName())) {
			return TypesOfChanges.PLAYERMOVED;
		}
		
		// Uma mensagem foi enviada?
		if(messageSent != null && messageSent.wasMade && !messageSent.player.getName().equals(player.getName())) {
			return TypesOfChanges.MESSAGESENT;
		}
		
		// O jogo foi resetado?
		if(gameReseted != null && gameReseted.wasMade && !gameReseted.player.getName().equals(player.getName())) {
			return TypesOfChanges.GAMERESETED;
		}
		
		// O jogo finalizou?
		if(gameEnded != null && gameEnded.wasMade && !gameEnded.player.getName().equals(player.getName())) {
			return TypesOfChanges.GAMEENDED;
		}
		
		// Não houveram mudanças
		return TypesOfChanges.NOCHANGES;
	}

	// Registrar um jogador
	@Override
	public Player registerPlayer(Player player) throws RemoteException {
		
		// O primeiro jogador a se registrar sempre será o jogador 1
		if(player1 == null) {
			this.player1 = new Player(player.getName(), true, 1);
			this.player1Registered = new Action(true,player);
			return player1;
		}
		else {
			this.player2 = new Player(player.getName(), false, 2);
			this.player2Registered = new Action(true,player);
			return player2;
		}
		
	}

	// Conseguir os dados do oponente
	@Override
	public Player getOponnent(Player player) throws RemoteException {
		
		if(player.getNumber() == 1) {
			this.player2Registered.wasMade = false;
			return this.player2;
		}
		else {
			this.player1Registered.wasMade = false;
			return this.player1;
		}
	}

	// Avisar ao servidor que o jogador escreveu uma mensagem no chat
	@Override
	public void writeMessage(Player player, String message) throws RemoteException {
		this.chat.appendText(message);
		this.messageSent = new Action(true, player);
	}

	// Atualizar o chat com a mensagem do outro jogador
	@Override
	public String updateChat() throws RemoteException {
		this.messageSent.wasMade = false;
		return this.chat.getText();
	}
	

	// Avisar ao servidor que o jogador realizou seu movimento
	@Override
	public void movePiece(MoveResultData moveResultData, Player player) throws RemoteException {
		this.moveResultData = moveResultData;
		this.moveMade = new Action(true, player);
	}

	// Atualizar o tabuleiro com a jogada do oponente
	@Override
	public MoveResultData updateBoard() throws RemoteException {	
		this.moveMade.wasMade = false;		
		return moveResultData;
	}  
	
	// Avisar ao servidor que o jogador resetou seu tabuleiro
	@Override
    public void confirmReset(Player player) throws RemoteException{
		this.gameReseted = new Action(true, player);
	}
	
	// Avisar ao servidor que o tabuleiro do outro jogador foi resetado
	@Override
	public void resetGame() throws RemoteException{
		this.gameReseted.wasMade = false;
	}

	// Verificar o estado atual do jogo e se ele já está finalizado
	@Override
	public GameResult checkIfgameEnded(Tile[][] mainBoard) throws RemoteException {
		
		int avaliableMoves = 0;
    	int avaliablePieces = 0;
    	
		// Primeiras duas linhas do tabuleiro
		for(int y = 2; y < 4; y++) {
			for(int x = 10; x < 13; x++ ) {
				if(mainBoard[x][y] != null && mainBoard[x][y].hasPiece()) {
					avaliablePieces += 1;
					if(checkIfThePieceCanMove(x,y, mainBoard)) {
						avaliableMoves += 1;
					}
				}
			}
		}
		
		
		// Linhas 3 a 5 do tabuleiro
		for(int y = 4; y < 7; y++) {
			for(int x = 8; x < 15; x++ ) {
				if(mainBoard[x][y] != null && mainBoard[x][y].hasPiece()) {
					avaliablePieces += 1;
					if(checkIfThePieceCanMove(x,y, mainBoard)) {
						avaliableMoves += 1;
					}
				}
				
			}
		
		}
		
		// Duas últimas linhas do tabuleiro
		for(int y = 7; y < 9; y++) {
			for(int x = 10; x < 13; x++ ) {
				if(mainBoard[x][y] != null && mainBoard[x][y].hasPiece()) {
					avaliablePieces += 1;
					if(checkIfThePieceCanMove(x,y, mainBoard)) {
						avaliableMoves += 1;
					}
				}
			}
		}
    	
    	if(avaliableMoves > 0) {
    		return GameResult.STILLAVALIABLEMOVES;
    	}
    	
    	if(avaliableMoves == 0 && avaliablePieces > 1) {
    		return GameResult.NOMOVESLEFT;
    	}
    	
    	if(avaliablePieces == 1) {
    		return GameResult.NOPIECESLEFT;
    	}
    	
    	return GameResult.STILLAVALIABLEMOVES;
	}
	
	// Avisar ao servidor que o jogo finalizou
	@Override
	public void endGame(GameResult gameResult, Player player) throws RemoteException {
		this.gameResult = gameResult;
		this.gameEnded = new Action(true, player);
		if(player.getNumber() == 1) {
			this.player1.setWinner(player.getWinner());
			this.player2.setWinner(!player.getWinner());
		}
		else {
			this.player1.setWinner(!player.getWinner());
			this.player2.setWinner(player.getWinner());
		}
	}

	// Atualizar o jogo do outro jogador com a finalização adequada
	@Override
	public GameResult updateGameResult(Player player) throws RemoteException {
		this.gameEnded.wasMade = false;
		if(player.getNumber() == 1) {
			player.setWinner(!this.player2.getWinner());
		}
		else {
			player.setWinner(!this.player1.getWinner());
		}
		return this.gameResult;
	}
	
	@Override
	public Player getWinner() throws RemoteException {
		return this.player1.getWinner() ? this.player1 : this.player2;
	}
	
	@Override
	public Player getLoser() throws RemoteException {
		return this.player1.getWinner() ? this.player2 : this.player1;
	}
	
	
	// Checar condições para a peça se mover
    private boolean checkIfThePieceCanMove(int x, int y, Tile[][] mainBoard) {
    	   	
    	// Para a direita
    	if(mainBoard[x+1][y] != null && mainBoard[x+2][y] != null && mainBoard[x+1][y].hasPiece() && !(mainBoard[x+2][y].hasPiece()) && mainBoard[x+2][y].getIsPartOfBoard()) {
    		return true;
    	}
    	
    	// Para a esquerda
    	if(mainBoard[x-1][y] != null && mainBoard[x-2][y] != null && mainBoard[x-1][y].hasPiece() && !(mainBoard[x-2][y].hasPiece()) && mainBoard[x-2][y].getIsPartOfBoard()) {
    		return true;
    	}
    	
    	// Para baixo
    	if(mainBoard[x][y+1] != null && mainBoard[x][y+2] != null && mainBoard[x][y+1].hasPiece() && !(mainBoard[x][y+2].hasPiece()) && mainBoard[x][y+2].getIsPartOfBoard()) {
    		return true;
    	}
    	
    	// Para cima
    	if(mainBoard[x][y-1] != null && mainBoard[x][y-2] != null && mainBoard[x][y-1].hasPiece() && !(mainBoard[x][y-2].hasPiece()) && mainBoard[x][y-2].getIsPartOfBoard()) {
    		return true;
    	}
    	
    	// Não pode mover
    	return false;
    }	


}
