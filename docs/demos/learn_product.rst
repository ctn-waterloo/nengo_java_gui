Learning Multiplication
============================
*Purpose*: This is demo shows learning a familiar nonlinear function, multiplication.

*Comments*: The set up here is very similar to the other learning demos.  The main difference is that this demo learns a nonlinear projection from a 2D to a 1D space (i.e. multiplication).

*Usage*: When you run the network, it automatically has a random white noise input injected into it in both dimensions.

Turn learning on: To allow the learning rule to work, you need to move the 'switch' to +1.

Monitor the error:  When the simulation starts and learning is on, the error is high.  After about 10s it will do a reasonable job of computing the produt, and the error should be quite small.

Is it working? To see if the right function is being computed, compare the 'pre' and 'post' population value graphs. You should note that if either dimension in the input is small, the output will be small.  Only when both dimensions have larger absolute values does the output go away from zero (see the screen capture below).

*Output*: See the screen capture below. 

.. image:: images/learn-product.png


*Code*:
    .. literalinclude:: ../../dist-files/demo/learn_product.py




