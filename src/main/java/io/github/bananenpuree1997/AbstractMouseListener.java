package io.github.bananenpuree1997;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * A simple class so you don't need to implement all the
 * interface methods when you need a {@link MouseListener}.
 */
public abstract class AbstractMouseListener implements MouseListener {

    // @Override
    public void mouseClicked(MouseEvent e) {}

    // @Override
    public void mousePressed(MouseEvent e) {}

    // @Override
    public void mouseReleased(MouseEvent e) {}

    // @Override
    public void mouseEntered(MouseEvent e) {}

    // @Override
    public void mouseExited(MouseEvent e) {}
}
