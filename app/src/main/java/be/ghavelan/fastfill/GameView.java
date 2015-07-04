package be.ghavelan.fastfill;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * <p>
 * This class implements the grid view.
 * </p>
 */
public class GameView extends SurfaceView implements Runnable, SurfaceHolder.Callback {

    // The game session linked to this view
    private GameActivity game;
    //To draw the orthogonal lines
    private Paint stroke;
    //To fill shapes with color
    private Paint fill;
    //To fill shapes with striped lines
    private Paint striped;
    //To store the selected color (default : gray background)
    private int background;
    //Default background color
    private int DEFAULT_BACKGROUND;
    //Grid (set of orthogonal lines)
    private MondrianGrid mg = null;
    //Holder (to hold a display surface)
    private SurfaceHolder holder;
    //To update the UI from a different thread
    private Thread gameThread = null;
    //Thread state
    private boolean isRunning = false;
    //Boolean to allow drawing (when surface is first created)
    private boolean drawing = false;
    //Area already painted
    private int surface_painted = 0;
    //To save the randomly computed colored shapes (initial state)
    private Map<Rect, Integer> initiallyColoredShapes = null;
    //To store once (and only once) each chosen color
    private HashSet<Integer> chosenColors = null;
    //To hide the game view
    private boolean hide = false;
    //To store all adjacent shapes with the same color
    private List<Rect> adjacent = null;

    public GameView(Context context) {
        super(context);
        initGame();
    }

