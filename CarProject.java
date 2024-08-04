import javafx.application.*;
import javafx.event.*;
import javafx.scene.*;
import javafx.scene.image.*;
import javafx.scene.control.*;
import javafx.scene.control.Alert.*;
import javafx.scene.text.*;
import javafx.scene.layout.*;
import javafx.scene.shape.*;
import javafx.stage.*;
import javafx.geometry.*;
import javafx.animation.*;
import java.io.*;
import java.util.*;
import java.net.*;
import javafx.scene.input.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.geometry.Point2D;
import javafx.scene.shape.Polygon;

/**
*  Java Car Project - The main file to run the game
*  @author Domagoj Kurf�rst
*/

public class CarProject extends Application
{
   /*
    * All the attributes
    */
   private Stage stage;
   private Scene scene;
   private VBox root;
   private static String[] args;
   private final static String ICON_IMAGE="assets/Car.png";  // file with icon for a racer
   private int iconWidth; // width (in pixels) of the icon
   private int iconHeight;  // height (in pixels) or the icon
   private Image carImage =  null;
   private AnimationTimer timer;  // timer to control animation
   private double speedS;
   double steeringAngle = 0;
   
   // networking
   public static final int SERVER_PORT = 1234;
   Vector <String> players = new Vector <String>();
   Vector <String> message = new Vector <String>();
   Socket socket = null;
   TextArea taLog = new TextArea();
   ArrayList <CarRacer> cars = new ArrayList<CarRacer>();
   ObjectInputStream ois = null;
   ObjectOutputStream oos = null;
   int counter;
   
   /**
   *  The main method
   *  @param args
   */
   public static void main(String [] _args) 
   {
      args = _args;
      launch(args);
   }
   
   /*
    * Void method
    */
   public void start(Stage _stage) 
   {
      // Setting up the stage
      stage = _stage;
      stage.setTitle("Java Project Race game");
      stage.setOnCloseRequest(
         new EventHandler<WindowEvent>() 
         {
            public void handle(WindowEvent evt) 
            {
               System.exit(0);
            }
         });
      root = new VBox();
      initializeScene();
   }

   /*
    * Initializing and starting the race
    */
   public void initializeScene() 
   {
      try 
      {
         carImage = new Image(new FileInputStream(ICON_IMAGE));
      }
      catch(Exception e) 
      {
         System.out.println("Exception: " + e);
         System.exit(1);
      }
            
      // Size of the image
      iconWidth = (int)carImage.getWidth();
      iconHeight = (int)carImage.getHeight();
      /*
       * Adding the amount of players on screen
       */
      for(int i=0; i<4; i++)
      {
         CarRacer racer = new CarRacer(i);
         root.getChildren().add(racer);
         root.setId("pane");  
         cars.add(racer);
      }
      
      scene = new Scene(root, 1400, 800);
      scene.getStylesheets().addAll(this.getClass().getResource("style.css").toExternalForm());
      stage.setScene(scene);
      stage.show(); 
      System.out.println("Starting race...");  
      
      /*
       * The key event where sets the movement speed of the car
       */
      scene.setOnKeyPressed(
         new EventHandler<KeyEvent>() 
         {
            @Override
            public void handle(KeyEvent event) 
            {
               if (event.getCode() == KeyCode.LEFT) 
               {
                  steeringAngle = -30;
               }
               else if(event.getCode() == KeyCode.RIGHT)
               {
                  steeringAngle = 30;
               }
               else if(event.getCode() == KeyCode.UP)
               {
                  speedS = 5;
               }
               else if(event.getCode() == KeyCode.DOWN)
               {
                  speedS = -5;
               } 
               else 
               {
                  return;
               }
            }
         });
         
           /*
            * control key released
            * setting speed when key is released
            * setting speed on 0 because we don't control the car
           */          
      scene.setOnKeyReleased(
         new EventHandler<KeyEvent>() 
         {
            @Override
            public void handle(KeyEvent event) 
            {
               if (event.getCode() == KeyCode.LEFT) 
               {
                  steeringAngle = 0;
               }
               else if(event.getCode() == KeyCode.RIGHT)
               {
                  steeringAngle = 0;
               }
               else if(event.getCode() == KeyCode.UP)
               {
                  steeringAngle = 0;
                  speedS=0;
               }
               else if(event.getCode() == KeyCode.DOWN)
               {
                  steeringAngle = 0;
                  speedS=0;
               }
            }
         });
      doConnect();
      
     
      
      /*
       * Use an animation to update the screen
       */
      timer = 
         new AnimationTimer() 
         {
            public void handle(long now) 
            {
               for(int i= 0; i<cars.size(); i++)
               {
                  cars.get(i).update();
               }
            }
         };
      
      /*
       * TimerTask to delay start of race for 2 seconds
       */
      TimerTask task = 
         new TimerTask() 
         {
            public void run() 
            { 
               timer.start();
            }
         };
      Timer startTimer = new Timer();
      long delay = 1000L;
      startTimer.schedule(task, delay);
   }
   
