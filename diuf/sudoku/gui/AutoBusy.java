package diuf.sudoku.gui;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

/**
 * Utilities to improve Swing's percieved speed.
 * <p>
 * When a long action is performed as a result of a user action on the
 * user interface, the user interface is not refreshed until the action
 * has completed. This makes the user feel that the program is slow.
 * <p>
 * This class provides utilities that automatically replaces existing
 * action listener by wrapping them into listener providing the following:
 * <ul>
 * <li>A busy cursor is automatically displayed on the whole window is the
 * action is not finished after a small delay.
 * <li>The action are performed <i>after</i> swing has refreshed the
 * user interface.
 * </ul>
 */
public class AutoBusy {

    static Window getWindow(Component cmp) {
        if (cmp == null)
            return null;
        else if (cmp instanceof JComponent)
            return (Window)((JComponent)cmp).getTopLevelAncestor();
        else if (cmp instanceof Window)
            return (Window)cmp;
        else
            return getWindow(cmp.getParent());
    }

    static Window getWindow(EventObject e) {
        Window result = getWindow((Component)e.getSource());
        return result;
    }

    public static void setBusy(Component cmp, boolean busy) {
        Cursor cmpCursor = Cursor.getPredefinedCursor(busy ? Cursor.WAIT_CURSOR : Cursor.DEFAULT_CURSOR);
        Cursor txtCursor = Cursor.getPredefinedCursor(busy ? Cursor.WAIT_CURSOR : Cursor.TEXT_CURSOR);
        if (cmp == null) {
            Frame[] frames = Frame.getFrames();
            for (int i = 0; i < frames.length; i++)
                setCursor(frames[i], cmpCursor, txtCursor);
        } else {
            setCursor(cmp, cmpCursor, txtCursor);
        }
    }

    private static void setCursor(Component cmp, Cursor cmpCursor, Cursor txtCursor) {
        if (cmp instanceof TextComponent || cmp instanceof JTextComponent)
            cmp.setCursor(txtCursor);
        else
            cmp.setCursor(cmpCursor);
        if (cmp instanceof Container) {
            Component[] childs = ((Container)cmp).getComponents();
            for (int i = 0; i < childs.length; i++)
                setCursor(childs[i], cmpCursor, txtCursor);
        }
    }

    /**
     * This event listener class wraps all other listener and invoke them
     * with SwingUtilities.invokeLater() to ensure the gui is refreshed first.
     * It also displays the busy cursor (with a small delay) until all listeners
     * have finished.
     */
    static class BusyActionListener
            implements ActionListener, ItemListener, ChangeListener, ListSelectionListener {

        EventListener[] als;

        public BusyActionListener(EventListener[] als) {
            this.als = als;
        }

        private class DelayedBusy extends Thread {
            Component cmp;
            public volatile boolean isFinished = false;
            public DelayedBusy(Component cmp) {
                this.setName("AutoBusy.DelayedBusy");
                this.cmp = cmp;
            }
            @Override
            public void run() {
                try {
                    sleep(300);
                } catch(InterruptedException ex) {
                }
                synchronized(this) {
                    if (!isFinished)
                        setBusy(cmp, true);
                }
            }
        }

        private void event(final EventObject e) {
            final DelayedBusy db = new DelayedBusy(getWindow(e));
            db.start();
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    SwingUtilities.invokeLater(new Runnable() {

                        public void run() {
                            try {
                                for (int i = 0; i < als.length; i++) {
                                    if (!(als[i] instanceof BusyActionListener)) {
                                        if (als[i] instanceof ActionListener)
                                            ((ActionListener)als[i]).actionPerformed((ActionEvent)e);
                                        else if (als[i] instanceof ItemListener)
                                            ((ItemListener)als[i]).itemStateChanged((ItemEvent)e);
                                        else if (als[i] instanceof ChangeListener)
                                            ((ChangeListener)als[i]).stateChanged((ChangeEvent)e);
                                        else if (als[i] instanceof ListSelectionListener)
                                            ((ListSelectionListener)als[i]).valueChanged((ListSelectionEvent)e);
                                    }
                                }
                            } catch(Throwable ex) {
                                ex.printStackTrace();
                            }
                            synchronized(db) {
                                db.isFinished = true;
                                if (db.isAlive())
                                    db.interrupt();
                                setBusy(getWindow(e), false);
                            }
                        }
                    });
                }
            });
        }

        public void actionPerformed(ActionEvent e) {
            event(e);
        }

        public void itemStateChanged(ItemEvent e) {
            event(e);
        }

        public void stateChanged(ChangeEvent e) {
            event(e);
        }

        public void valueChanged(ListSelectionEvent e) {
            event(e);
        }

    }

    private static void addAutoBusy(AbstractButton button) {
        ActionListener[] als = button.getListeners(ActionListener.class);
        if (als.length > 0 && als[0] instanceof BusyActionListener)
            return;
        for (int i = 0; i < als.length; i++)
            button.removeActionListener(als[i]);
        button.addActionListener(new BusyActionListener(als));
        ItemListener[] ils = button.getListeners(ItemListener.class);
        if (ils.length > 0 && ils[0] instanceof BusyActionListener)
            return;
        for (int i = 0; i < ils.length; i++)
            button.removeItemListener(ils[i]);
        button.addItemListener(new BusyActionListener(ils));
    }

    /**
     * Modify the event listeners of this component and all its descendants
     * in such a way that:
     * <ul>
     * <li>The event is invoked <i>after</i> swing has refreshed the user interface
     * <li>A busy pointer is displayed on the window if the event has not
     * finished after a small delay.
     * </ul>
     * @param cmp the component whose listeners to modify
     */
    public static void addFullAutoBusy(Component cmp) {
        synchronized (cmp.getTreeLock()) {
            if (cmp instanceof Container) {
                Component[] childs = ((Container)cmp).getComponents();
                for (int i = 0; i < childs.length; i++)
                    addFullAutoBusy(childs[i]);
            }
            if (cmp instanceof AbstractButton) {
                addAutoBusy((AbstractButton)cmp);
            }
        }
    }

}