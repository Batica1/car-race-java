/**
*  Java Car Project
*  @author Domagoj Kurf�rst
*
*/
import java.io.Serializable;

public class CarOrder implements Serializable
{
   /*
    * All the attributes
    */   
   private int racePosX;
   private int racePosY;
   private int raceROT;
   private int indexAuta;
   private static final long  serialVersionUid = 01L;
   /**
   *  Constructor for Car Order
   *  Positioning etc...
   */
   public CarOrder(int x, int y, int _rot, int _indexAuta)
   {
      this.racePosX = x;
      this.racePosY = y;
      this.raceROT = _rot;
      this.indexAuta = _indexAuta;

   } //end of constructor
   
   /*
    * Get methods
    */
   public int getPosX()
   {
      return this.racePosX;
   }
   
   public int getPosY()
   {
      return this.racePosY;
   }
   
   public int getRot()
   {
      return this.raceROT;
   }
   
   public int getIndexAuta()
   {
      return this.indexAuta;
   }
   
   public String toString()
   {
      return "Pos X: " + getPosX() + 
             "| Pos Y: " + getPosY() + 
             "| Pos Rot: " + getRot();
   } //end of toString
} //end of class