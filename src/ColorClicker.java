import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class ColorClicker implements MouseListener {
    private int colourClicked = 0;
    private Window w;

    ColorClicker(int a, Window win) {
        colourClicked = a;
        w = win;
    }

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {
        System.out.println("Clicked on colour " + colourClicked);
        w.changePixels(colourClicked);
    }

    @Override
    public void mousePressed(MouseEvent mouseEvent) {
        //Nothing
    }

    @Override
    public void mouseReleased(MouseEvent mouseEvent) {
        //Nothing
    }

    @Override
    public void mouseEntered(MouseEvent mouseEvent) {
        //Nothing
    }

    @Override
    public void mouseExited(MouseEvent mouseEvent) {
        //Nothing
    }
}
