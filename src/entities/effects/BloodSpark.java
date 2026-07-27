package entities.effects;

import empirebuilder.Game;

public class BloodSpark extends Effect{

    static int images = 8;

    public BloodSpark(double x, double y) {

        super(x, y, images, 0, 0);
    }

    @Override
    public void tick(Game game) {
        remainingDuration--;
    }

    @Override
    public String getImageName() {
        int imageNumber = initialDuration - remainingDuration + 1;
        if (imageNumber > images) return "bloodSpark" + images;
        return "bloodSpark" + imageNumber;
    }
}
