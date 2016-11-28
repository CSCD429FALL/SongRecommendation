package songRecommendation;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JComboBox;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import decisionTree.DecisionTree;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JTextPane;
import java.awt.Color;

/**
 * Author: Sergio Ramirez
 * 
 */
public class GUI implements ActionListener{

	private static int TRACK_ID = 0, TITLE = 1, RELEASE = 2, ARTIST_ID = 3, ARTIST_NAME = 4, DURATION = 5,
			ARTIST_FAMILIARITY = 6, ARTIST_HOTNESS = 7, YEAR = 8, GENRE = 9, NUMBER_OF_ATTRIBUTES = 10;
	
	private String[] origin = {"","","Music While You Work","AR00A1N1187FB484EB", "", "", "", "", "", "", "", ""};

	private DecisionTree decisionTree;

	private JComboBox<Object> comboBox;
	private JTextArea txtCurrentTrack;
	private JFrame frame;
	private JTextField txtSearch;
	private static int MAXRESULTS = 20;
	private Connection connection;
	private Statement statement;
	private ArrayList<String> retrieveArray;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GUI window = new GUI();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 * 
	 * @throws IOException
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 */
	public GUI() throws IOException, ClassNotFoundException, SQLException {
		initialize();
		
		comboBox.addActionListener(this);
		txtSearch.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent ke) {
				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {
						comboFilter(txtSearch.getText());

					}

				});
			}
		});
		
		
		

	}

	/**
	 * Initialize the contents of the frame.
	 * 
	 * @throws IOException
	 * @throws ClassNotFoundException 
	 * @throws SQLException 
	 */
	private void initialize() throws IOException, ClassNotFoundException, SQLException {

//		 System.out.println("Loading test data set...");
//		 String[][] test = FileOpener.loadTable("/test.csv", 10);
		 
		 System.out.println("Loading train data set...");
		 String[][] train = FileOpener.loadTable("/complete.csv", NUMBER_OF_ATTRIBUTES);
		 
		 HashMap<Integer,String> attributeList = new HashMap<>();
		
		 //attributeList.put(RELEASE, "Release");
		 //attributeList.put(ARTIST_ID, "Artist_ID");
		 attributeList.put(GENRE, "Genre");
		 attributeList.put(YEAR, "Year");
		 attributeList.put(ARTIST_HOTNESS, "hotness");
		 attributeList.put(ARTIST_FAMILIARITY, "fam");
		 attributeList.put(DURATION, "duration");


		 boolean[] discreteValued = new boolean[NUMBER_OF_ATTRIBUTES];
		 //discreteValued[RELEASE] = true;
		 //discreteValued[ARTIST_ID] = true;
		 discreteValued[GENRE] = true;

		
		 double[] seperatingValues = new double[NUMBER_OF_ATTRIBUTES];
		 seperatingValues[YEAR] = 2000;
		 seperatingValues[ARTIST_FAMILIARITY] = .7;
		 seperatingValues[ARTIST_HOTNESS] = .7;
		 seperatingValues[DURATION] = 240;

		 System.out.println("Generating decision tree from train set...");
		 decisionTree = new DecisionTree(train, attributeList, ARTIST_ID,
				 	discreteValued, seperatingValues);
		 
		 System.out.println("Tree Structure");
		 decisionTree.printTreeStructure();
		 System.out.println("Finished printing tree structure");
//		 System.out.println("Calculating accuracy againts test set.");
//		 double hits = 0;
//		 double accuracy = 0;
//		 for(String[] tuple : test){
//			 String[][] result = decisionTree.suggestItems(tuple);
//			 
//			 if(result != null){
//				 for(String[] rTuple : result){
//					 if(tuple[ARTIST_ID].equals(rTuple[ARTIST_ID])){
//						 ++hits;
//						 break;
//					 }
//				 }
//			 }
//			 else{
//				 //ignoring misses due to attribute values in test 
//				 //data set not being in the training set
//				 ++hits;
//			 }
//			 
//
//		 }
//		 
//		 accuracy = hits/(double)test.length;
//		 
//		 System.out.println("Accuracy: " + accuracy);
		 

		frame = new JFrame();
		frame.getContentPane().setBackground(new Color(230, 230, 250));
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);

		comboBox = new JComboBox<Object>();
		comboBox.setBounds(35, 47, 360, 20);
		comboBox.setEditable(true);
		frame.getContentPane().add(comboBox);

		txtCurrentTrack = new JTextArea();
		txtCurrentTrack.setWrapStyleWord(true);
		txtCurrentTrack.setLineWrap(true);
		txtCurrentTrack.setText("Current Track");
		txtCurrentTrack.setBounds(35, 106, 360, 50);
		frame.getContentPane().add(txtCurrentTrack);
		
		JTextPane txtpnSearchForAn = new JTextPane();
		txtpnSearchForAn.setBackground(new Color(230, 230, 250));
		txtpnSearchForAn.setEditable(false);
		txtpnSearchForAn.setText("Search For An Artist:");
		txtpnSearchForAn.setBounds(35, 29, 157, 20);
		frame.getContentPane().add(txtpnSearchForAn);
		
		JTextPane txtpnSuggestedTrack = new JTextPane();
		txtpnSuggestedTrack.setBackground(new Color(230, 230, 250));
		txtpnSuggestedTrack.setEditable(false);
		txtpnSuggestedTrack.setText("Suggested Track:");
		txtpnSuggestedTrack.setBounds(35, 87, 140, 20);
		frame.getContentPane().add(txtpnSuggestedTrack);


		txtSearch = (JTextField) comboBox.getEditor().getEditorComponent();
		
		Class.forName("org.sqlite.JDBC");
		connection = DriverManager.getConnection("jdbc:sqlite:src/data/db/tagtraum.db");
		statement = connection.createStatement();
	

	}

	public void comboFilter(String enteredText) {
		List<String> resultArray = new ArrayList<String>();
		retrieveArray = new ArrayList<String>();

		try {

			String sql = "SELECT * " + "FROM complete " + "WHERE artist_name  LIKE '%"
					+ enteredText + "%'";

			
			ResultSet rs = statement.executeQuery(sql);

			int results = 0;

			while (rs.next() && results < MAXRESULTS) {


				resultArray.add(rs.getString("title") + "," + rs.getString("artist_name") + "," + rs.getString("release") + ","  + 
						 rs.getString("genre") + "," + rs.getInt("year"));
				retrieveArray.add(rs.getString("track_id"));
				++results;

			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		if (resultArray.size() > 0) {
			comboBox.setModel(new DefaultComboBoxModel<Object>(resultArray.toArray()));
			
			comboBox.setSelectedItem(enteredText);			
			//String[] suggestion = decisionTree.suggestItem(origin);
			
			comboBox.showPopup();
		} else {
			comboBox.hidePopup();
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		int selectedIndex = comboBox.getSelectedIndex();
		
		if(selectedIndex >= 0){
			String sql = "SELECT * FROM complete  WHERE track_id = '" +  retrieveArray.get(selectedIndex) + "'";

			
			try{
				ResultSet rs = statement.executeQuery(sql);
				if(rs.next()){
					origin[GENRE] = rs.getString("genre");
					origin[YEAR] = rs.getString("year");
					origin[ARTIST_HOTNESS] = rs.getString("artist_hotttnesss");
					origin[ARTIST_FAMILIARITY] = rs.getString("artist_familiarity");
					origin[DURATION] = rs.getString("duration");
					
					String[][] sugg = decisionTree.suggestItems(origin);
					Random r = new Random();
					int randomIndex = r.nextInt(((sugg.length - 1) - 0) + 1) - 0;
					
					if(sugg[randomIndex][YEAR].equals("0")){
						sugg[randomIndex][YEAR] = "Unknown Year";
					}
					txtCurrentTrack.setText(sugg[randomIndex][TITLE] + ", " + sugg[randomIndex][ARTIST_NAME] + ", " + sugg[randomIndex][GENRE] + ", " + sugg[randomIndex][YEAR]);
				}
			}
			catch(Exception ex){
				ex.printStackTrace();
			}
			
		}
		
		
	}
}
