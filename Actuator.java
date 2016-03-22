/**
* This class simulates the behavior of Actuator. It receives the action value from the controller and sends it across to the process.
*/
import java.io.*;

class Actuator implements Runnable {

  Physics physics;
  private ObjectInputStream in;

  Actuator(Physics phy, ObjectInputStream in) {
    this.physics = phy;
    this.in = in;
  }

  void init() {
    double init_actions[] = new double[physics.NUM_POLES];
    for (int i = 0; i < physics.NUM_POLES; i++) {
      init_actions[i] = 0;
    }
    physics.update_actions(init_actions);
  }

  double[] data;

  public synchronized void run() {
    Parachute parachute = new Parachute();
    Thread parachuteThread = new Thread(parachute);
    while (true) {
      try {
        // read action data from control server
        Object obj = in.readObject();
        data = (double[]) (obj);
        assert(data.length == physics.NUM_POLES);
        synchronized (parachute) {
          parachute.update();
        }

        physics.update_actions(data);

        parachute = new Parachute();
        parachuteThread = new Thread(parachute);
        parachuteThread.start();
      } catch (Exception e) {
        e.printStackTrace();
      }

    }
  }

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
        Thread.sleep(100);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      // if not, adjust actions
      synchronized (this) {
        if (!actionsUpdated) {
          // simplest implementation: update actions to be 0
          // could create and send a fake update here
          for (double d : data) {
            d = 0;
          }
          physics.update_actions(data);
        }
      }
    } // class Parachute

  }
}
