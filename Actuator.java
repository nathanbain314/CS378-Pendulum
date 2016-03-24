/**
* This class simulates the behavior of Actuator. It receives the action value from the controller and sends it across to the process.
*/
import java.io.*;
import java.util.Arrays;

class Actuator implements Runnable {

  Physics physics;
  private ObjectInputStream in;
  double[] data;
  //double[] lastData;

  Actuator(Physics phy, ObjectInputStream in) {
    this.physics = phy;
    this.in = in;
    data = new double[physics.NUM_POLES];
    //lastData = data;

  }

  void init() {
    data = new double[physics.NUM_POLES];
    for (int i = 0; i < physics.NUM_POLES; i++) {
      data[i] = -0.75;
      //lastData[i] = data[i];
    }
    physics.update_actions(data);
  }


  public synchronized void run() {
    Parachute parachute = new Parachute();
    Thread parachuteThread = new Thread(parachute);
    while (true) {
      try {
        // read action data from control server
        Object obj = in.readObject();
        // add previous data to moving average
        //for (int i = 0; i < physics.NUM_POLES; i++) {
          //lastData[i] = lastData[i] * 0.8 + data[i] * 0.2;
        //}
        // extract new data from obj
        data = (double[]) (obj);
        assert(data.length == physics.NUM_POLES);
        // stop the last parachute from canceling action
        synchronized (parachute) {
          parachute.update();
        }

        physics.update_actions(data);
        // create a new parachute for this update
        parachute = new Parachute();
        parachuteThread = new Thread(parachute);
        parachuteThread.start();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }


  // cancels an action after a period of time
  private class Parachute implements Runnable {
    boolean actionsUpdated;

    Parachute() {
      actionsUpdated = false;
    }

    protected void update() {
      actionsUpdated = true;
    }

    public synchronized void run() {
      // wait until a sensor data packet should have arrived
      try {
        Thread.sleep((int)(110)); // 110 up to 180ms
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      // if not, adjust actions
      synchronized (this) {
        if (!actionsUpdated) {
          // could create and send a fake update here
          double newData[] = new double[physics.NUM_POLES];
          for (int i = 0; i < physics.NUM_POLES; i++) {
            newData[i] = 0;//data[i] - lastData[i];
          }

          physics.update_actions(newData);
        }
      }
    } // class Parachute

  }
}
