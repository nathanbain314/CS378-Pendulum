/**
 * This class simulates the behavior of Actuator. It receives the action value from the controller and sends it across to the process.
 */
import java.io.*;

class Actuator implements Runnable {

    Physics physics;
    private ObjectInputStream in;
    protected boolean actionsUpdated;

    Actuator(Physics phy, ObjectInputStream in) {
        this.physics = phy;
        this.in = in;
        actionsUpdated = false;
    }

    void init() {
        double init_actions[] = new double[physics.NUM_POLES];
        for (int i = 0; i < physics.NUM_POLES; i++) {
          init_actions[i] = 0.75;
        }
        physics.update_actions(init_actions);
    }

    public synchronized void run() {
        while (true) {
            try {
              // read action data from control server
              Object obj = in.readObject();
              double[] data = (double[]) (obj);
              assert(data.length == physics.NUM_POLES);
              actionsUpdated = true;
              physics.update_actions(data);
              ActionKiller actionKiller = new ActionKiller(physics);
              Thread actionKillThread = new Thread(actionKiller);
              actionKillThread.start();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    private class ActionKiller implements Runnable {
      Physics physics;

      ActionKiller(Physics phy) {
        actionsUpdated = false;
        this.physics = phy;
      }

      public synchronized void run() {
          try {
            Thread.sleep(100);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
          if (!actionsUpdated) {
            // simplest implementation: update actions to be 0
            double[] data = new double[physics.NUM_POLES];
            for (double d : data) {
              d = 0;
            }
            physics.update_actions(data);
          }
      }

    }
}
