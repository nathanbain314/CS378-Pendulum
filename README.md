# CS378-Pendulum

Report:

In order to balance the pendulum, we commented out all of the conditional statements in the calculate_action function in ControlServer.java. We replaced it with the following lines of code:

double action = 0;
double move = (pos - dest) > 0 ?
          // min / max add slow start near boundaries
          Math.min((pos - dest)  / 2, (TRACK_LIMIT - pos) * 3)
          : Math.max((pos - dest)  / 2, (TRACK_LIMIT + pos) * -3);

      action = 10 / (80 * 0.01745) * angle + angleDot + posDot + move;

return action;


These lines balance the pole on the cart while also providing a slower cart speed when the cart is near a boundary.
