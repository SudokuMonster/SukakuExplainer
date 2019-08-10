package diuf.sudoku.io;

/**
 * Fast sine and cosine approximations.
 */
public class FastSinCos {

    private final static double B = 4.0 / Math.PI;
    private final static double C = -4.0 / (Math.PI * Math.PI);
    private final static double P = 0.218;
    private final static double hPI = Math.PI / 2.0;
    private final static double PI2 = Math.PI * 2.0;
    private final static double sPI = Math.PI * 1.5;


    public static double wrap(double angle) {
        angle = angle % PI2;
        if (angle > Math.PI)
            angle -= PI2;
        else if (angle < -Math.PI)
            angle += PI2;
        assert angle >= -Math.PI && angle <= Math.PI;
        return angle;
    }

    /**
     * Fast sine approximation
     * @param theta the argument
     * @return sin(theta)
     */
    public static double fastSin(double theta) {
        return fastSin0(wrap(theta));
    }
    
    /**
     * Fast cosine approximation
     * @param theta the argument
     * @return cos(theta)
     */
    public static double fastCos(double theta) {
        return fastSin0(wrap(theta + hPI));
    }
    
    /**
     * Fast sine approximation. Slightly faster than
     * {@link #fastSin(double)} but the argument must
     * be within the [-&pi;..&pi;] range!
     * @param theta the argument.
     * @return sin(theta)
     */
    public static double fastSin0(double theta) {
        double y = B * theta + C * theta * Math.abs(theta);
        y = P * (y * Math.abs(y) - y) + y;
        return y;
    }
    
    /**
     * Fast cosine approximation. Can be slightly faster than
     * {@link #fastCos(double)} but the argument must
     * be within the [-&pi;..&pi;] range!
     * @param theta the argument.
     * @return cos(theta)
     */
    public static double fastCos0(double theta) {
        if (theta > hPI) {
            theta -= sPI; // - 1.5pi
        } else {
            theta += hPI; // + 0.5pi
        }
        return fastSin0(theta);
    }
    
}