    public GameView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        initGame();
    }

    public GameView(Context context, AttributeSet attributeSet, int defStyle) {
        super(context, attributeSet, defStyle);
        initGame();
    }

    private void initGame() {

        holder = getHolder();
        holder.addCallback(this);
        //To store the initially colored shapes (Rect--> integer map)
        initiallyColoredShapes = new LinkedHashMap<>();
        initiallyColoredShapes.clear();
        //Default background color
        background = getResources().getColor(R.color.mondrian_background);
        //To store used colors (once)
        chosenColors = new HashSet<>();
        chosenColors.clear();
        //Lines are drawn in black with a stroke
        stroke = new Paint();
        stroke.setAntiAlias(true);
        stroke.setStrokeWidth(getResources().getDimension(R.dimen.gv_line_width));
        stroke.setColor(getResources().getColor(R.color.line_color));
        stroke.setStyle(Paint.Style.STROKE);
        //The shapes are filled with colors
        fill = new Paint();
        fill.setAntiAlias(true);
        fill.setStyle(Paint.Style.FILL);
        //To bring out adjacent shapes with the same color (game over)
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.game_over_stripes);
        BitmapShader bitmapshader = new BitmapShader(bitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
        //If you lose, shapes are drawn with striped lines
        striped = new Paint();
        striped.setAntiAlias(true);
        striped.setStyle(Paint.Style.FILL);
        striped.setShader(bitmapshader);
        DEFAULT_BACKGROUND = getResources().getColor(R.color.mondrian_background);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        if (mg == null) {
            //Grid creation (width and height)
            mg = new MondrianGrid(getWidth(), getHeight());
            //Minimum distance between lines (in mm)
            final int REJECT_DISTANCE = 7;
            // Minimum distance between lines (in pixels)
            int min = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, REJECT_DISTANCE,
                    getResources().getDisplayMetrics());
            int rejection = getWidth() / 10;

            if (rejection < min) {
                rejection = min;
            }

            mg.setRejectionDistance(rejection, rejection);
            //Computation of the grid
            mg.computeMondrian(GameActivity.MIN_LINES);
            //Computation of initially colored shapes
            randomlyColorShapes(0, GameActivity.MAX_COLOURED, MondrianGrid.RANDOM);
            //Surface is created -> start drawing
            drawing = true;
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    public void run() {

        while (isRunning) {

            if (!holder.getSurface().isValid()) {
                continue;
            }
            Canvas surfaceCanvas = holder.lockCanvas();
            if (isReady()) {
                //To draw (via onDraw) the view from a thread which is not the UIThread
                postInvalidate();
            }
            holder.unlockCanvasAndPost(surfaceCanvas);
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //If the game is paused we mask the game view
        if (hide) {
            setBackgroundColor(getResources().getColor(R.color.black));
        }
        //Otherwise if the game is pending
        else {

            setBackgroundResource(R.drawable.grid_background);
            //Fill the shapes
            if (mg != null && !mg.getShapes().isEmpty()) {

                for (Map.Entry<Rect, Integer> e : mg.getShapes().entrySet()) {
                    fill.setColor(e.getValue());
                    canvas.drawRect(e.getKey(), fill);
                }
            }
            //If you lose (there is at least one adjacent shape with the same color)
            if (adjacent != null && !adjacent.isEmpty()) {

                for (Rect rect : adjacent) {
                    canvas.drawRect(rect, striped);
                }
            }
            //Stroke
            if (mg != null && !mg.getLines().isEmpty()) {

                for (Line line : mg.getLines()) {
                    canvas.drawLine(line.getStart().x, line.getStart().y, line.getEnd().x, line.getEnd().y, stroke);
                }

            }

        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int x = (int) event.getX();
        int y = (int) event.getY();

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                break;

            case MotionEvent.ACTION_MOVE:
                break;

            case MotionEvent.ACTION_UP:

                if (isAvailable(x, y) && background != DEFAULT_BACKGROUND && !game.isOk() && !game.isOver() &&
                        !game.isOnPause()) {
                    //selected shape
                    Rect selected = getSelectedShape(x, y);

                    if (selected != null) {
                        //save the map shape -> color
                        mg.putShape(selected, background);
                        //add color in the set
                        chosenColors.add(background);
                        //compute the painted area
                        surface_painted += selected.width() * selected.height();
                        //true is the game is over
                        boolean isOver = isAdjacentShapesWithSameColor(selected, background);

                        if (isOver) {
                            adjacent = getAdjacentShapesWithSameColor(selected, background);
                            adjacent.add(selected);
                            game.gameOver();

                        } else if (surface_painted < getWidth() * getHeight()) {

                            game.updateScore(computeScore());
                        }
                        //the entire surface is painted
                        else if (surface_painted == getWidth() * getHeight()) {

                            game.increaseGridNumber();
                            game.updateScore(computeScore());
                            game.gameSuccess();
                        }
                    }

                }

                break;
        }

        return true;
    }

    /**
     * Computes the score.
     *
     * @return Score as an integer.
     */
    private int computeScore() {

        int bonus;

        switch (chosenColors.size()) {

            case 1:
                bonus = 5;
                break;

            case 2:
                bonus = 4;
                break;

            case 3:
                bonus = 3;
                break;

            case 4:
                bonus = 2;
                break;

            default:
                bonus = 0;
                break;

        }

        return bonus;
    }

    /**
     * To pause the thread.
     */
    public void pause() {

        isRunning = false;

        while (true) {

            try {
                //wait until the end of gameThread.
                gameThread.join();

            } catch (InterruptedException e) {

                e.printStackTrace();
            }

            break;

        }
        gameThread = null;
    }

    /**
     * To resume the thread.
     */
    public void resume() {
        isRunning = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    /**
     * To change the background color.
     *
     * @param color color.
     */
    public void setColor(int color) {
        this.background = color;
    }

    /**
     * @return True if the surface is ready to be painted
     */
    private boolean isReady() {
        return drawing;
    }

    /**
     * Checks whether or not a shape has already been selected.
     *
     * @param x x coordinate of the selected pixel.
     * @param y y coordinate of the selected pixel.
     * @return True if the selected shape has not already been selected.
     */
    private boolean isAvailable(int x, int y) {

        if (mg.getShapes().isEmpty()) {
            return true;

        } else {

            for (Map.Entry<Rect, Integer> e : mg.getShapes().entrySet()) {

                if (e.getKey().contains(x, y)) {

                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Returns the Rect area containing the selected pixel
     *
     * @param x x-coordinate of the selected pixel
     * @param y y-coordinate of the selected pixel
     * @return Rectangle
     */
    public Rect getSelectedShape(int x, int y) {

        if (!isOnEdge(x, y)) {

            List<Point> intersections_y = new ArrayList<>();
            List<Point> intersections_x = new ArrayList<>();
            intersections_y.clear();
            intersections_x.clear();
            int index_x, index_y;
            intersections_y = mg.findIntersection(new Line(new Point(x, 0), new Point(x, mg.getHeight())));
            intersections_y.add(new Point(x, y));
            intersections_x = mg.findIntersection(new Line(new Point(0, y), new Point(mg.getWidth(), y)));
            intersections_x.add(new Point(x, y));
            int[] values_y = new int[intersections_y.size()];
            int[] values_x = new int[intersections_x.size()];
            for (int j = 0; j < intersections_y.size(); j++) {
                values_y[j] = intersections_y.get(j).y;
            }
            for (int j = 0; j < intersections_x.size(); j++) {
                values_x[j] = intersections_x.get(j).x;
            }
            Arrays.sort(values_x);
            Arrays.sort(values_y);
            index_x = Arrays.binarySearch(values_x, x);
            index_y = Arrays.binarySearch(values_y, y);
            intersections_y.clear();
            intersections_x.clear();

            return new Rect(values_x[index_x - 1], values_y[index_y - 1], values_x[index_x + 1],
                    values_y[index_y + 1]);
        }

        return null;
    }

    /**
     * To check if a given point is on the lines defining the grid
     *
     * @param x x-coordinate of the selected pixel
     * @param y y-coordinate of the selected pixel
     * @return Boolean
     */
    private boolean isOnEdge(int x, int y) {

        Point selectedPoint = new Point(x, y);

        List<Line> lines = mg.getLines();
        for (int i = 0, linesSize = lines.size(); i < linesSize; i++) {
            Line line = lines.get(i);
            if (line.contains(selectedPoint)) {
                return true;
            }
        }

        return false;

    }

    /**
     * To know if a selected area can be painted with a given color
     *
     * @param current Selected area
     * @param color   Color of the selected area
     * @return True if an adjacent area is already painted with the same color
     */
    private boolean isAdjacentShapesWithSameColor(Rect current, int color) {

        if (!mg.getShapes().isEmpty()) {

            for (Map.Entry<Rect, Integer> entry : mg.getShapes().entrySet()) {

                // one shape on top of another
                if (current.bottom == entry.getKey().top || current.top == entry.getKey().bottom) {

                    if (current.left < entry.getKey().right && current.left >= entry.getKey().left && current.right >= entry.getKey().right) {

                        if (color == entry.getValue()) {

                            return true;

                        }

                    } else if (current.left >= entry.getKey().left && current.left < entry.getKey().right && current.right > entry.getKey().left &&
                            current.right <= entry.getKey().right) {

                        if (color == entry.getValue()) {
                            return true;

                        }

                    } else if (current.left <= entry.getKey().left && current.right > entry.getKey().left && current.right <= entry.getKey().right) {

                        if (color == entry.getValue()) {
                            return true;

                        }

                    } else if (current.left < entry.getKey().left && current.right > entry.getKey().right) {

                        if (color == entry.getValue()) {
                            return true;

                        }

                    }

                }
                //One shape on the side of another
                else if (current.right == entry.getKey().left || current.left == entry.getKey().right) {

                    if (current.bottom <= entry.getKey().bottom && current.bottom > entry.getKey().top && current.top <= entry.getKey().top) {

                        if (color == entry.getValue()) {
                            return true;

                        }

                    } else if (current.bottom > entry.getKey().top && current.bottom <= entry.getKey().bottom && current.top >= entry.getKey().top &&
                            current.top < entry.getKey().bottom) {

                        if (color == entry.getValue()) {
                            return true;

                        }

                    } else if (current.top < entry.getKey().bottom && current.top >= entry.getKey().top && current.bottom >= entry.getKey().bottom) {

                        if (color == entry.getValue()) {
                            return true;

                        }

                    } else if (current.top < entry.getKey().top && current.bottom > entry.getKey().bottom) {

                        if (color == entry.getValue()) {
                            return true;

                        }

                    }

                }

            }

        }

        return false;
    }

    /**
     * To have a list of adjacent shapes with the same color (game over case)
     *
     * @param current Selected area
     * @param color   Color of the selected area
     * @return A list of adjacent shapes with the same color
     */
    private List<Rect> getAdjacentShapesWithSameColor(Rect current, int color) {

        List<Rect> adjacent = null;

        if (!mg.getShapes().isEmpty()) {

            adjacent = new ArrayList<>();
            adjacent.clear();

            for (Map.Entry<Rect, Integer> entry : mg.getShapes().entrySet()) {

                // one shape on top of another
                if (current.bottom == entry.getKey().top || current.top == entry.getKey().bottom) {

                    if (current.left < entry.getKey().right && current.left >= entry.getKey().left && current.right >= entry.getKey().right) {

                        if (color == entry.getValue()) {

                            adjacent.add(entry.getKey());

                        }

                    } else if (current.left >= entry.getKey().left && current.left < entry.getKey().right && current.right > entry.getKey().left &&
                            current.right <= entry.getKey().right) {

                        if (color == entry.getValue()) {
                            adjacent.add(entry.getKey());

                        }

                    } else if (current.left <= entry.getKey().left && current.right > entry.getKey().left && current.right <= entry.getKey().right) {

                        if (color == entry.getValue()) {
                            adjacent.add(entry.getKey());

                        }

                    } else if (current.left < entry.getKey().left && current.right > entry.getKey().right) {

                        if (color == entry.getValue()) {
                            adjacent.add(entry.getKey());

                        }

                    }

                }
                //One shape on the side of another
                else if (current.right == entry.getKey().left || current.left == entry.getKey().right) {

                    if (current.bottom <= entry.getKey().bottom && current.bottom > entry.getKey().top && current.top <= entry.getKey().top) {

                        if (color == entry.getValue()) {
                            adjacent.add(entry.getKey());

                        }

                    } else if (current.bottom > entry.getKey().top && current.bottom <= entry.getKey().bottom && current.top >= entry.getKey().top &&
                            current.top < entry.getKey().bottom) {

                        if (color == entry.getValue()) {
                            adjacent.add(entry.getKey());

                        }

                    } else if (current.top < entry.getKey().bottom && current.top >= entry.getKey().top && current.bottom >= entry.getKey().bottom) {

                        if (color == entry.getValue()) {
                            adjacent.add(entry.getKey());

                        }

                    } else if (current.top < entry.getKey().top && current.bottom > entry.getKey().bottom) {

                        if (color == entry.getValue()) {
                            adjacent.add(entry.getKey());

                        }

                    }

                }

            }

        }

        return adjacent;
    }

    /**
     * Getter
     *
     * @return Running (or not) thread
     */
    public Thread getGameThread() {
        return gameThread;
    }

    /**
     * Getter
     *
     * @return Grid
     */
    public MondrianGrid getGrid() {

        return mg;
    }

    /**
     * Selects and colors random shapes.
     *
     * @param min Minimum number of pre-painted shapes.
     * @param max Maximum number of pre-painted shapes.
     */
    public void randomlyColorShapes(int min, int max, Random rand) {
        //Number of pre-colored shapes
        int nb_shapes = Utility.randInt(min, max, rand);
        int color = 0;
        int x, y, col;
        int cpt = 0;
        Rect current;
        if (nb_shapes > 0) {
            while (cpt < nb_shapes) {
                x = Utility.randInt(this.getLeft() + 1, this.getWidth() - 1, rand);
                y = Utility.randInt(this.getTop() + 1, this.getHeight() - 1, rand);
                current = getSelectedShape(x, y);
                col = Utility.randInt(0, 3, rand);

                switch (col) {

                    case 0:
                        color = getResources().getColor(R.color.red);
                        break;

                    case 1:
                        color = getResources().getColor(R.color.blue);
                        break;

                    case 2:
                        color = getResources().getColor(R.color.yellow);
                        break;

                    case 3:
                        color = getResources().getColor(R.color.white);
                        break;
                }

                if (current != null && isAvailable(x, y) &&
                        !isAdjacentShapesWithSameColor(current, color)) {

                    initiallyColoredShapes.put(current, color);
                    mg.putShape(current, color);
                    surface_painted += current.width() * current.height();
                    cpt++;
                }

            }
        }

    }

    /**
     * Setter : game object
     *
     * @param game Current game object
     */
    public void setGame(GameActivity game) {
        this.game = game;
    }

    /**
     * To clear the colored shapes.
     */
    public void clearColoredShapes() {
        this.surface_painted = 0;
        background = getResources().getColor(R.color.mondrian_background);
        mg.clearShapes();
        if (adjacent != null) {
            adjacent.clear();
        }
    }

    /**
     * Updates the painted surface to a predefined value.
     *
     * @param surface area already painted.
     */
    public void updateSurfacePainted(int surface) {
        this.surface_painted += surface;
    }

    /**
     * Returns saved shapes
     *
     * @return Map with shapes and color
     */
    public Map<Rect, Integer> getInitiallyColoredShapes() {
        return initiallyColoredShapes;
    }

    /**
     * Returns the set of already used colors.
     *
     * @return HashSet of used colors
     */
    public HashSet<Integer> getChosenColors() {
        return chosenColors;
    }

    /**
     * Hides the game view
     */
    public void hide() {
        this.hide = true;
    }

    /**
     * Shows the game view
     */
    public void show() {
        this.hide = false;
    }

}
