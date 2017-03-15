import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.table.DefaultTableModel;

public class Fenetre extends JFrame {
	
	public JPanel panResult = new JPanel();
	public JTextArea texteRequete = new JTextArea();
	private JToolBar barreOutils = new JToolBar();
	private JButton boutonLecture = new JButton(new ImageIcon("images/lecture.png"));
	private String query;
	private JSplitPane split;
	
	private JTable tabDonnees = new JTable();
	private DefaultTableModel model = new DefaultTableModel();
	private Vector title = new Vector();
	private Vector data = new Vector();
	
	public Fenetre()
	{
		this.setSize(900,600);
	    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    this.setLocationRelativeTo(null);
	    
	    //ajout barre outils
	    barreOutils.add(boutonLecture);
	    getContentPane().add(barreOutils, BorderLayout.NORTH);
	    
	    //ajoute le split avec le texte de la requete et un pane resultat
	    panResult.setLayout(new BorderLayout());
	    split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,new JScrollPane(texteRequete) ,panResult);
	    split.setDividerLocation(150);
	    getContentPane().add(split, BorderLayout.CENTER);

	    //recupere le texte lorsque l'on appuye sur le bouton
	    boutonLecture.addActionListener(new ActionListener ()
	    {
	   		 public void actionPerformed(ActionEvent e)
	   		 {
	   			//recupere le texte de la requete
	   			query = texteRequete.getText();
	   			//met a jour le tableau
	   			getTableauDonnees(query);
	   		 }
	    });

	    this.setVisible(true);
	}
	
	private void getTableauDonnees(String query)
	{
		try 
		{
			long startTime = System.currentTimeMillis();
			
			title.clear();
			data.clear();
			
			Class.forName("org.postgresql.Driver");
			System.out.println("Driver OK");
			
			String url = "jdbc:postgresql://localhost:5432/Ecole";
			String user = "postgres";
			String passwd= "postgres";
			
			Connection conn = DriverManager.getConnection(url, user, passwd);
			System.out.println("Connection effective");
			
			//TYPE_SCROLL_SENSITIVE permet le retour en arriere
			Statement state = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
			
			//Liste les profs  
			//query = "SELECT * FROM professeur";
		      
			ResultSet result = state.executeQuery(query);
			
			ResultSetMetaData resultMeta = result.getMetaData();
			
			//recupere le nom des colonnes
			for(int i = 1; i <= resultMeta.getColumnCount(); i++)
				title.addElement(resultMeta.getColumnName(i).toUpperCase());
			
			//compte le nombre de ligne
			int rowCount = 0;
			while (result.next())
				rowCount++;
			
			//revient au début 
			result.beforeFirst() ;
			
			//recupere les donnes du tableau
			while(result.next())
			{
				Vector row = new Vector(resultMeta.getColumnCount());
				for(int i = 1; i <= resultMeta.getColumnCount(); i++)
				{
					row.addElement(result.getObject(i).toString());
				}	
				data.add(row);
			}
			
			result.close();
			state.close();
			
			long endTime = System.currentTimeMillis();
			
			//Clean le tableau précédent puis met à jour les données
			panResult.removeAll();
			panResult.add(new JScrollPane(new JTable(data, title)), BorderLayout.CENTER);
			panResult.revalidate();
			panResult.add(new JLabel("Requête exécutée en : " + (endTime - startTime) + " ms et retourne : " + rowCount + " lignes."), BorderLayout.SOUTH);
			
		} 
		catch (Exception e)
		{
			panResult.removeAll();
			panResult.add(new JScrollPane(new JTable()), BorderLayout.CENTER);
			panResult.revalidate();
			panResult.add(new JLabel("Erreur Requête"), BorderLayout.SOUTH);
		} 
	};
}
