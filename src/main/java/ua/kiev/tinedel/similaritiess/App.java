package ua.kiev.tinedel.similaritiess;

import fj.F;
import fj.P2;
import fj.data.Array;
import fj.data.List;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.util.Random;

import static fj.P.p;
import static fj.data.List.*;
import static fj.function.Integers.sum;

/**
 * Hello world!
 */

public class App {

    public static final List<P2<Integer, Integer>> shifts = range(-1, 2).map(new F<Integer, List<P2<Integer, Integer>>>() {
        @Override
        public List<P2<Integer, Integer>> f(Integer integer) {
            return range(-1, 2).zip(replicate(3, integer));
        }
    }).bind(new F<List<P2<Integer, Integer>>, List<P2<Integer, Integer>>>() {
        @Override
        public List<P2<Integer, Integer>> f(List<P2<Integer, Integer>> p2s) {
            return p2s;
        }
    }).removeAll(new F<P2<Integer, Integer>, Boolean>() {
        @Override
        public Boolean f(P2<Integer, Integer> integerIntegerP2) {
            return integerIntegerP2._1() == 0 && integerIntegerP2._2() == 0;
        }
    });

    public static void main(String[] args) {
        App app = new App();
        for (int i = 0; i <= 30; i++) {
            colors[30 - i] = new Color(i * 8, i * 8, i * 8);
        }
        app.buildAndDisplay();
    }

    private void buildAndDisplay() {
        JFrame frame = new JFrame("Conformism");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        buildContent(frame);
        frame.pack();
        frame.setVisible(true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (active) {
                    try {
                        calcSuccs();
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                updateColor();
                            }
                        });

                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                stealLetter();
                            }
                        });
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                    }
                }
            }
        }).start();
    }

    private void stealLetter() {
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                P2<JLabel, P2<Integer, Integer>> pair = shuffleArray(findNeighbors(i, j)).head();
                if(succs[pair._2()._1()][pair._2()._2()] > succs[i][j]) {
                    String label = labels[i][j].getText();
                    int k = r.nextInt(label.length());
                    char[] nl = label.toCharArray();
                    nl[k] = pair._1().getText().toCharArray()[k];
                    labels[i][j].setText(new String(nl));
                }
            }
        }
    }

    private static final Color[] colors = new Color[31];

    private void updateColor() {
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                labels[i][j].setForeground(colors[succs[i][j]]);
            }
        }
    }

    private void calcSuccs() {
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                succs[i][j] = calcSucc(labels[i][j], takeThree(shuffleArray(findNeighbors(i, j))));
            }
        }
    }

    private int calcSucc(final JLabel jLabel, List<P2<JLabel, P2<Integer, Integer>>> jLabels) {
        final String orig = jLabel.getText();
        return sum(jLabels.map(new F<P2<JLabel, P2<Integer, Integer>>, Integer>() {
            @Override
            public Integer f(P2<JLabel, P2<Integer, Integer>> jLabel) {
                return diffStrings(orig, jLabel._1().getText());
            }
        }));
    }

    private Integer diffStrings(String orig, String df) {
        char[] origArray = orig.toCharArray();
        char[] dfArray = df.toCharArray();
        int succ = 0;
        for (int i = 0; i < orig.length(); i++) {
            if (origArray[i] == dfArray[i])
                succ++;
        }
        return succ;
    }

    private <A> List<A> takeThree(List<A> jLabels) {
        return jLabels.take(3);
    }

    private List<P2<JLabel, P2<Integer, Integer>>> findNeighbors(final int i, final int j) {
        List<P2<JLabel, P2<Integer, Integer>>> lst =
                shifts.map(new F<P2<Integer, Integer>, P2<Integer, Integer>>() {
                    @Override
                    public P2<Integer, Integer> f(P2<Integer, Integer> p2) {
                        return p(p2._1() + i, p2._2() + j);
                    }
                }).filter(new F<P2<Integer, Integer>, Boolean>() {
                    @Override
                    public Boolean f(P2<Integer, Integer> p2) {
                        return p2._1() >= 0 && p2._1() < 10
                                && p2._2() >= 0 && p2._2() < 10;
                    }
                }).map(new F<P2<Integer, Integer>, P2<JLabel, P2<Integer, Integer>>>() {
                    @Override
                    public P2<JLabel, P2<Integer, Integer>> f(P2<Integer, Integer> p2) {
                        return p(labels[p2._1()][p2._2()], p2);
                    }
                });
        return lst;
    }

    private static <A> List<A> shuffleArray(List<A> lst) {
        Array<A> ar = lst.toArray();
        Random rnd = new Random();
        for (int i = lst.length() - 1; i > 0; i--) {
            int index = rnd.nextInt(i + 1);
            // Simple swap
            A a = ar.get(index);
            ar.set(index, ar.get(i));
            ar.set(i, a);
        }

        return ar.toList();
    }

    private JLabel[][] labels = new JLabel[10][10];
    private int[][] succs = new int[10][10];
    private volatile boolean active = true;

    private void buildContent(final JFrame frame) {
        JPanel panel = new JPanel(new MigLayout("insets 10, wrap 10"));
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                labels[i][j] = new JLabel(makeRandomText());
                panel.add(labels[i][j]);
            }
        }
        panel.add(new JButton(new AbstractAction("Ok") {
            @Override
            public void actionPerformed(ActionEvent e) {
                active = false;
                WindowEvent wev = new WindowEvent(frame, WindowEvent.WINDOW_CLOSING);
                Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(wev);
            }


        }), "span 10, center");
        frame.getContentPane().add(panel);
    }

    private String makeRandomText() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < 10; i++) {
            sb.append(randomChar());
        }

        return sb.toString();
    }

    private static final char[] chars = "qwertyuioplkjhgfdsazxcvbnm".toCharArray();
    private static final Random r = new Random();

    private char randomChar() {
        return chars[r.nextInt(chars.length)];
    }
}
