import java.util.LinkedList;

public class Configurations {
    private char[][] board; // 2D array representing the game board
    private int size; // Board size 
    private int lengthToWin; // Number of symbols in a row needed to win
    private int max_levels; // Maximum search depth for AI (if applicable)
    
    // Constructor to initialize the board and its settings
    public Configurations(int board_size, int lengthToWin, int max_levels) {
        this.size = board_size;
        this.lengthToWin = lengthToWin;
        this.max_levels = max_levels;
        
        // Fill board with empty spaces to start
        board = new char[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                board[i][j] = ' '; // each cell initialized to ' ' (empty)
            }
        }
    }
    
    // Creates and returns a new hash table for storing board configurations
    public HashDictionary createDictionary() {
        return new HashDictionary(6121); // Random prime number for hash table size
    }
    
    // Checks if the current configuration exists in the dictionary
    public int repeatedConfiguration(HashDictionary hashTable) {
        String s = "";
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                s += board[i][j]; // convert board to a string for hashing
            }
        }
        
        int score = hashTable.get(s); // Retrieve the score from the hash table
        return score;
    }
    
    // Adds a new configuration to the dictionary
    public void addConfiguration(HashDictionary hashDictionary, int score) {
        String s = "";
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                s += board[i][j]; // convert board to string to store in dictionary
            }
        }
        Data entry = new Data(s, score); // Create a Data object with given configuration and score
        
        try {
            hashDictionary.put(entry); // Attempt to add to hash table
        } catch (DictionaryException e) {
            System.out.println("DictionaryException: " + e.getMessage());
            // Catches failed attempt
        }
    }
    
    // Places either an X or an O on the board at the specified row and column
    public void savePlay(int row, int col, char symbol) {
        board[row][col] = symbol;
    }
    
    // Checks if a specific square is empty
    public boolean squareIsEmpty(int row, int col) {
        return board[row][col] == ' ';
    }
    
    // Checks if the given symbol has won by forming a row, column, or diagonal of required length
    public boolean wins(char symbol) {
        // Check horizontal rows for a winning sequence
        for (int row = 0; row < size; row++) {
            int count = 0;
            for (int col = 0; col < size; col++) {
                if (board[row][col] == symbol) {
                    count++;
                } else {
                    count = 0;
                    
                }
                
                if (count >= lengthToWin) return true;
            }
        }

        // Check vertical columns for a winning sequence
        for (int col = 0; col < size; col++) {
            int count = 0;
            for (int row = 0; row < size; row++) {
                if (board[row][col] == symbol) {
                    count++;
                } else {
                    count = 0;
                }
                if (count >=lengthToWin) return true;
            }
        }

        // Check diagonals (top-left to bottom-right) for a winning sequence
        for (int row = 0; row <= size - lengthToWin; row++) {
            for (int col = 0; col <= size - lengthToWin; col++) {
                int count = 0;
                for (int i = 0; i < lengthToWin; i++) {
                    if (board[row + i][col + i] == symbol) {
                        count++;
                        if (count >= lengthToWin) return true;
                        
                    } else {
                        break;
                        
                    }
                }
            }
        }

        // Check diagonals (top-right to bottom-left) for a winning sequence
        for (int row = 0; row <= size - lengthToWin; row++) {
            for (int col = lengthToWin - 1; col < size; col++) {
                int count = 0;
                for (int i = 0; i < lengthToWin; i++) {
                    if (board[row + i][col - i] == symbol) {
                        count++;
                        if (count >= lengthToWin) {
                        	return true;
                        	}
                    } else {
                        break;
                    }
                }
            }
        }

        // no winning sequence found
        return false;
    }
    
    // Checks if the game is a draw (no empty cells and no winner)
    public boolean isDraw() {
        // Check for any empty cells
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (board[i][j] == ' ') {
                    return false; // Game is not a draw if any empty cells remain
                }
            }
        }
        
        // Check if either player has won
        if (this.wins('X') || this.wins('O')) {
            return false; // Game is not a draw if someone has won
        }
        return true; // No empty cells and no winner means the game is a draw
    }
    
    // Evaluates the board state and returns a code indicating the result
    public int evalBoard() {
        if (this.wins('O')) {
            return 3; // Return 3 if the computer ('O') has won
        } else if (this.wins('X')) {
            return 0; // Return 0 if the human player ('X') has won
        } else if (this.isDraw()) {
            return 2; // Return 2 if the game is a draw
        } else {
            return 1; // Return 1 if the game is still ongoing
        }
        
    }
}
