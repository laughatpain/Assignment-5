import processing.core.*;

import java.net.URL;
import java.util.*;
import java.io.FileNotFoundException;
import java.io.File;

public class Main extends PApplet
{
   private static final int WORLD_WIDTH_SCALE = 2;
   private static final int WORLD_HEIGHT_SCALE = 2;

   private static final int SCREEN_WIDTH = 640;
   private static final int SCREEN_HEIGHT = 480;
   private static final int TILE_WIDTH = 32;
   private static final int TILE_HEIGHT = 32;

   private static final int TIMER_ACTION_DELAY = 100;

   private static final String IMAGE_LIST_FILE_NAME = "imagelist";
   private static final String DEFAULT_IMAGE_NAME = "background_default";
   private static final int DEFAULT_IMAGE_COLOR = 0x808080;

   private static final String SAVE_FILE_NAME = "gaia.sav";

   private ImageStore imageStore;
   private long next_time;
   private WorldModel world;
   private WorldView view;


   public void setup()
   {
      size(SCREEN_WIDTH, SCREEN_HEIGHT);
      imageStore = new ImageStore(
              createImageColored(TILE_WIDTH, TILE_HEIGHT, DEFAULT_IMAGE_COLOR));
      loadImages(IMAGE_LIST_FILE_NAME, imageStore, this);

      int num_cols = SCREEN_WIDTH / TILE_WIDTH * WORLD_WIDTH_SCALE;
      int num_rows = SCREEN_HEIGHT / TILE_HEIGHT * WORLD_HEIGHT_SCALE;

      // create default background
      Background background = createDefaultBackground(imageStore);

      // create world model
      world = new WorldModel(num_rows, num_cols, background);

      // load world
      Map<String, PropertyParser> parsers = buildPropertyParsers(world,
              imageStore, System.currentTimeMillis());
      loadWorld(world, SAVE_FILE_NAME, imageStore, parsers);

      // create world view
      view = new WorldView(SCREEN_WIDTH / TILE_WIDTH,
              SCREEN_HEIGHT / TILE_HEIGHT, this, world, TILE_WIDTH, TILE_HEIGHT);

      // update view?

      next_time = System.currentTimeMillis() + TIMER_ACTION_DELAY;
   }

   public void draw()
   {
      long time = System.currentTimeMillis();
      if (time >= next_time)
      {
         world.updateOnTime(time);
         next_time = time + TIMER_ACTION_DELAY;
      }

      view.drawViewport();
      drawPath();
   }

   public void keyPressed()
   {
      if (key == CODED)
      {
         int dx = 0;
         int dy = 0;
         switch (keyCode)
         {
            case UP:
               dy = -1;
               break;
            case DOWN:
               dy = 1;
               break;
            case LEFT:
               dx = -1;
               break;
            case RIGHT:
               dx = 1;
               break;
         }
         view.updateView(dx, dy);
      }
   }

   private static Background createDefaultBackground(ImageStore imageStore)
   {
      List<PImage> bgndImgs = imageStore.get(DEFAULT_IMAGE_NAME);
      return new Background(DEFAULT_IMAGE_NAME, bgndImgs);
   }

   private static PImage createImageColored(int width, int height, int color)
   {
      PImage img = new PImage(TILE_WIDTH, TILE_HEIGHT, RGB);
      img.loadPixels();
      for (int i = 0; i < img.pixels.length; i++)
      {
         img.pixels[i] = color;
      }
      img.updatePixels();
      return img;
   }


   private static void loadImages(String filename, ImageStore imageStore,
                                  PApplet screen)
   {
      URL path = ClassLoader.getSystemResource(filename);
      try
      {
         Scanner in = new Scanner(new File(path.getFile()));
         ImageStore.loadImages(in, imageStore, TILE_WIDTH,
                 TILE_HEIGHT, screen);
      }
      catch (FileNotFoundException e)
      {
         System.err.println(e.getMessage());
      }
   }

   private static void loadWorld(WorldModel world, String filename,
                                 ImageStore imageStore, Map<String, PropertyParser> parsers)
   {
      URL path = ClassLoader.getSystemResource(filename);
      try
      {
         Scanner in = new Scanner(new File(path.getFile()));
         WorldLoad.load(in, world, imageStore, parsers);
      }
      catch (FileNotFoundException e)
      {
         System.err.println(e.getMessage());
      }
   }

   private static final String BGND_KEY = "background";
   private static final int BGND_NUM_PROPERTIES = 4;
   private static final int BGND_NAME = 1;
   private static final int BGND_COL = 2;
   private static final int BGND_ROW = 3;

   private static final String MINER_KEY = "miner";
   private static final int MINER_NUM_PROPERTIES = 7;
   private static final int MINER_NAME = 1;
   private static final int MINER_LIMIT = 4;
   private static final int MINER_COL = 2;
   private static final int MINER_ROW = 3;
   private static final int MINER_RATE = 5;
   private static final int MINER_ANIMATION_RATE = 6;

   private static final String OBSTACLE_KEY = "obstacle";
   private static final int OBSTACLE_NUM_PROPERTIES = 4;
   private static final int OBSTACLE_NAME = 1;
   private static final int OBSTACLE_COL = 2;
   private static final int OBSTACLE_ROW = 3;

