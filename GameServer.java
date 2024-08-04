/**
*  Java Car Project - Game Server file
*  @author Domagoj Kurf�rst
*
*/
import java.io.*;
import javafx.application.*;
import javafx.event.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.Alert.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import javafx.geometry.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class GameServer extends Application{
   /*
    * All the attributes
    */
   public static final int SERVER_PORT = 1234;
   ServerSocket ss = null;
   Socket cSocket = null;
   ArrayList <CarOrder> orders = new ArrayList<CarOrder>();
   ArrayList <ObjectOutputStream> players = new ArrayList<ObjectOutputStream>();
   int counter = 0;
   
   
   TextArea taLog = new TextArea();
   
   
   
   /**
   *  The main method
   *  @param args
   */
   public static void main(String[] args) 
   {
      launch(args);
      new GameServer();
      // 
   }
   
   
   
   @Override
   public void start(Stage _stage) throws Exception{
     
     
      //set the window title
     _stage.setTitle("Game Server");

      
      VBox root = new VBox(10);
      
      Scene scene = new Scene(root, 360,400);
      
      
      root.getChildren().addAll(taLog);
      
    
      
     
      
      //connect stage with the Scene and show it, finalization
      _stage.setScene(scene);
      _stage.show();
      
   }
   
   /*
      * creating in the conotructor server thread that we can start server
      * waiting also for the clients to connect
      * if thread is started you will see message "Thread started"
   */  
   public GameServer(){
      ServerThread st = new ServerThread();
      st.start();
      System.out.println("Thread started");
      taLog.appendText(""+st+"\nThread Started");
   }//end of construcotr

   /*
      * inner class Server Thread
      * run method which runnign server and waiting for the clients
      * accepting server socekt and starting client server
   */
   class ServerThread extends Thread{
      public void run()
      {
         try
         {
            ss = new ServerSocket(SERVER_PORT);
            
            while(true)
            {
            
            
               cSocket = ss.accept();
               System.out.println("Server accepted");
               taLog.appendText("Server accepted");
               
               ClientThread ct = new ClientThread(cSocket);
               ct.start();
               System.out.println("Succesful Connected");
               taLog.appendText("Succesful Connected");
            }
         }
         catch(IOException ioe)
         {
            Alert alert = new Alert(AlertType.ERROR , "IOException: " + ioe.toString());
         }
      } //end of run
   } // end of inner

   /*
     * if player is connected clientThread will say send me your cordinates
     * client will receive cordinates and send in inner class server
     * he is counting how many players are in the game or in the server asking everyone for cordinates
   */

   class ClientThread extends Thread
   {
      ObjectOutputStream oos = null;
      ObjectInputStream ois = null;
      private Socket cSocket = null;
        
      public ClientThread(Socket s)
      {
         this.cSocket = s;
      }//end of constructor
      
      /*
      * run method contains ObjectOutputStream and ObjectInputStream
      * when we add first player if another is connected counter will increase for one
      * and that player will have their own id and everyone in the game can see his controlls
      */      
      public void run()
      {
         try
         {
            oos = new ObjectOutputStream((cSocket.getOutputStream()));
            ois = new ObjectInputStream((cSocket.getInputStream()));
            
            players.add(oos);
            oos.writeObject(counter);
            counter++;
            
            oos.flush();
            
            /*
               * reading object of car
               * if evverything is ok server is waiting for other client
            */          
            while(true)
            {
               Object car = new Object();  
               try
               {
                  car = ois.readObject();
               }
               catch(ClassNotFoundException cnfe)
               {
                  System.out.println("ClassNotFoundException: " + cnfe);
               }
               
               if(car instanceof CarOrder)
               {
                  CarOrder order = (CarOrder)car;
                  System.out.println(order);
                  taLog.appendText(""+order+"\n");
                 
                  for(int i=0; i<players.size(); i++)
                  {
                     if(oos != players.get(i))
                     {
                        players.get(i).writeObject(order);
                        players.get(i).flush();
                     }
                  } 
               }//end of if
            }//end of while 
         }
         catch(IOException ioe)
         {
            Alert alert = new Alert(AlertType.ERROR , "Exception "+ ioe.toString());
         }//end of catch
 
      }//end of void      
   }//end of inner
}//end of class