/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;

/**
 *
 * @author Michaela
 */
public class Server {
    private String tsvPath = "C:/Users/Michaela/Documents/NetBeansProjects/NOG/ID2212/participants.tsv";
    private final int PORT = 4444;
    private ServerSocket socket; 
    
    static final String JDBC_DRIVER = "jdbc:derby://localhost:1527/Members";
    static protected Connection connection;
    
    public static void main(String[] args){
        Server server = new Server();
        if(server.isEmpty()){
            try{
                server.fillDatabase();
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        server.listen();
    }
    
    public Server(){
        
        try{
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
            connection = DriverManager.getConnection(JDBC_DRIVER, "michaela", "michaela");
            socket = new ServerSocket(PORT, 0, InetAddress.getByName(null));
            System.out.println("Created server");
            fillDatabase();
        }catch(IOException e){
            e.printStackTrace();
        }catch(ClassNotFoundException  ce){
            ce.printStackTrace();
        }catch(SQLException se){
            se.printStackTrace();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
    private void listen(){
        
        while(true){
            System.out.println("running");
            try{
                Socket client = socket.accept();
                ConnectionHandler handler = new ConnectionHandler(this, client);
                handler.start();
            }catch(IOException e){
                e.printStackTrace();
            }
        }
    }
    
        private void fillDatabase() throws Exception{
        BufferedReader TSVFile = new BufferedReader(new FileReader(tsvPath));
        String dataRow;
        String query = "INSERT INTO PARTICIPANTS (ID, NAME, GENDER, BIRTHDAY, HEIGHT, WEIGHT, SPORT, COUNTRY)"
                            + "VALUES(?, ?, ?, ?, ?, ?, ?, ?)";
        
        float height = 0f, weight = 0f;
        String name = " ", country = " ", birthday = " ", sport = " ", gender = " ";
        int ID = 0;
        
        PreparedStatement pState = null;
        pState = connection.prepareStatement(query);
       
        while((dataRow = TSVFile.readLine()) != null){
            //System.out.println(dataRow);
            String[] tokens = dataRow.split(" ");
            //System.out.println(Arrays.toString(tokens));
            for(int i = 0; i < tokens.length; i++){
                switch(i){
                    case 0: ID = Integer.parseInt(tokens[0]);
                        break;
                    case 1:
                        String tmp = tokens[1].substring(0, tokens[1].length()-2);
                        name = tmp + tokens[2];
                        break;
                    case 2: gender = tokens[3];
                        break;
                    case 3: country = tokens[4];
                        break;
                    case 4: birthday = tokens[5];
                        break;
                    case 5: height = Float.parseFloat(tokens[6]);
                        break;
                    case 6: weight = Float.parseFloat(tokens[7]);
                        break;                         
                }
                if(tokens.length == 8){
                   System.out.println(tokens.length); 
                   System.out.println(Arrays.toString(tokens));
                }
                 
                if(tokens.length - 8 > 1){
                    sport = tokens[8] + tokens[9];
                }else{
                    sport = tokens[8];
                }
            }
            
            pState.setInt(1, ID);
            pState.setString(2, name);
            pState.setString(3, gender);
            pState.setString(4, birthday);
            pState.setFloat(5, height);
            pState.setFloat(6, weight);
            pState.setString(7, sport);
            pState.setString(8, country);
                    
        }
        pState.close();
    }
        
    private boolean isEmpty(){
        String query = "SELECT * FROM PARTICIPANTS";
        try{
            Statement st = connection.createStatement();
            ResultSet resultSet = st.executeQuery(query);
            
            if(resultSet.next()){
                resultSet.close();
                return true;
            }else{
                resultSet.close();
                return false;
            }
        }catch(SQLException s){
            s.printStackTrace();
        }
        return false;
    }
}