   private static final String ORE_KEY = "ore";
   private static final int ORE_NUM_PROPERTIES = 5;
   private static final int ORE_NAME = 1;
   private static final int ORE_COL = 2;
   private static final int ORE_ROW = 3;
   private static final int ORE_RATE = 4;

   private static final String SMITH_KEY = "blacksmith";
   private static final int SMITH_NUM_PROPERTIES = 4;
   private static final int SMITH_NAME = 1;
   private static final int SMITH_COL = 2;
   private static final int SMITH_ROW = 3;

   private static final String VEIN_KEY = "vein";
   private static final int VEIN_NUM_PROPERTIES = 6;
   private static final int VEIN_NAME = 1;
   private static final int VEIN_RATE = 4;
   private static final int VEIN_COL = 2;
   private static final int VEIN_ROW = 3;
   private static final int VEIN_REACH = 5;

   private static Map<String, PropertyParser> buildPropertyParsers(
           WorldModel world, ImageStore imageStore, long time)
   {
      Map<String, PropertyParser> parsers = new HashMap<>();

      parsers.put(BGND_KEY, properties -> {
         if (properties.length >= BGND_NUM_PROPERTIES)
         {
            Point pt = new Point(Integer.parseInt(properties[BGND_COL]),
                    Integer.parseInt(properties[BGND_ROW]));
            String name = properties[BGND_NAME];
            world.setBackground(pt, new Background(name,
                    imageStore.get(name)));
         }
      });

      parsers.put(MINER_KEY, properties -> {
         if (properties.length == MINER_NUM_PROPERTIES)
         {
            Point pt = new Point(Integer.parseInt(properties[MINER_COL]),
                    Integer.parseInt(properties[MINER_ROW]));
            Actor entity = new MinerNotFull(properties[MINER_NAME],
                    pt,
                    Integer.parseInt(properties[MINER_RATE]),
                    Integer.parseInt(properties[MINER_ANIMATION_RATE]),
                    Integer.parseInt(properties[MINER_LIMIT]),
                    imageStore.get(MINER_KEY));
            world.addEntity(entity);
            entity.schedule(world, time + entity.getRate(), imageStore);
         }
      });

      parsers.put(OBSTACLE_KEY, properties -> {
         if (properties.length == OBSTACLE_NUM_PROPERTIES)
         {
            Point pt = new Point(
                    Integer.parseInt(properties[OBSTACLE_COL]),
                    Integer.parseInt(properties[OBSTACLE_ROW]));
            WorldEntity entity = new Obstacle(properties[OBSTACLE_NAME],
                    pt,
                    imageStore.get(OBSTACLE_KEY));
            world.addEntity(entity);
         }
      });

      parsers.put(ORE_KEY, properties -> {
         if (properties.length == ORE_NUM_PROPERTIES)
         {
            Point pt = new Point(Integer.parseInt(properties[ORE_COL]),
                    Integer.parseInt(properties[ORE_ROW]));
            Actor entity = new Ore(properties[ORE_NAME],
                    pt,
                    Integer.parseInt(properties[ORE_RATE]),
                    imageStore.get(ORE_KEY));
            world.addEntity(entity);
            entity.schedule(world, time + entity.getRate(), imageStore);
         }
      });

      parsers.put(SMITH_KEY, properties -> {
         if (properties.length >= SMITH_NUM_PROPERTIES)
         {
            Point pt = new Point(Integer.parseInt(properties[SMITH_COL]),
                    Integer.parseInt(properties[SMITH_ROW]));
            WorldEntity entity = new Blacksmith(properties[SMITH_NAME],
                    pt,
                    imageStore.get(SMITH_KEY));
            world.addEntity(entity);
         }
      });

      parsers.put(VEIN_KEY, properties -> {
         if (properties.length == VEIN_NUM_PROPERTIES)
         {
            Point pt = new Point(Integer.parseInt(properties[VEIN_COL]),
                    Integer.parseInt(properties[VEIN_ROW]));
            Actor entity = new Vein(properties[VEIN_NAME],
                    pt,
                    Integer.parseInt(properties[VEIN_RATE]),
                    Integer.parseInt(properties[VEIN_REACH]),
                    imageStore.get(VEIN_KEY));
            world.addEntity(entity);
            entity.schedule(world, time + entity.getRate(), imageStore);
         }
      });

      return parsers;
   }

   public void drawPath()
   {
      int x = mouseX / TILE_WIDTH;
      int y = mouseY / TILE_HEIGHT;

      Point pt = view.viewportToWorld(WorldView.viewport, x, y);

      if(world.isOccupied(pt))
      {
         try
         {
            MobileAnimatedActor animatedActor = (MobileAnimatedActor) world.getTileOccupant(pt);
            ArrayList<Point> path = animatedActor.getPath();
            for (int i = 0; i < path.size() - 1; i++)
            {
               Point pt2 = view.worldToViewport(WorldView.viewport, path.get(i).x, path.get(i).y);
               image(loadImage("grass2.png"),
                       (pt2.x * TILE_WIDTH) + (TILE_WIDTH / 8),
                       (pt2.y * TILE_HEIGHT) + (TILE_HEIGHT / 8));
            }
         }
         catch (ClassCastException e)
         {

         }
      }
   }

   public static void main(String[] args)
   {
      PApplet.main("Main");
   }
}