   /* 
    *  Racer creates the race lane (Pane) and the ability to 
    *  keep itself going (Runnable) and the position of the cars.
    */
   protected class CarRacer extends Pane 
   {
      public int racePosX=20;          
      public int racePosY=0;         
      public int raceROT= 0;          
      private ImageView aPicView;   
      public int index=0;
      
      public CarRacer(int carIndex) 
      {
         aPicView = new ImageView(carImage);
         this.getChildren().add(aPicView);
         this.index = carIndex;
      }
      
   
   
      /*
       *  update() method keeps the thread (racer) alive and moving.  
       */
      public void update() 
      { 
         aPicView.setTranslateX(racePosX);
         aPicView.setTranslateY(racePosY);
         aPicView.setRotate(raceROT);
         
         if(index != counter)
         {
            return;
         }
         
         /*
         *  creating car object with his formula cordinate
         */         
         CarOrder car = new CarOrder(racePosX, racePosY, raceROT, index);
         try
         {
            oos.writeObject(car);
            oos.flush();
         }
         catch(IOException ioe){}
      
         /*
            * control for car
            * formula how car can go forward, left , right , bottom
         */      
         double deltaRaceROTX = speedS * Math.cos(raceROT * Math.PI / 180.0 );
         racePosX += deltaRaceROTX;
         
         double deltaRaceROTY = speedS * Math.sin(raceROT * Math.PI / 180.0 );
         racePosY += deltaRaceROTY;
      
         double deltaRaceROT = (speedS / 20.0) * Math.tan( (steeringAngle * Math.PI) / 180.0 )*40; 
         raceROT += deltaRaceROT;
         
         System.out.println(deltaRaceROT);
         
         try
         {
            Thread.sleep(20);
         }
         catch(InterruptedException ie){} 
      } 
   }  // end inner class Racer
   
     /* 
         * connecting with the server
         * setting socket and objects 
         * sending objects to the server that other players car see your cordinates
     */
   public void doConnect()
   {
            
      try
      {
         socket = new Socket("26.240.208.23", SERVER_PORT);
                  
         ois = new ObjectInputStream(socket.getInputStream());
         oos = new ObjectOutputStream(socket.getOutputStream());
            
         System.out.println("Connected");
         counter = (Integer)ois.readObject();
         System.out.println("Car index counter is : " + counter);   
      }
      catch(UnknownHostException uhe)
      {
         Alert alert = new Alert(AlertType.ERROR , "UnknownHostException: " + uhe.toString());
      }
      catch(IOException ioe)
      {
         Alert alert = new Alert(AlertType.ERROR , "IOException: " + ioe.toString());
      } 
      catch(ClassNotFoundException cnfe)
      {
         Alert alert = new Alert(AlertType.ERROR , "IOException: " + cnfe.toString());
      }
      
      /*
         * starting server
         * ReceieveThread
      */      
      ReceieveThread rt = new ReceieveThread();
      rt.start();
      System.out.println("ReceiveThread started...");
   }//end of void Connect()
   

   class ReceieveThread extends Thread
   {
         
      public void run()
      {
         try
         {
            System.out.println("Start with oos");
          //oos = new ObjectOutputStream((socket.getOutputStream()));
            System.out.println("Start with ois");
           //ois = new ObjectInputStream((socket.getInputStream()));
            System.out.println("Entered");
 
          /*
            * sending position of the car to the server
            * every position of x,y and rot sending to the server and other players can see your cordinate
          */          
            while(true)
            {
               Object car = new Object();  
               try
               {
                  car = ois.readObject();
                  CarOrder carO = (CarOrder)car;
                  
                  int indexAuta =  carO.getIndexAuta();
                  if(counter!=indexAuta)
                  {
                     for(int i=0;i<cars.size();i++)
                     {
                        if(cars.get(i).index ==indexAuta)
                        {
                           cars.get(i).racePosX =carO.getPosX(); 
                           cars.get(i).racePosY =carO.getPosY();
                           cars.get(i).raceROT =carO.getRot();
                        } //end of if for car index
                     } //end of for loop
                  } // end of if counter is not the same as indexAuta
               }
               catch(ClassNotFoundException cnfe)
               {
                  System.out.println("ClassNotFoundException: " + cnfe);
               }
               
               /*
                  * checking if this car is instance of class CarOrder
               */               
               if(car instanceof CarOrder)
               {
                  CarOrder order = (CarOrder)car;   
                  System.out.println(order); 
               }//end of if
            }//end of while 
         }
         catch(IOException ioe)
         {
            Alert alert = new Alert(AlertType.ERROR , "Exception "+ ioe.toString());
         }//end of catch              
      }//end of void
   }//end of inner  
} // end class Races