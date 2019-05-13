# Wolfram's Elementary Cellular Automaton Generator #

This project is a simple implementation of Wolfram's Elementary Cellular Automata. The system is implemented in Java and allows to plot the evolution of an elementary automaton over time.

## Elementary Cellular Automaton

An Elementary Cellular Automaton can be thought of a sequence of symbols (sometimes thought of as a row of cells) with the symbols 0 or 1 on it, or, in other words, the corresponding cell can be either "dead" or "alive".
The state of the sequence is computed from the previous state.
The state of a cell depends on the previous state of the cell itself, as well as it's surrounding neighbors.

For this simple automaton, we are considering the cell itself, and the two adjacent cells to the left and right as its neighbors. Thus, there are 2<sup>3</sup> (or eight) different possibilities of states per neighborhood.

We can represent the rules of a particular elementary cellular automaton as an 8-bit binary number. For example, rule number 90 = 01011010<sub>2</sub> would be:

|         **current pattern**         |  111  |  110  |  101  |  100  |  011  |  010  |  001  |  000  |
| :---------------------------------: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: |
|  **new state for the center cell**  |   0   |   1   |   0   |   1   |   1   |   0   |   1   |   0   |

Under this rule, if a cell is currently dead (0) and both of its immediate neighbors are alive (1), the cell would remain dead (0) in the next iteration (101 → 0 under the rule).

For more information on Elementary Cellular Automata check [Elementary cellular automaton @ Wikipedia](https://en.wikipedia.org/wiki/Elementary_cellular_automaton).

## How to use it

Enter the rule number to be used, then the number of steps to be printed to the screen.
The program draws each generation of the simulation as a successive row on the screen.

The "On in the middle" option creates a single state in the 1 state in the middle,
the "Randomize" option randomizes the initial automaton state.

## License

See the [LICENSE](https://github.com/gustavohb/wolfram-ca/blob/master/LICENSE) file for license rights and limitations (MIT)
