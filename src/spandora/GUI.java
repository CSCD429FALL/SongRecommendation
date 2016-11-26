package spandora;

import java.awt.EventQueue;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JComboBox;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import decisionTree.DecisionTree;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;

/**
 * Author: Sergio Ramirez
 * 
 */
public class GUI {


	private static int TRACK_ID = 0, TITLE = 1, RELEASE = 2, ARTIST_ID = 3, 
			ARTIST_NAME = 4, DURATION = 5, ARTIST_FAMILIARITY = 6, ARTIST_HOTNESS = 7, 
			YEAR = 8, TAG = 9, TERM = 10, NUMBER_OF_ATTRIBUTES = 11;

	private DecisionTree decisionTree;

	private JComboBox<Object> comboBox;
	private JTextArea txtCurrentTrack;
	private JFrame frame;
	private JTextField txtSearch;
	private String trackID, trackTitle, trackArtist;
	private static int MAXRESULTS = 5;

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
	 * @throws IOException 
	 */
	public GUI() throws IOException {
		initialize();

		txtSearch.addKeyListener(new KeyAdapter(){
			public void keyReleased(KeyEvent ke){
				SwingUtilities.invokeLater(new Runnable(){

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
	 * @throws IOException 
	 */
	private void initialize() throws IOException {


		/**
		 * Second argument defines how many tuples to read, defining a value larger than the number of total tuples
		 * in data will default to max tuples in data.
		 */		
//		System.out.println("Loading multivalues");
//		HashMap<String, Integer> terms = FileOpener.getValues("jdbc:sqlite:src/data/db/artist_term.db", "terms", "term");
//		HashMap<String, Integer> tags = FileOpener.getValues("jdbc:sqlite:src/data/db/artist_term.db", "mbtags", "mbtag");
//
//		System.out.println(terms.size() + ", " + tags.size());
//
//		HashMap<Integer, HashMap<String, Integer>> multiValuedAttributes = new HashMap<>();
//		multiValuedAttributes.put(TERM, terms);
//		multiValuedAttributes.put(TAG, tags);
//		
//		System.out.println("loading dataset");
//		String[][] songs = FileOpener.loadTable("/tags.csv", NUMBER_OF_ATTRIBUTES, TRACK_ID, multiValuedAttributes);
//		
//		System.out.println("writin data");
//		FileWriter.write("src/data/songs.csv", songs);

//		HashMap<Integer,String> attributeList = new HashMap<>();
//
//		//Creating attribute list to be used in decision tree.
//		attributeList.put(RELEASE, "Release");
//		//attributeList.put(ARTIST_ID, "Artist ID");
//		attributeList.put(ARTIST_HOTTNESS, "Artist Hottness");
//		attributeList.put(TAG, "Tag");
//
//		//Setting the discrete valued attributes.
//		boolean[] discreteValued = new boolean[NUMBER_OF_ATTRIBUTES]; 
//		discreteValued[RELEASE] = true;
//		discreteValued[TAG] = true;
//		discreteValued[ARTIST_HOTTNESS] = true;
//
//		//Setting the separating values for the continuous attributes.
//		double[] seperatingValues = new double[NUMBER_OF_ATTRIBUTES];
//		//seperatingValues[ARTIST_HOTTNESS] = 0.5;
//
//		decisionTree = new DecisionTree(songs, attributeList, ARTIST_ID, discreteValued, seperatingValues);


		frame = new JFrame();
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);

		comboBox = new JComboBox<Object>();
		comboBox.setBounds(35, 11, 360, 20);
		comboBox.setEditable(true);
		frame.getContentPane().add(comboBox);

		txtCurrentTrack = new JTextArea();
		txtCurrentTrack.setText("Current Track");
		txtCurrentTrack.setBounds(35, 105, 299, 22);
		frame.getContentPane().add(txtCurrentTrack);

		JButton btnNewButton = new JButton("Next");
		btnNewButton.setBounds(335, 106, 72, 23);
		frame.getContentPane().add(btnNewButton);

		txtSearch = (JTextField) comboBox.getEditor().getEditorComponent();




		trackID = "";

	}

	public void comboFilter(String enteredText) {
		List<String> resultArray = new ArrayList<String>();


		try
		{

			String sql = 	"SELECT track_id, title, artist_name " + 
					"FROM complete " +
					"WHERE artist_name  LIKE '%" + enteredText + "%'";

			Class.forName("org.sqlite.JDBC");
			Connection connection = DriverManager.getConnection("jdbc:sqlite:src/data/db/tagtraum.db");
			Statement statement = connection.createStatement();
			ResultSet rs = statement.executeQuery(sql);

			int results = 0;		

			while(rs.next() && results < MAXRESULTS) {

				trackArtist = rs.getString("artist_name");
				trackID = rs.getString("track_id");
				trackTitle = rs.getString("title");
				resultArray.add(trackArtist + ", " + trackTitle);
				++results;

			}

		}
		catch(Exception e) {
			e.printStackTrace(); 
		}



		if (resultArray.size() > 0) {
			comboBox.setModel(new DefaultComboBoxModel<Object>(resultArray.toArray()));
			comboBox.setSelectedItem(enteredText);
			txtCurrentTrack.setText(trackID);
			comboBox.showPopup();
		}
		else {
			comboBox.hidePopup();
		}
	}
}
