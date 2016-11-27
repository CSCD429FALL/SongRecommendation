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
import javax.swing.JTextPane;

/**
 * Author: Sergio Ramirez
 * 
 */
public class GUI implements ActionListener{

	private static int TRACK_ID = 0, TITLE = 1, RELEASE = 2, ARTIST_ID = 3, ARTIST_NAME = 4, DURATION = 5,
			ARTIST_FAMILIARITY = 6, ARTIST_HOTNESS = 7, YEAR = 8, GENRE = 9, LAST_PLAYED = 10, RATING = 11, NUMBER_OF_ATTRIBUTES = 12;
	
	private String[] origin = {"","","Music While You Work","AR00A1N1187FB484EB", "", "", "", "", "", "", "", ""};

	private DecisionTree decisionTree;

	private JComboBox<Object> comboBox;
	private JTextArea txtCurrentTrack;
	private JFrame frame;
	private JTextField txtSearch;
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
	 * 
	 * @throws IOException
	 */
	public GUI() throws IOException {
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
	 */
	private void initialize() throws IOException {

		 System.out.println("Loading test data set...");
		 String[][] test = FileOpener.loadTable("/test.csv", 10);
		 
		 System.out.println("Loading train data set...");
		 String[][] train = FileOpener.loadTable("/train.csv", NUMBER_OF_ATTRIBUTES);
		 
		 HashMap<Integer,String> attributeList = new HashMap<>();
		
		 attributeList.put(RELEASE, "Release");
		 attributeList.put(ARTIST_ID, "Artist_ID");
		 attributeList.put(GENRE, "Genre");


		 boolean[] discreteValued = new boolean[NUMBER_OF_ATTRIBUTES];
		 discreteValued[RELEASE] = true;
		 discreteValued[ARTIST_ID] = true;
		 discreteValued[GENRE] = true;

		
		 double[] seperatingValues = new double[NUMBER_OF_ATTRIBUTES];

		 System.out.println("Generating decision tree from train set...");
		 decisionTree = new DecisionTree(train, attributeList, GENRE,
				 	discreteValued, seperatingValues);

		 
		 System.out.println("Calculating accuracy againts test set.");
		 double hits = 0;
		 double accuracy = 0;
		 for(String[] tuple : test){
			 String result = decisionTree.predict(tuple);
			 if(result.equals(tuple[GENRE])){
				 ++hits;
			 }
		 }
		 
		 accuracy = hits/(double)test.length;
		 
		 System.out.println("Accuracy: " + accuracy);
		 

		frame = new JFrame();
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);

		comboBox = new JComboBox<Object>();
		comboBox.setBounds(35, 47, 360, 20);
		comboBox.setEditable(true);
		frame.getContentPane().add(comboBox);

		txtCurrentTrack = new JTextArea();
		txtCurrentTrack.setText("Current Track");
		txtCurrentTrack.setBounds(35, 105, 299, 22);
		frame.getContentPane().add(txtCurrentTrack);
		
		JTextPane txtpnSearchForAn = new JTextPane();
		txtpnSearchForAn.setEditable(false);
		txtpnSearchForAn.setText("Search For An Artist:");
		txtpnSearchForAn.setBounds(35, 29, 157, 20);
		frame.getContentPane().add(txtpnSearchForAn);
		
		JTextPane txtpnSuggestedTrack = new JTextPane();
		txtpnSuggestedTrack.setEditable(false);
		txtpnSuggestedTrack.setText("Suggested Track:");
		txtpnSuggestedTrack.setBounds(35, 84, 140, 20);
		frame.getContentPane().add(txtpnSuggestedTrack);


		txtSearch = (JTextField) comboBox.getEditor().getEditorComponent();

	

	}

	public void comboFilter(String enteredText) {
		List<String> resultArray = new ArrayList<String>();

		try {

			String sql = "SELECT * " + "FROM complete " + "WHERE artist_name  LIKE '%"
					+ enteredText + "%'";

			Class.forName("org.sqlite.JDBC");
			Connection connection = DriverManager.getConnection("jdbc:sqlite:src/data/db/tagtraum.db");
			Statement statement = connection.createStatement();
			ResultSet rs = statement.executeQuery(sql);

			int results = 0;

			while (rs.next() && results < MAXRESULTS) {


				resultArray.add(rs.getString("title") + "," + rs.getString("release") + "," + rs.getString("genre") + "," + rs.getString("artist_id") );
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
		String[] commaSep = ((String)comboBox.getSelectedItem()).split(",");
		
		if(commaSep.length == 4){
			origin[RELEASE] = commaSep[1];
			origin[ARTIST_ID] = commaSep[3];
			origin[GENRE] = commaSep[2];
			
			String[] sugg = decisionTree.suggestItem(origin);
			
			txtCurrentTrack.setText(sugg[TITLE] + ", " + sugg[ARTIST_NAME] + ", " + sugg[GENRE]);
		}
		
	}
}
