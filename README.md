In order to balance the pendulum, we commented out all of the conditional statements in the calculate_action function in ControlServer.java. We replaced it with the following lines of code:

double action = 0;
double move = (pos - dest) > 0 ?
          // min / max add slow start near boundaries
          Math.min((pos - dest)  / 2, (TRACK_LIMIT - pos) * 3)
          : Math.max((pos - dest)  / 2, (TRACK_LIMIT + pos) * -3);

      action = 10 / (80 * 0.01745) * angle + angleDot + posDot + move;

return action;


These lines balance the pole on the cart while also providing a slower cart speed when the cart is near a boundary.

To implement two inverted pendulums, one following the other, we first created a second pendulum by changing the global variable NUM_POLES in Physics.java to 2. We did this same thing in ControlServer.java. We also initialized the position of the second pole in the Physics.java global array pole_init_pos. To ensure that a pole follows another pole, we wrote a function called follow:

double follow(int thisPole, double [] data, boolean goinRight) {
      double cartWidth = 0.4;
      double brakeBuffer = 0.05 + cartWidth;
      double pos = data[thisPole*4+2];
      // default not to hit a wall
      double minDelta = goinRight ? 10 - brakeBuffer : -10 + brakeBuffer;
      // find nearest cart in movement direction
      for (int i = 0; i < NUM_POLES; i++){
        double otherPos = data[i*4+2];
        double delta = otherPos - pos;

        if (goinRight && (0 < delta) && (delta < minDelta))
          minDelta = delta;
        else if (!goinRight && (minDelta < delta) && (delta < 0))
          minDelta = delta;
      }

      return pos + minDelta + brakeBuffer * (goinRight ? -1 : 1);
    }



The follow function is called in the control_pendulum function (also in ControlServer.java) inside of the for loop.


TODO::::::
- List & explain test cases.

- "Please also describe the files you submitted in the report and describe how to run your code."

	^From assignment description on the webpage.

