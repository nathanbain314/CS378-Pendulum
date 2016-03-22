/**
* This class simulates the behavior of Actuator. It receives the action value from the controller and sends it across to the process.
*/
import java.io.*;
import java.util.Arrays;

class Actuator implements Runnable {

  Physics physics;
  private ObjectInputStream in;
  double[] data;
  double[] lastData;
  double sensorSamplingRate;

  Actuator(Physics phy, ObjectInputStream in, double sensorSamplingRate) {
    this.physics = phy;
    this.in = in;
    this.sensorSamplingRate = sensorSamplingRate;
    data = new double[physics.NUM_POLES];
    lastData = data;
  }

  void init() {
    data = new double[physics.NUM_POLES];
    for (int i = 0; i < physics.NUM_POLES; i++) {
      data[i] = 0;
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
        for (int i = 0; i < physics.NUM_POLES; i++) {
          lastData[i] = lastData[i] * 0.75 + data[i] * 0.25;
        }
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
        Thread.sleep((int)(sensorSamplingRate * 0.5));
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      // if not, adjust actions
      synchronized (this) {
        if (!actionsUpdated) {
          // could create and send a fake update here
          double newData[] = new double[physics.NUM_POLES];
          for (int i = 0; i < physics.NUM_POLES; i++) {
            newData[i] = data[i] - lastData[i];
          }
          System.out.println("parachuting actions");
          System.out.println("data: " + Arrays.toString(data));

          physics.update_actions(newData);
        }
      }
    } // class Parachute

  }
}
